package com.gestion.evenements.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Modality;

import com.gestion.evenements.model.*;
import com.gestion.evenements.model.evenementparticulier.*;
import com.gestion.evenements.observer.UIObserver;
import com.gestion.evenements.util.DataSynchronizer;
import com.gestion.evenements.auth.User;
import com.gestion.evenements.auth.UserRole;
import com.gestion.evenements.exception.*;
import com.gestion.evenements.ui.utils.ModernNotificationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.scene.text.Text;

/**
 * Application pour l'interface Participant - Version connectée avec notifications modernes
 * Interface dédiée aux participants pour consulter et s'inscrire aux événements
 * Utilise le pattern Observer pour les mises à jour en temps réel
 */
public class ParticipantApp extends Application {

    private BorderPane mainLayout;
    private VBox sideBar;
    private BorderPane contentArea;
    private ScrollPane contentScrollPane;
    private ScrollPane sideBarScrollPane;
    private Button selectedButton;
    private Label globalStatusLabel;
    
    // Services et synchronisation
    private DataSynchronizer dataSynchronizer;
    private UIObserver participantUIObserver;
    private GestionEvenements gestionEvenements;
    
    // Utilisateur connecté (réel ou invité)
    private User currentUser;
    private Participant currentParticipant;
    private boolean isGuest = false;
    
    
    // Containers pour les vues dynamiques
    private VBox dashboardContainer;
    private VBox availableEventsContainer;
    private VBox myEventsContainer;
    private VBox historyContainer;


    /**
     * Définit l'utilisateur connecté (appelé depuis LoginView)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        if (user == null) {
            // Mode invité
            this.isGuest = true;
            this.currentParticipant = new Participant(
                "GUEST_" + System.currentTimeMillis(), 
                "Invité", 
                "invite@guest.com"
            );
            System.out.println("✅ Mode invité activé");
        } else if (user.getRole() == UserRole.PARTICIPANT) {
            // Utilisateur participant connecté
            this.currentParticipant = new Participant(
                "PART_" + user.getId(), 
                user.getNom(), 
                user.getEmail()
            );
            System.out.println("✅ Participant connecté: " + user.getNom());
        } else {
            throw new IllegalArgumentException("L'utilisateur doit être un participant ou null (invité)");
        }
    }


    @Override
    public void start(Stage primaryStage) {


        // Initialiser avec invité si aucun utilisateur défini
        if (currentUser == null && currentParticipant == null) {
            setCurrentUser(null); // Mode invité
        }

        // Initialisation des services et du participant connecté
        initializeServices();
        
        // Configuration de la fenêtre principale
        primaryStage.setTitle("Gestion d'Événements - Espace Participant ("+ currentUser.getNom() + ")");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        // Chargement des polices
        loadFonts();

        synchronizeWithExistingEvents();
        
        // Création de l'interface
        createUI();

        // Création de la scène avec CSS
        Scene scene = new Scene(mainLayout, 1008, 720);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Afficher le tableau de bord par défaut
        showDashboard();
        
        // Message de bienvenue moderne
        showWelcomeMessage();
    }
    
    private void initializeServices() {
        // Initialiser les services
        dataSynchronizer = DataSynchronizer.getInstance();
        gestionEvenements = GestionEvenements.getInstance();
        
        
        // Créer un observer pour mettre à jour l'interface
        participantUIObserver = new UIObserver(globalStatusLabel, this::refreshCurrentView);
        
        // Ajouter l'observer global
        dataSynchronizer.addGlobalObserver(participantUIObserver);
    }
    
   /* private void initializeCurrentParticipant() {
        // Chercher le participant dans les événements existants
        currentParticipant = gestionEvenements.getEvenements().values().stream()
            .flatMap(e -> e.getParticipants().stream())
            .filter(p -> p.getEmail().equals(participantEmail))
            .findFirst()
            .orElse(null);
        
        // Si pas trouvé, créer un nouveau participant
        if (currentParticipant == null) {
            currentParticipant = new Participant("PART_CURRENT", participantName, participantEmail);
            
            // L'inscrire automatiquement à un événement de démo
            try {
                List<Evenement> events = gestionEvenements.getEvenements().values().stream()
                    .limit(2)
                    .collect(Collectors.toList());
                
                for (Evenement event : events) {
                    if (event.getParticipants().size() < event.getCapaciteMax()) {
                        event.ajouterParticipant(currentParticipant);
                        break;
                    }
                }
            } catch (CapaciteMaxAtteinteException e) {
                System.err.println("Impossible d'inscrire le participant de démo: " + e.getMessage());
            }
        }
    }*/

    private void synchronizeWithExistingEvents() {
        // Chercher si le participant est déjà inscrit à des événements existants
        if (!isGuest && currentUser != null) {
            List<Evenement> allEvents = gestionEvenements.getEvenements().values().stream()
                .collect(Collectors.toList());
            
            for (Evenement event : allEvents) {
                // Vérifier si un participant avec le même email existe
                Optional<Participant> existingParticipant = event.getParticipants().stream()
                    .filter(p -> p.getEmail().equals(currentParticipant.getEmail()))
                    .findFirst();
                
                if (existingParticipant.isPresent()) {
                    // Remplacer par notre instance pour maintenir la cohérence
                    event.retirerParticipant(existingParticipant.get());
                    try {
                        event.ajouterParticipant(currentParticipant);
                        System.out.println("✅ Participant synchronisé avec l'événement: " + event.getNom());
                    } catch (CapaciteMaxAtteinteException e) {
                        System.err.println("⚠️ Impossible de synchroniser avec l'événement " + event.getNom() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private void loadFonts() {
        Font font = Font.loadFont(
            getClass().getResourceAsStream("/com/gestion/evenements/ui/fonts/Poppins-Regular.ttf"), 12
        );
        
        if (font == null) {
            System.out.println("⚠️ Police Poppins non trouvée, utilisation de la police par défaut");
        }
    }
    
    private void createUI() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-container");
        
        // Création de la barre latérale
        createSideBar();
        
        // Création de la zone de contenu
        createResponsiveContentArea();
        
        // Assemblage final
        mainLayout.setLeft(sideBarScrollPane);
        mainLayout.setCenter(contentScrollPane);
    }
    
    private void createSideBar() {



        sideBar = new VBox();
        sideBar.getStyleClass().add("sidebar");
        sideBar.setPrefWidth(280);
        sideBar.setSpacing(8);
        sideBar.setPadding(new Insets(24, 0, 24, 0));

        // Wrap content area dans un ScrollPane stylisé
        sideBarScrollPane = new ScrollPane(sideBar);
        sideBarScrollPane.getStyleClass().add("content-scroll");
        sideBarScrollPane.setFitToWidth(true);
        sideBarScrollPane.setFitToHeight(false);
        sideBarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sideBarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Style personnalisé pour le scrollpane du contenu
        sideBarScrollPane.setStyle(
            "-fx-background: white;" +
            "-fx-background-color: white;" +
            "-fx-border-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        
        // S'assurer que le contenu s'étend correctement
        VBox.setVgrow(sideBarScrollPane, Priority.ALWAYS);
        HBox.setHgrow(sideBarScrollPane, Priority.ALWAYS);
        
        // Logo et titre de l'application
        VBox header = createHeader();
        
        // Profil utilisateur connecté
        VBox userProfile = createUserProfile();
        
        // Boutons de navigation
        Button btnDashboard = createNavButton("🏠", "Tableau de bord", "nav-button");
        Button btnEvents = createNavButton("📅", "Événements disponibles", "nav-button");
        Button btnMyEvents = createNavButton("🎫", "Mes inscriptions", "nav-button");
        Button btnHistory = createNavButton("📊", "Historique", "nav-button");
        
        // Actions des boutons avec gestion d'erreurs
        btnDashboard.setOnAction(e -> {
            setSelectedButton(btnDashboard);
            showDashboard();
        });
        
        btnEvents.setOnAction(e -> {
            setSelectedButton(btnEvents);
            showAvailableEvents();
        });
        
        btnMyEvents.setOnAction(e -> {
            setSelectedButton(btnMyEvents);
            showMyEvents();
        });
        
        btnHistory.setOnAction(e -> {
            setSelectedButton(btnHistory);
            showHistory();
        });
        
        // Spacer pour pousser les boutons vers le bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Boutons paramètres
        Button btnProfile = createNavButton("👤", "Mon profil", "nav-button-secondary");
        Button btnNotifications = createNavButton("🔔", "Notifications", "nav-button-secondary");
        Button btnLogout = createNavButton("🚪", "Déconnexion", "nav-button-secondary");
        
        // Actions des boutons
        btnProfile.setOnAction(e -> showProfile());
        btnNotifications.setOnAction(e -> showNotifications());
        btnLogout.setOnAction(e -> logout());
        
        sideBar.getChildren().addAll(
            header,
            new Separator(),
            userProfile,
            new Separator(),
            btnDashboard,
            btnEvents,
            btnMyEvents,
            btnHistory,
            spacer,
            new Separator(),
            btnProfile,
            btnNotifications,
            btnLogout
        );
        
        // Sélectionner le premier bouton par défaut
        selectedButton = btnDashboard;
        btnDashboard.getStyleClass().add("nav-button-selected");
    }
    
    private VBox createHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(12);
        header.setPadding(new Insets(0, 24, 24, 24));
        
        // Icône de l'application
        Label appIcon = new Label("🎯");
        appIcon.getStyleClass().add("app-icon");
        
        // Titre de l'application
        Label appTitle = new Label("Espace");
        appTitle.getStyleClass().add("app-title");
        
        Label appSubtitle = new Label("Participant");
        appSubtitle.getStyleClass().add("app-subtitle");
        
        Label appStatus = new Label("v2.0 - Moderne");
        appStatus.getStyleClass().add("text-secondary");
        appStatus.setStyle("-fx-font-size: 10px;");
        
        header.getChildren().addAll(appIcon, appTitle, appSubtitle, appStatus);
        return header;
    }
    
    private VBox createUserProfile() {
        VBox profile = new VBox();
        profile.setAlignment(Pos.CENTER);
        profile.setSpacing(8);
        profile.setPadding(new Insets(16, 24, 16, 24));
        profile.getStyleClass().add("stat-card");
        
        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 32px;");
        
        Label name = new Label(currentParticipant.getNom());
        name.getStyleClass().add("organizer-name");
        name.setStyle("-fx-font-size: 14px;");
        
        Label email = new Label(currentParticipant.getEmail());
        email.getStyleClass().add("text-secondary");
        email.setStyle("-fx-font-size: 12px;");
        
        // Statut de connexion
        globalStatusLabel = new Label("✅ Connecté");
        globalStatusLabel.getStyleClass().add("status-active");
        globalStatusLabel.setStyle("-fx-font-size: 11px;");
        
        profile.getChildren().addAll(avatar, name, email, globalStatusLabel);
        return profile;
    }
    
    private Button createNavButton(String icon, String text, String styleClass) {
        Button button = new Button();
        button.getStyleClass().add(styleClass);
        button.setPrefWidth(240);
        button.setMaxWidth(Double.MAX_VALUE);
        
        // Création du contenu du bouton avec icône et texte
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(16);
        
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("nav-icon");
        
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("nav-text");
        
        content.getChildren().addAll(iconLabel, textLabel);
        button.setGraphic(content);
        
        return button;
    }
    
    private void createResponsiveContentArea() {
        contentArea = new BorderPane();
        contentArea.getStyleClass().add("content-area");
        
        // Header par défaut
        updateContentHeader("Tableau de bord", "Vue d'ensemble de vos événements en temps réel");
        
        // Wrap content area dans un ScrollPane stylisé
        contentScrollPane = new ScrollPane(contentArea);
        contentScrollPane.getStyleClass().add("content-scroll");
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(false);
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Style personnalisé pour le scrollpane du contenu
        contentScrollPane.setStyle(
            "-fx-background: white;" +
            "-fx-background-color: white;" +
            "-fx-border-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        
        // S'assurer que le contenu s'étend correctement
        VBox.setVgrow(contentScrollPane, Priority.ALWAYS);
        HBox.setHgrow(contentScrollPane, Priority.ALWAYS);
    }
    
    private void updateContentHeader(String title, String subtitle) {
        VBox header = new VBox();
        header.getStyleClass().add("content-header");
        header.setSpacing(8);
        header.setPadding(new Insets(32, 32, 24, 32));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("content-title");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("content-subtitle");
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        contentArea.setTop(header);
    }
    
    private void setSelectedButton(Button button) {
        // Retirer la sélection du bouton précédent
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("nav-button-selected");
        }
        
        // Sélectionner le nouveau bouton
        selectedButton = button;
        button.getStyleClass().add("nav-button-selected");
    }
    
    // Méthodes pour changer le contenu
    private void showDashboard() {
        updateContentHeader("Tableau de bord", "Aperçu de vos activités et prochains événements");
        dashboardContainer = createDashboardView();
        contentArea.setCenter(dashboardContainer);
    }
    
    private void showAvailableEvents() {
        updateContentHeader("Événements disponibles", "Découvrez et inscrivez-vous aux nouveaux événements");
        availableEventsContainer = createAvailableEventsView();
        contentArea.setCenter(availableEventsContainer);
    }
    
    private void showMyEvents() {
        updateContentHeader("Mes inscriptions", "Gérez vos événements à venir et confirmés");
        myEventsContainer = createMyEventsView();
        contentArea.setCenter(myEventsContainer);
    }
    
    private void showHistory() {
        updateContentHeader("Historique", "Votre historique de participations et évaluations");
        historyContainer = createHistoryView();
        contentArea.setCenter(historyContainer);
    }
    
    private VBox createDashboardView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Statistiques personnelles (données en temps réel)
        HBox statsBar = createPersonalStatsBar();
        
        // Actions rapides
        HBox quickActions = createQuickActions();
        
        // Prochains événements
        VBox upcomingEvents = createUpcomingEventsSection();
        
        // Recommandations
        VBox recommendations = createRecommendationsSection();
        
        view.getChildren().addAll(statsBar, quickActions, upcomingEvents, recommendations);
        return view;
    }
    
    private HBox createPersonalStatsBar() {
        HBox statsBar = new HBox();
        statsBar.getStyleClass().add("stats-bar");
        statsBar.setSpacing(24);
        
        // Calculer les statistiques en temps réel
        long activeRegistrations = getMyRegisteredEvents().size();
        long completedEvents = getMyCompletedEvents().size();
        long upcomingEvents = getMyUpcomingEvents().size();
        
        VBox totalEvents = createStatCard(String.valueOf(activeRegistrations), "Inscriptions actives", "total-stat");
        VBox thisMonth = createStatCard(String.valueOf(upcomingEvents), "À venir", "positive-stat");
        VBox completed = createStatCard(String.valueOf(completedEvents), "Terminés", "info-stat");
        VBox points = createStatCard("150", "Points fidélité", "success-stat");
        
        statsBar.getChildren().addAll(totalEvents, thisMonth, completed, points);
        return statsBar;
    }
    
    private HBox createQuickActions() {
        HBox actions = new HBox();
        actions.setSpacing(16);
        actions.setPadding(new Insets(16, 0, 16, 0));
        
        Button browseEventsBtn = new Button("🔍 Parcourir les événements");
        browseEventsBtn.getStyleClass().add("primary-button");
        browseEventsBtn.setOnAction(e -> {
            setSelectedButton(null); // Réinitialiser pour forcer la sélection
            showAvailableEvents();
        });
        
        Button myProfileBtn = new Button("👤 Mon profil");
        myProfileBtn.getStyleClass().add("secondary-button");
        myProfileBtn.setOnAction(e -> showProfile());
        
        Button helpBtn = new Button("❓ Aide");
        helpBtn.getStyleClass().add("secondary-button");
        helpBtn.setOnAction(e -> showHelp());
        
        actions.getChildren().addAll(browseEventsBtn, myProfileBtn, helpBtn);
        return actions;
    }
    
    private VBox createUpcomingEventsSection() {
        VBox section = new VBox();
        section.getStyleClass().add("events-list");
        section.setSpacing(16);
        
        Label title = new Label("Mes prochains événements");
        title.getStyleClass().add("section-title");
        
        VBox eventsContainer = new VBox();
        eventsContainer.setSpacing(12);
        
        // Récupérer les événements à venir en temps réel
        List<Evenement> upcomingEvents = getMyUpcomingEvents();
        
        if (upcomingEvents.isEmpty()) {
            Label noEvents = new Label("Aucun événement à venir. Découvrez nos événements disponibles !");
            noEvents.getStyleClass().add("text-secondary");
            eventsContainer.getChildren().add(noEvents);
        } else {
            for (Evenement event : upcomingEvents) {
                HBox eventCard = createMyEventCard(event, "Confirmé");
                eventsContainer.getChildren().add(eventCard);
            }
        }
        
        section.getChildren().addAll(title, eventsContainer);
        return section;
    }
    
    private VBox createRecommendationsSection() {
        VBox section = new VBox();
        section.getStyleClass().add("events-list");
        section.setSpacing(16);
        
        Label title = new Label("Recommandations pour vous");
        title.getStyleClass().add("section-title");
        
        VBox recoContainer = new VBox();
        recoContainer.setSpacing(12);
        
        // Récupérer les événements recommandés (ceux où l'utilisateur n'est pas inscrit)
        List<Evenement> recommendedEvents = gestionEvenements.getEvenements().values().stream()
            .filter(e -> !e.getParticipants().contains(currentParticipant))
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .limit(3)
            .collect(Collectors.toList());
        
        if (recommendedEvents.isEmpty()) {
            Label noReco = new Label("Aucune recommandation pour le moment.");
            noReco.getStyleClass().add("text-secondary");
            recoContainer.getChildren().add(noReco);
        } else {
            for (Evenement event : recommendedEvents) {
                HBox eventCard = createRecommendedEventCard(event);
                recoContainer.getChildren().add(eventCard);
            }
        }
        
        section.getChildren().addAll(title, recoContainer);
        return section;
    }
    
    private VBox createAvailableEventsView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Barre de recherche et filtres
        HBox searchBar = createSearchBar();
        
        // Liste des événements disponibles
        VBox eventsList = new VBox();
        eventsList.setSpacing(12);
        
        // Récupérer tous les événements disponibles
        List<Evenement> availableEvents = gestionEvenements.getEvenements().values().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
            .collect(Collectors.toList());
        
        if (availableEvents.isEmpty()) {
            Label noEvents = new Label("Aucun événement disponible pour le moment.");
            noEvents.getStyleClass().add("text-secondary");
            eventsList.getChildren().add(noEvents);
        } else {
            for (Evenement event : availableEvents) {
                HBox eventCard = createAvailableEventCard(event);
                eventsList.getChildren().add(eventCard);
            }
        }
        
        view.getChildren().addAll(searchBar, eventsList);
        return view;
    }
    
    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(16);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher un événement...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(400);
        
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("Tous", "Conférences", "Concerts", "Gratuits", "Payants");
        filterCombo.setValue("Tous");
        filterCombo.getStyleClass().add("search-field");
        
        Button searchBtn = new Button("🔍 Rechercher");
        searchBtn.getStyleClass().add("secondary-button");
        searchBtn.setOnAction(e -> filterEvents(searchField.getText(), filterCombo.getValue()));
        
        searchBar.getChildren().addAll(searchField, filterCombo, searchBtn);
        return searchBar;
    }
    
    private VBox createMyEventsView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Onglets pour séparer les événements
        HBox tabs = createEventTabs();
        
        // Liste des événements inscrits
        VBox eventsList = new VBox();
        eventsList.setSpacing(12);
        
        List<Evenement> myEvents = getMyRegisteredEvents();
        
        if (myEvents.isEmpty()) {
            Label noEvents = new Label("Vous n'êtes inscrit à aucun événement.");
            noEvents.getStyleClass().add("text-secondary");
            
            Button browseBtn = new Button("Découvrir les événements");
            browseBtn.getStyleClass().add("primary-button");
            browseBtn.setOnAction(e -> showAvailableEvents());
            
            VBox emptyState = new VBox();
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setSpacing(16);
            emptyState.getChildren().addAll(noEvents, browseBtn);
            
            eventsList.getChildren().add(emptyState);
        } else {
            for (Evenement event : myEvents) {
                HBox eventCard = createMyDetailedEventCard(event);
                eventsList.getChildren().add(eventCard);
            }
        }
        
        view.getChildren().addAll(tabs, eventsList);
        return view;
    }
    
    private HBox createEventTabs() {
        HBox tabs = new HBox();
        tabs.setSpacing(16);
        
        Button activeTab = new Button("Événements actifs (" + getMyUpcomingEvents().size() + ")");
        activeTab.getStyleClass().add("primary-button");
        
        Button pastTab = new Button("Événements passés (" + getMyCompletedEvents().size() + ")");
        pastTab.getStyleClass().add("secondary-button");
        
        tabs.getChildren().addAll(activeTab, pastTab);
        return tabs;
    }
    
    private VBox createHistoryView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Statistiques de participation
        HBox statsBar = createHistoryStatsBar();
        
        // Historique des événements
        VBox historyList = createHistoryList();
        
        view.getChildren().addAll(statsBar, historyList);
        return view;
    }
    
    private HBox createHistoryStatsBar() {
        HBox statsBar = new HBox();
        statsBar.getStyleClass().add("stats-bar");
        statsBar.setSpacing(24);
        
        long totalParticipations = getMyCompletedEvents().size();
        long thisYear = getMyCompletedEvents().stream()
            .filter(e -> e.getDate().getYear() == LocalDateTime.now().getYear())
            .count();
        
        VBox totalStat = createStatCard(String.valueOf(totalParticipations), "Participations totales", "total-stat");
        VBox yearStat = createStatCard(String.valueOf(thisYear), "Cette année", "positive-stat");
        VBox avgRating = createStatCard("4.2", "Note moyenne", "info-stat");
        VBox certificates = createStatCard("3", "Certificats obtenus", "success-stat");
        
        statsBar.getChildren().addAll(totalStat, yearStat, avgRating, certificates);
        return statsBar;
    }
    
    private VBox createHistoryList() {
        VBox historyList = new VBox();
        historyList.getStyleClass().add("events-list");
        historyList.setSpacing(16);
        
        Label title = new Label("Historique des participations");
        title.getStyleClass().add("section-title");
        
        VBox eventsContainer = new VBox();
        eventsContainer.setSpacing(8);
        
        List<Evenement> completedEvents = getMyCompletedEvents();
        
        if (completedEvents.isEmpty()) {
            Label noHistory = new Label("Aucun événement terminé pour le moment.");
            noHistory.getStyleClass().add("text-secondary");
            eventsContainer.getChildren().add(noHistory);
        } else {
            for (Evenement event : completedEvents) {
                HBox eventCard = createHistoryEventCard(event);
                eventsContainer.getChildren().add(eventCard);
            }
        }
        
        historyList.getChildren().addAll(title, eventsContainer);
        return historyList;
    }
    
    // Méthodes utilitaires pour récupérer les données en temps réel
    private List<Evenement> getMyRegisteredEvents() {
        return gestionEvenements.getEvenements().values().stream()
            .filter(e -> e.getParticipants().contains(currentParticipant))
            .collect(Collectors.toList());
    }
    
    private List<Evenement> getMyUpcomingEvents() {
        return getMyRegisteredEvents().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
            .collect(Collectors.toList());
    }
    
    private List<Evenement> getMyCompletedEvents() {
        return getMyRegisteredEvents().stream()
            .filter(e -> e.getDate().isBefore(LocalDateTime.now()))
            .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
            .collect(Collectors.toList());
    }
    
    // Méthodes pour créer les cartes d'événements
    private HBox createMyEventCard(Evenement evenement, String status) {
        HBox card = new HBox();
        card.getStyleClass().add("event-card");
        if (evenement instanceof Conference) {
            card.getStyleClass().add("conference-card");
        } else if (evenement instanceof Concert) {
            card.getStyleClass().add("concert-card");
        }
        
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        VBox.setVgrow(info, Priority.ALWAYS);
        
        String icon = evenement instanceof Conference ? "🎤" : "🎵";
        Label titleLabel = new Label(icon + " " + evenement.getNom());
        titleLabel.getStyleClass().add("event-title");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(evenement.getDate().format(formatter) + " • " + evenement.getLieu());
        dateLabel.getStyleClass().add("event-date");
        
        Label statusLabel = new Label("Statut: " + status);
        statusLabel.getStyleClass().add("event-participants");
        
        info.getChildren().addAll(titleLabel, dateLabel, statusLabel);
        
        Button detailsBtn = new Button("Détails");
        detailsBtn.getStyleClass().add("primary-button-small");
        detailsBtn.setOnAction(e -> showEventDetails(evenement));
        
        card.getChildren().addAll(info, detailsBtn);
        return card;
    }
    
    private HBox createAvailableEventCard(Evenement evenement) {
        HBox card = new HBox();
        card.getStyleClass().add("event-card");
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        VBox.setVgrow(info, Priority.ALWAYS);
        
        String icon = evenement instanceof Conference ? "🎤" : "🎵";
        Label titleLabel = new Label(icon + " " + evenement.getNom());
        titleLabel.getStyleClass().add("event-title");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(evenement.getDate().format(formatter) + " • " + evenement.getLieu());
        dateLabel.getStyleClass().add("event-date");
        
        int placesRestantes = evenement.getCapaciteMax() - evenement.getParticipants().size();
        Label placesLabel = new Label(placesRestantes + " places restantes");
        placesLabel.getStyleClass().add("text-secondary");
        
        info.getChildren().addAll(titleLabel, dateLabel, placesLabel);
        
        VBox priceAction = new VBox();
        priceAction.setSpacing(8);
        priceAction.setAlignment(Pos.CENTER_RIGHT);
        
        Label priceLabel = new Label("Gratuit");
        priceLabel.getStyleClass().add("event-participants");
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600;");
        
        Button registerBtn = new Button(evenement.getParticipants().contains(currentParticipant) ? "Inscrit ✓" : "S'inscrire");
        registerBtn.getStyleClass().add(evenement.getParticipants().contains(currentParticipant) ? 
                                        "secondary-button" : "primary-button-small");
        registerBtn.setDisable(evenement.getParticipants().contains(currentParticipant));
        registerBtn.setOnAction(e -> registerToEvent(evenement));
        
        priceAction.getChildren().addAll(priceLabel, registerBtn);
        
        card.getChildren().addAll(info, priceAction);
        return card;
    }
    
    private HBox createRecommendedEventCard(Evenement evenement) {
        return createAvailableEventCard(evenement); // Même format pour les recommandations
    }
    
    private HBox createMyDetailedEventCard(Evenement evenement) {
        HBox card = new HBox();
        card.getStyleClass().add("event-card");
        if (evenement instanceof Conference) {
            card.getStyleClass().add("conference-card");
        } else if (evenement instanceof Concert) {
            card.getStyleClass().add("concert-card");
        }
        
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        VBox.setVgrow(info, Priority.ALWAYS);
        
        String icon = evenement instanceof Conference ? "🎤" : "🎵";
        Label titleLabel = new Label(icon + " " + evenement.getNom());
        titleLabel.getStyleClass().add("event-title");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(evenement.getDate().format(formatter) + " • " + evenement.getLieu());
        dateLabel.getStyleClass().add("event-date");
        
        String status = evenement.getDate().isAfter(LocalDateTime.now()) ? "À venir" : "Terminé";
        Label statusLabel = new Label("Statut: " + status);
        statusLabel.getStyleClass().add(status.equals("À venir") ? "status-active" : "event-participants");
        
        info.getChildren().addAll(titleLabel, dateLabel, statusLabel);
        
        HBox actions = new HBox();
        actions.setSpacing(8);
        
        Button detailsBtn = new Button("Détails");
        detailsBtn.getStyleClass().add("secondary-button");
        detailsBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 12px;");
        detailsBtn.setOnAction(e -> showEventDetails(evenement));
        
        if (evenement.getDate().isAfter(LocalDateTime.now())) {
            Button cancelBtn = new Button("Se désinscrire");
            cancelBtn.getStyleClass().add("icon-button-danger");
            cancelBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 12px;");
            cancelBtn.setOnAction(e -> unregisterFromEvent(evenement));
            actions.getChildren().addAll(detailsBtn, cancelBtn);
        } else {
            Button rateBtn = new Button("Noter");
            rateBtn.getStyleClass().add("primary-button-small");
            rateBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 12px;");
            rateBtn.setOnAction(e -> rateEvent(evenement));
            actions.getChildren().addAll(detailsBtn, rateBtn);
        }
        
        card.getChildren().addAll(info, actions);
        return card;
    }
    
    private HBox createHistoryEventCard(Evenement evenement) {
        HBox card = new HBox();
        card.getStyleClass().add("event-card");
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        VBox.setVgrow(info, Priority.ALWAYS);
        
        String icon = evenement instanceof Conference ? "🎤" : "🎵";
        Label titleLabel = new Label(icon + " " + evenement.getNom());
        titleLabel.getStyleClass().add("event-title");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label dateLabel = new Label(evenement.getDate().format(formatter) + " • " + evenement.getLieu());
        dateLabel.getStyleClass().add("event-date");
        
        Label statusLabel = new Label("Terminé");
        statusLabel.getStyleClass().add("status-active");
        
        info.getChildren().addAll(titleLabel, dateLabel, statusLabel);
        
        VBox ratingSection = new VBox();
        ratingSection.setAlignment(Pos.CENTER_RIGHT);
        ratingSection.setSpacing(4);
        
        Label ratingLabel = new Label("⭐⭐⭐⭐⭐");
        ratingLabel.setStyle("-fx-font-size: 14px;");
        
        Label ratingText = new Label("Ma note");
        ratingText.getStyleClass().add("text-secondary");
        ratingText.setStyle("-fx-font-size: 10px;");
        
        ratingSection.getChildren().addAll(ratingLabel, ratingText);
        
        card.getChildren().addAll(info, ratingSection);
        return card;
    }
    
    private VBox createStatCard(String value, String label, String styleClass) {
        VBox card = new VBox();
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setAlignment(Pos.CENTER);
        card.setSpacing(8);
        card.setPrefWidth(150);
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("stat-label");
        
        card.getChildren().addAll(valueLabel, labelLabel);
        return card;
    }
    
    // Actions des événements avec notifications modernes
    private void registerToEvent(Evenement evenement) {
        try {
            evenement.ajouterParticipant(currentParticipant);
            showSuccessMessage("Inscription réussie à " + evenement.getNom());
            refreshCurrentView();
        } catch (CapaciteMaxAtteinteException e) {
            showError("Inscription impossible: " + e.getMessage());
        }
    }
    
    private void unregisterFromEvent(Evenement evenement) {
        boolean confirmed = ModernNotificationUtils.showConfirmation(
            "Confirmer la désinscription",
            "Êtes-vous sûr de vouloir vous désinscrire de \"" + evenement.getNom() + "\" ?\n\n" +
            "⚠️ Cette action est irréversible."
        );
        
        if (confirmed) {
            // Simulation d'une désinscription avec progression
            Stage progressDialog = ModernNotificationUtils.createProgressDialog(
                "Désinscription en cours",
                "Traitement de votre demande..."
            );
            progressDialog.show();
            
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Simulation du traitement
                    
                    Platform.runLater(() -> {
                        progressDialog.close();
                        evenement.retirerParticipant(currentParticipant);
                        
                        // Notification avec possibilité d'annulation
                        ModernNotificationUtils.showSnackbar(
                            "Désinscription réussie de \"" + evenement.getNom() + "\"",
                            "ANNULER",
                            () -> {
                                try {
                                    evenement.ajouterParticipant(currentParticipant);
                                    ModernNotificationUtils.showSuccessToast("Inscription restaurée");
                                    refreshCurrentView();
                                } catch (CapaciteMaxAtteinteException ex) {
                                    ModernNotificationUtils.showErrorToast("Impossible de restaurer: " + ex.getMessage());
                                }
                            }
                        );
                        
                        refreshCurrentView();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> {
                        progressDialog.close();
                        ModernNotificationUtils.showErrorToast("Désinscription interrompue");
                    });
                }
            }).start();
        }
    }
    
    private void showEventDetails(Evenement evenement) {
        // Créer un contenu riche pour les détails de l'événement
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setMaxWidth(500);
        
        // Informations principales
        VBox mainInfo = createEventMainInfo(evenement);
        
        // Informations spécifiques selon le type
        VBox specificInfo = createEventSpecificInfo(evenement);
        
        // Statut d'inscription
        VBox registrationStatus = createRegistrationStatus(evenement);
        
        // Actions rapides
        HBox quickActions = createEventQuickActions(evenement);
        
        content.getChildren().addAll(mainInfo, specificInfo, registrationStatus, quickActions);
        
        Stage detailsDialog = ModernNotificationUtils.createCustomDialog(
            "Détails de l'événement",
            content,
            true
        );
        
        detailsDialog.show();
    }
    
    private VBox createEventMainInfo(Evenement evenement) {
        VBox mainInfo = new VBox();
        mainInfo.getStyleClass().add("form-section");
        mainInfo.setSpacing(12);
        
        Label mainTitle = new Label("📋 Informations générales");
        mainTitle.getStyleClass().add("form-section-title");
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(8);
        
        int row = 0;
        addInfoRow(infoGrid, "Événement:", evenement.getNom(), row++);
        addInfoRow(infoGrid, "Date:", evenement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), row++);
        addInfoRow(infoGrid, "Lieu:", evenement.getLieu(), row++);
        addInfoRow(infoGrid, "Capacité:", evenement.getCapaciteMax() + " places", row++);
        addInfoRow(infoGrid, "Inscrits:", evenement.getParticipants().size() + " participants", row++);
        addInfoRow(infoGrid, "Places restantes:", (evenement.getCapaciteMax() - evenement.getParticipants().size()) + "", row++);
        
        mainInfo.getChildren().addAll(mainTitle, infoGrid);
        return mainInfo;
    }
    
    private VBox createEventSpecificInfo(Evenement evenement) {
        VBox specificInfo = new VBox();
        specificInfo.getStyleClass().add("form-section");
        specificInfo.setSpacing(12);
        
        Label specificTitle = new Label("✨ Détails spécifiques");
        specificTitle.getStyleClass().add("form-section-title");
        
        VBox specificDetails = new VBox();
        specificDetails.setSpacing(8);
        
        if (evenement instanceof Conference) {
            Conference conf = (Conference) evenement;
            Label typeLabel = new Label("🎤 Type: Conférence");
            typeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #1976d2;");
            
            Label themeLabel = new Label("Thème: " + conf.getTheme());
            themeLabel.getStyleClass().add("info-text");
            
            specificDetails.getChildren().addAll(typeLabel, themeLabel);
            
            if (!conf.getIntervenants().isEmpty()) {
                Label intervenantsLabel = new Label("Intervenants: " + 
                    conf.getIntervenants().stream()
                        .map(i -> i.getNom())
                        .collect(Collectors.joining(", ")));
                intervenantsLabel.getStyleClass().add("info-text");
                intervenantsLabel.setWrapText(true);
                specificDetails.getChildren().add(intervenantsLabel);
            }
        } else if (evenement instanceof Concert) {
            Concert concert = (Concert) evenement;
            Label typeLabel = new Label("🎵 Type: Concert");
            typeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #7b1fa2;");
            
            Label artisteLabel = new Label("Artiste: " + concert.getArtiste());
            artisteLabel.getStyleClass().add("info-text");
            
            Label genreLabel = new Label("Genre: " + concert.getGenreMusical());
            genreLabel.getStyleClass().add("info-text");
            
            specificDetails.getChildren().addAll(typeLabel, artisteLabel, genreLabel);
        }
        
        specificInfo.getChildren().addAll(specificTitle, specificDetails);
        return specificInfo;
    }
    
    private VBox createRegistrationStatus(Evenement evenement) {
        VBox statusSection = new VBox();
        statusSection.getStyleClass().add("form-section");
        statusSection.setSpacing(12);
        
        Label statusTitle = new Label("🎫 Votre statut");
        statusTitle.getStyleClass().add("form-section-title");
        
        boolean isRegistered = evenement.getParticipants().contains(currentParticipant);
        Label statusLabel = new Label(isRegistered ? "✅ Inscrit" : "❌ Non inscrit");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; " +
                           (isRegistered ? "-fx-text-fill: #4caf50;" : "-fx-text-fill: #f44336;"));
        
        if (isRegistered) {
            Label registrationInfo = new Label("Vous êtes inscrit à cet événement. Un rappel vous sera envoyé avant l'événement.");
            registrationInfo.getStyleClass().add("form-help-text");
            registrationInfo.setWrapText(true);
            registrationInfo.setStyle("-fx-background-color: #e8f5e8; -fx-padding: 12px; " +
                                    "-fx-border-color: #4caf50; -fx-border-radius: 4px; " +
                                    "-fx-background-radius: 4px;");
            statusSection.getChildren().addAll(statusTitle, statusLabel, registrationInfo);
        } else {
            Label availabilityInfo = new Label("Places disponibles. Inscrivez-vous maintenant pour participer.");
            availabilityInfo.getStyleClass().add("form-help-text");
            availabilityInfo.setWrapText(true);
            availabilityInfo.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 12px; " +
                                    "-fx-border-color: #2196f3; -fx-border-radius: 4px; " +
                                    "-fx-background-radius: 4px;");
            statusSection.getChildren().addAll(statusTitle, statusLabel, availabilityInfo);
        }
        
        return statusSection;
    }
    
    private HBox createEventQuickActions(Evenement evenement) {
        HBox actions = new HBox();
        actions.setSpacing(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        boolean isRegistered = evenement.getParticipants().contains(currentParticipant);
        boolean isFuture = evenement.getDate().isAfter(LocalDateTime.now());
        
        if (!isRegistered && isFuture) {
            Button registerBtn = new Button("📝 S'inscrire");
            registerBtn.getStyleClass().add("primary-button");
            registerBtn.setOnAction(e -> {
                registerToEvent(evenement);
                Stage stage = (Stage) registerBtn.getScene().getWindow();
                stage.close();
            });
            actions.getChildren().add(registerBtn);
        } else if (isRegistered && isFuture) {
            Button unregisterBtn = new Button("❌ Se désinscrire");
            unregisterBtn.getStyleClass().add("icon-button-danger");
            unregisterBtn.setOnAction(e -> {
                Stage stage = (Stage) unregisterBtn.getScene().getWindow();
                stage.close();
                unregisterFromEvent(evenement);
            });
            actions.getChildren().add(unregisterBtn);
        } else if (!isFuture) {
            Button rateBtn = new Button("⭐ Noter");
            rateBtn.getStyleClass().add("secondary-button");
            rateBtn.setOnAction(e -> {
                Stage stage = (Stage) rateBtn.getScene().getWindow();
                stage.close();
                rateEvent(evenement);
            });
            actions.getChildren().add(rateBtn);
        }
        
        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });
        
        actions.getChildren().add(closeBtn);
        return actions;
    }
    
    private void addInfoRow(GridPane grid, String label, String value, int row) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: 600; -fx-min-width: 120px;");
        
        Label valueNode = new Label(value);
        valueNode.setWrapText(true);
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
    
    private void rateEvent(Evenement evenement) {
        // Créer un dialog de notation moderne
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setMaxWidth(400);
        
        Label title = new Label("Noter votre expérience");
        title.getStyleClass().add("form-section-title");
        
        Label eventInfo = new Label("Événement: " + evenement.getNom());
        eventInfo.setStyle("-fx-font-weight: 600;");
        
        // Sélecteur d'étoiles (simulation)
        HBox starsContainer = new HBox();
        starsContainer.setSpacing(8);
        starsContainer.setAlignment(Pos.CENTER);
        
        for (int i = 1; i <= 5; i++) {
            Button star = new Button("⭐");
            star.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-border-color: transparent;");
            final int rating = i;
            star.setOnAction(e -> {
                ModernNotificationUtils.showSuccessToast("Note attribuée: " + rating + "/5 étoiles");
                Stage stage = (Stage) star.getScene().getWindow();
                stage.close();
            });
            starsContainer.getChildren().add(star);
        }
        
        Label instruction = new Label("Cliquez sur les étoiles pour noter (1-5)");
        instruction.getStyleClass().add("form-help-text");
        instruction.setAlignment(Pos.CENTER);
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Laissez un commentaire (optionnel)...");
        commentArea.setPrefRowCount(3);
        commentArea.getStyleClass().add("search-field");
        
        HBox actions = new HBox();
        actions.setSpacing(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button submitBtn = new Button("Envoyer la note");
        submitBtn.getStyleClass().add("primary-button");
        submitBtn.setOnAction(e -> {
            ModernNotificationUtils.showSuccessToast("Merci pour votre évaluation !");
            Stage stage = (Stage) submitBtn.getScene().getWindow();
            stage.close();
        });
        
        Button cancelBtn = new Button("Annuler");
        cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setOnAction(e -> {
            Stage stage = (Stage) cancelBtn.getScene().getWindow();
            stage.close();
        });
        
        actions.getChildren().addAll(cancelBtn, submitBtn);
        
        content.getChildren().addAll(title, eventInfo, starsContainer, instruction, 
                                     new Label("Commentaire:"), commentArea, actions);
        
        Stage ratingDialog = ModernNotificationUtils.createCustomDialog(
            "Évaluation de l'événement",
            content,
            true
        );
        
        ratingDialog.show();
    }
    
    private void filterEvents(String searchText, String filter) {
        // Recharger la vue avec les filtres appliqués
        showAvailableEvents();
        
        if (!searchText.trim().isEmpty() || !"Tous".equals(filter)) {
            ModernNotificationUtils.showInfoToast("Filtres appliqués: " + 
                (!searchText.trim().isEmpty() ? "\"" + searchText + "\"" : "") +
                (!"Tous".equals(filter) ? " • " + filter : ""));
        }
    }
    
    private void showProfile() {
        // Créer un dialog de profil moderne
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setMaxWidth(450);
        
        // En-tête du profil
        VBox profileHeader = new VBox();
        profileHeader.getStyleClass().add("form-section");
        profileHeader.setAlignment(Pos.CENTER);
        profileHeader.setSpacing(12);
        profileHeader.setPadding(new Insets(20));
        
        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 48px;");
        
        Label nameLabel = new Label(currentParticipant.getNom());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600;");
        
        Label emailLabel = new Label(currentParticipant.getEmail());
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        profileHeader.getChildren().addAll(avatar, nameLabel, emailLabel);
        
        // Statistiques personnelles
        VBox statsSection = new VBox();
        statsSection.getStyleClass().add("form-section");
        statsSection.setSpacing(12);
        
        Label statsTitle = new Label("📊 Vos statistiques");
        statsTitle.getStyleClass().add("form-section-title");
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(8);
        
        int row = 0;
        addInfoRow(statsGrid, "ID participant:", currentParticipant.getId(), row++);
        addInfoRow(statsGrid, "Événements inscrits:", String.valueOf(getMyRegisteredEvents().size()), row++);
        addInfoRow(statsGrid, "Événements terminés:", String.valueOf(getMyCompletedEvents().size()), row++);
        addInfoRow(statsGrid, "Points fidélité:", "150", row++);
        addInfoRow(statsGrid, "Membre depuis:", "2024", row++);
        
        statsSection.getChildren().addAll(statsTitle, statsGrid);
        
        // Actions du profil
        HBox actions = new HBox();
        actions.setSpacing(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("✏️ Modifier");
        editBtn.getStyleClass().add("secondary-button");
        editBtn.setOnAction(e -> ModernNotificationUtils.showInfoToast("Édition du profil à implémenter"));
        
        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("primary-button");
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });
        
        actions.getChildren().addAll(editBtn, closeBtn);
        
        content.getChildren().addAll(profileHeader, statsSection, actions);
        
        Stage profileDialog = ModernNotificationUtils.createCustomDialog(
            "Mon profil",
            content,
            true
        );
        
        profileDialog.show();
    }
    
    private void showNotifications() {
        // Créer un dialog de notifications moderne
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setMaxWidth(500);
        
        Label title = new Label("🔔 Vos notifications");
        title.getStyleClass().add("form-section-title");
        
        VBox notificationsList = new VBox();
        notificationsList.setSpacing(12);
        
        // Notifications simulées basées sur les données réelles
        String[][] notifications = generateNotifications();
        
        for (String[] notif : notifications) {
            HBox notificationCard = new HBox();
            notificationCard.getStyleClass().add("event-card");
            notificationCard.setSpacing(16);
            notificationCard.setAlignment(Pos.CENTER_LEFT);
            notificationCard.setPadding(new Insets(16));
            
            Label iconLabel = new Label(notif[0]);
            iconLabel.setStyle("-fx-font-size: 24px;");
            
            VBox notifInfo = new VBox();
            notifInfo.setSpacing(4);
            
            Label notifTitle = new Label(notif[1]);
            notifTitle.setStyle("-fx-font-weight: 600;");
            
            Label notifContent = new Label(notif[2]);
            notifContent.setStyle("-fx-text-fill: #666;");
            notifContent.setWrapText(true);
            
            Label notifTime = new Label(notif[3]);
            notifTime.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
            
            notifInfo.getChildren().addAll(notifTitle, notifContent, notifTime);
            
            notificationCard.getChildren().addAll(iconLabel, notifInfo);
            HBox.setHgrow(notifInfo, Priority.ALWAYS);
            
            notificationsList.getChildren().add(notificationCard);
        }
        
        ScrollPane notificationsScroll = new ScrollPane(notificationsList);
        notificationsScroll.getStyleClass().add("content-scroll");
        notificationsScroll.setPrefHeight(300);
        notificationsScroll.setFitToWidth(true);
        
        HBox actions = new HBox();
        actions.setSpacing(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button markAllReadBtn = new Button("✅ Tout marquer lu");
        markAllReadBtn.getStyleClass().add("secondary-button");
        markAllReadBtn.setOnAction(e -> ModernNotificationUtils.showSuccessToast("Toutes les notifications marquées comme lues"));
        
        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("primary-button");
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });
        
        actions.getChildren().addAll(markAllReadBtn, closeBtn);
        
        content.getChildren().addAll(title, notificationsScroll, actions);
        
        Stage notificationsDialog = ModernNotificationUtils.createCustomDialog(
            "Notifications",
            content,
            true
        );
        
        notificationsDialog.show();
    }
    
    private String[][] generateNotifications() {
        List<String[]> notifications = new java.util.ArrayList<>();
        
        // Notifications basées sur les événements réels
        for (Evenement event : getMyUpcomingEvents()) {
            long daysUntil = java.time.Duration.between(LocalDateTime.now(), event.getDate()).toDays();
            if (daysUntil <= 7) {
                notifications.add(new String[]{
                    "⏰",
                    "Rappel d'événement",
                    "\"" + event.getNom() + "\" aura lieu dans " + daysUntil + " jour(s)",
                    "Il y a " + (7 - daysUntil) + " jour(s)"
                });
            }
        }
        
        // Notifications système
        notifications.add(new String[]{
            "🔄",
            "Système mis à jour",
            "Interface participant mise à jour avec notifications modernes",
            "Il y a 2 heures"
        });
        
        notifications.add(new String[]{
            "🎉",
            "Nouveaux événements",
            "3 nouveaux événements disponibles cette semaine",
            "Il y a 1 jour"
        });
        
        return notifications.toArray(new String[0][0]);
    }
    
    private void showHelp() {
        // Créer un dialog d'aide moderne avec onglets
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setMaxWidth(600);
        
        Label title = new Label("❓ Guide d'utilisation");
        title.getStyleClass().add("form-section-title");
        
        // Sections d'aide
        VBox helpSections = new VBox();
        helpSections.setSpacing(16);
        
        String[][] helpItems = {
            {"🏠", "Tableau de bord", "Vue d'ensemble de vos activités et statistiques personnelles"},
            {"📅", "Événements disponibles", "Parcourez et inscrivez-vous aux nouveaux événements"},
            {"🎫", "Mes inscriptions", "Gérez vos événements confirmés et à venir"},
            {"📊", "Historique", "Consultez vos participations passées et évaluations"},
            {"👤", "Mon profil", "Consultez et modifiez vos informations personnelles"},
            {"🔔", "Notifications", "Recevez des rappels et actualités en temps réel"}
        };
        
        for (String[] item : helpItems) {
            HBox helpCard = new HBox();
            helpCard.getStyleClass().add("event-card");
            helpCard.setSpacing(16);
            helpCard.setAlignment(Pos.CENTER_LEFT);
            helpCard.setPadding(new Insets(16));
            
            Label icon = new Label(item[0]);
            icon.setStyle("-fx-font-size: 24px;");
            
            VBox itemInfo = new VBox();
            itemInfo.setSpacing(4);
            
            Label itemTitle = new Label(item[1]);
            itemTitle.setStyle("-fx-font-weight: 600;");
            
            Label itemDesc = new Label(item[2]);
            itemDesc.setStyle("-fx-text-fill: #666;");
            itemDesc.setWrapText(true);
            
            itemInfo.getChildren().addAll(itemTitle, itemDesc);
            
            helpCard.getChildren().addAll(icon, itemInfo);
            HBox.setHgrow(itemInfo, Priority.ALWAYS);
            
            helpSections.getChildren().add(helpCard);
        }
        
        // Informations système
        VBox systemInfo = new VBox();
        systemInfo.getStyleClass().add("form-section");
        systemInfo.setSpacing(8);
        
        Label systemTitle = new Label("💡 Informations système");
        systemTitle.getStyleClass().add("form-section-title");
        
        Label systemDesc = new Label(
            "• Interface connectée en temps réel avec notifications modernes\n" +
            "• Synchronisation automatique avec le système\n" +
            "• Notifications push pour les rappels d'événements\n" +
            "• Sauvegarde automatique de vos préférences"
        );
        systemDesc.getStyleClass().add("form-help-text");
        systemDesc.setWrapText(true);
        
        systemInfo.getChildren().addAll(systemTitle, systemDesc);
        
        ScrollPane helpScroll = new ScrollPane(helpSections);
        helpScroll.getStyleClass().add("content-scroll");
        helpScroll.setPrefHeight(300);
        helpScroll.setFitToWidth(true);
        
        HBox actions = new HBox();
        actions.setSpacing(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button contactBtn = new Button("📧 Contacter le support");
        contactBtn.getStyleClass().add("secondary-button");
        contactBtn.setOnAction(e -> ModernNotificationUtils.showInfoToast("Redirection vers le support..."));
        
        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("primary-button");
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });
        
        actions.getChildren().addAll(contactBtn, closeBtn);
        
        content.getChildren().addAll(title, helpScroll, systemInfo, actions);
        
        Stage helpDialog = ModernNotificationUtils.createCustomDialog(
            "Aide et support",
            content,
            true
        );
        
        helpDialog.show();
    }
    
    private void logout() {
        boolean confirmed = ModernNotificationUtils.showConfirmation(
            "Confirmer la déconnexion",
            "Êtes-vous sûr de vouloir vous déconnecter ?\n\n" +
            "✅ Vos données sont sauvegardées automatiquement.\n" +
            "🔄 Vos inscriptions restent actives."
        );
        
        if (confirmed) {
            // Animation de déconnexion
            ModernNotificationUtils.showInfoToast("Déconnexion en cours...");
            
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        // Nettoyer les observers
                        dataSynchronizer.removeGlobalObserver(participantUIObserver);
                        ModernNotificationUtils.showSuccessToast("Déconnecté avec succès");
                        javafx.application.Platform.exit();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    private void showWelcomeMessage() {
        // Message de bienvenue moderne avec animations
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setMaxWidth(600);
        
        // En-tête de bienvenue
        VBox welcomeHeader = new VBox();
        welcomeHeader.getStyleClass().add("form-section");
        welcomeHeader.setAlignment(Pos.CENTER);
        welcomeHeader.setSpacing(12);
        welcomeHeader.setPadding(new Insets(20));
        
        Label welcomeIcon = new Label("🎉");
        welcomeIcon.setStyle("-fx-font-size: 48px;");
        
        Label welcomeTitle = new Label("Bienvenue " + currentParticipant.getNom() + " !");
        welcomeTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 600;");
        
        Label welcomeSubtitle = new Label("Espace Participant - Connecté en Temps Réel & Interface Moderne");
        welcomeSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        welcomeHeader.getChildren().addAll(welcomeIcon, welcomeTitle, welcomeSubtitle);
        
        // Statistiques actuelles
        VBox statsSection = new VBox();
        statsSection.getStyleClass().add("form-section");
        statsSection.setSpacing(12);
        
        Label statsTitle = new Label("📊 Votre situation actuelle");
        statsTitle.getStyleClass().add("form-section-title");
        
        HBox statsBar = new HBox();
        statsBar.setSpacing(16);
        statsBar.setAlignment(Pos.CENTER);
        
        int registeredCount = getMyRegisteredEvents().size();
        int upcomingCount = getMyUpcomingEvents().size();
        int completedCount = getMyCompletedEvents().size();
        
        VBox registeredCard = createQuickStatCard(String.valueOf(registeredCount), "Inscriptions", "#2196f3");
        VBox upcomingCard = createQuickStatCard(String.valueOf(upcomingCount), "À venir", "#4caf50");
        VBox completedCard = createQuickStatCard(String.valueOf(completedCount), "Terminés", "#ff9800");
        
        statsBar.getChildren().addAll(registeredCard, upcomingCard, completedCard);
        statsSection.getChildren().addAll(statsTitle, statsBar);
        
        // Fonctionnalités nouvelles
        VBox featuresSection = new VBox();
        featuresSection.getStyleClass().add("form-section");
        featuresSection.setSpacing(12);
        
        Label featuresTitle = new Label("✨ Nouvelles fonctionnalités");
        featuresTitle.getStyleClass().add("form-section-title");
        
        VBox featuresList = new VBox();
        featuresList.setSpacing(8);
        
        String[] features = {
            "✅ Notifications modernes avec animations",
            "✅ Dialogs interactifs et responsive",
            "✅ Confirmations avec actions d'annulation",
            "✅ Barres de progression pour les actions",
            "✅ Tooltips et indications visuelles",
            "✅ Interface adaptative et moderne"
        };
        
        for (String feature : features) {
            Label featureLabel = new Label(feature);
            featureLabel.getStyleClass().add("info-text");
            featuresList.getChildren().add(featureLabel);
        }
        
        featuresSection.getChildren().addAll(featuresTitle, featuresList);
        
        // Message d'encouragement
        Label encouragement = new Label(
            "🚀 Découvrez une expérience utilisateur entièrement repensée avec des interactions fluides " +
            "et des retours visuels enrichis. Toutes vos actions sont confirmées et peuvent être annulées."
        );
        encouragement.getStyleClass().add("form-help-text");
        encouragement.setWrapText(true);
        encouragement.setStyle("-fx-background-color: #e8f5e8; -fx-padding: 16px; " +
                              "-fx-border-color: #4caf50; -fx-border-radius: 8px; " +
                              "-fx-background-radius: 8px; -fx-text-fill: #2e7d32;");
        
        HBox actions = new HBox();
        actions.setSpacing(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button exploreBtn = new Button("🔍 Explorer les événements");
        exploreBtn.getStyleClass().add("primary-button");
        exploreBtn.setOnAction(e -> {
            Stage stage = (Stage) exploreBtn.getScene().getWindow();
            stage.close();
            showAvailableEvents();
        });
        
        Button profileBtn = new Button("👤 Mon profil");
        profileBtn.getStyleClass().add("secondary-button");
        profileBtn.setOnAction(e -> {
            Stage stage = (Stage) profileBtn.getScene().getWindow();
            stage.close();
            showProfile();
        });
        
        Button closeBtn = new Button("Commencer");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
            ModernNotificationUtils.showSuccessToast("Bienvenue dans votre espace moderne !");
        });
        
        actions.getChildren().addAll(exploreBtn, profileBtn, closeBtn);
        
        content.getChildren().addAll(welcomeHeader, statsSection, featuresSection, encouragement, actions);
        
        Stage welcomeDialog = ModernNotificationUtils.createCustomDialog(
            "Bienvenue dans votre espace",
            content,
            true
        );
        
        welcomeDialog.show();
    }
    
    private VBox createQuickStatCard(String value, String label, String color) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(4);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-border-color: " + color + "; -fx-border-width: 1px; " +
                      "-fx-border-radius: 8px; -fx-background-radius: 8px; " +
                      "-fx-background-color: derive(" + color + ", 95%);");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-text-alignment: center;");
        labelLabel.setWrapText(true);
        labelLabel.setMaxWidth(80);
        
        card.getChildren().addAll(valueLabel, labelLabel);
        return card;
    }
    
    private void refreshCurrentView() {
        // Rafraîchir la vue actuelle
        if (selectedButton != null) {
            selectedButton.fire();
        }
    }
    
    private void showError(String message) {
        globalStatusLabel.setText("❌ " + message);
        globalStatusLabel.getStyleClass().clear();
        globalStatusLabel.getStyleClass().add("status-inactive");
        
        ModernNotificationUtils.showErrorToast(message);
    }
    
    private void showSuccessMessage(String message) {
        globalStatusLabel.setText("✅ " + message);
        globalStatusLabel.getStyleClass().clear();
        globalStatusLabel.getStyleClass().add("status-active");
        
        // Toast pour feedback immédiat
        ModernNotificationUtils.showSuccessToast(message);
        
        // Revenir au statut normal après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    globalStatusLabel.setText("✅ Connecté");
                    globalStatusLabel.getStyleClass().clear();
                    globalStatusLabel.getStyleClass().add("status-active");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    @Override
    public void stop() throws Exception {
        // Nettoyer les observers lors de la fermeture
        if (dataSynchronizer != null && participantUIObserver != null) {
            dataSynchronizer.removeGlobalObserver(participantUIObserver);
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}