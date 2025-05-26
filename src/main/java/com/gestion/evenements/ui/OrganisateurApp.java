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

/**
 * Application pour l'interface Organisateur - Version connectée et responsive
 * Interface dédiée aux organisateurs pour gérer leurs événements
 * Utilise le pattern Observer pour les mises à jour en temps réel
 * Design responsive avec ScrollPane stylisés
 */
public class OrganisateurApp extends Application {

    private BorderPane mainLayout;
    private ScrollPane sideBarScrollPane;
    private VBox sideBar;
    private ScrollPane contentScrollPane;
    private BorderPane contentArea;
    private Button selectedButton;
    private Label globalStatusLabel;
    
    // Services et synchronisation
    private DataSynchronizer dataSynchronizer;
    private UIObserver organizerUIObserver;
    private GestionEvenements gestionEvenements;
    
     // Utilisateur connecté (réel)
    private User currentUser;
    private Organisateur currentOrganizer;
   /*private String organizerName = "Jean Dupont";
    private String organizerRole = "Directeur Événementiel";
    private String organizerCompany = "EventPro";
    private String organizerEmail = "jean.dupont@eventpro.com";*/
    
    // Containers pour les vues dynamiques
    private VBox dashboardContainer;
    private VBox myEventsContainer;
    private VBox participantsContainer;
    private VBox reportsContainer;


        /**
     * Définit l'utilisateur connecté (appelé depuis LoginView)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getRole() == UserRole.ORGANISATEUR) {
            // Créer l'organisateur à partir de l'utilisateur connecté
            this.currentOrganizer = new Organisateur(
                "ORG_" + user.getId(), 
                user.getNom(), 
                user.getEmail()
            );
            System.out.println("✅ Organisateur connecté: " + user.getNom());
        } else {
            throw new IllegalArgumentException("L'utilisateur doit être un organisateur");
        }
    }

    @Override
    public void start(Stage primaryStage) {

        // Vérifier que l'utilisateur est défini
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur connecté défini");
        }

        // Initialisation des services et de l'organisateur connecté
        initializeServices();
        
        // Configuration de la fenêtre principale
        primaryStage.setTitle("Gestion d'Événements - Espace Organisateur (" + currentUser.getNom() + ")");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        // Chargement des polices
        loadFonts();

        // Associer les événements existants à l'organisateur
        associateExistingEvents();

        
        // Création de l'interface responsive
        createResponsiveUI();

        // Création de la scène avec CSS
        Scene scene = new Scene(mainLayout, 1008, 720);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Afficher le tableau de bord par défaut
        showDashboard();
        
        // Message de bienvenue
        showWelcomeMessage();
    }
    
    private void initializeServices() {
        // Initialiser les services
        dataSynchronizer = DataSynchronizer.getInstance();
        gestionEvenements = GestionEvenements.getInstance();
    
        // Créer un observer pour mettre à jour l'interface
        organizerUIObserver = new UIObserver(globalStatusLabel, this::refreshCurrentView);
        
        // Ajouter l'observer global
        dataSynchronizer.addGlobalObserver(organizerUIObserver);
    }
    
    /*private void initializeCurrentOrganizer() {
        // Créer l'organisateur actuel
        currentOrganizer = new Organisateur("ORG_CURRENT", organizerName, organizerEmail);
        
        // Assigner automatiquement quelques événements existants
        List<Evenement> existingEvents = gestionEvenements.getEvenements().values().stream()
            .limit(3) // Prendre les 3 premiers événements
            .collect(Collectors.toList());
        
        for (Evenement event : existingEvents) {
            currentOrganizer.organiserEvenement(event);
        }
    }*/

       private void associateExistingEvents() {
        // Associer les événements existants qui pourraient appartenir à cet organisateur
        // (basé sur l'email ou d'autres critères)
        List<Evenement> allEvents = gestionEvenements.getEvenements().values().stream()
            .collect(Collectors.toList());
        
        for (Evenement event : allEvents) {
            // Pour la démo, on associe quelques événements à l'organisateur connecté
            // En production, on utiliserait une vraie logique de propriété
            if (event.getNom().contains("Tech") || event.getNom().contains("Innovation")) {
                currentOrganizer.organiserEvenement(event);
            }
        }
        
        System.out.println("✅ " + currentOrganizer.getEvenementsOrganises().size() + 
                          " événements associés à " + currentUser.getNom());
    }
    
    private void loadFonts() {
        Font font = Font.loadFont(
            getClass().getResourceAsStream("/com/gestion/evenements/ui/fonts/Poppins-Regular.ttf"), 12
        );
        
        if (font == null) {
            System.out.println("⚠️ Police Poppins non trouvée, utilisation de la police par défaut");
        }
    }
    
    private void createResponsiveUI() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-container");
        
        // Création de la barre latérale avec ScrollPane
        createResponsiveSideBar();
        
        // Création de la zone de contenu avec ScrollPane
        createResponsiveContentArea();
        
        // Assemblage final
        mainLayout.setLeft(sideBarScrollPane);
        mainLayout.setCenter(contentScrollPane);
    }
    
    private void createResponsiveSideBar() {
        // Création de la sidebar
        sideBar = new VBox();
        sideBar.getStyleClass().add("sidebar");
        sideBar.setPrefWidth(280);
        sideBar.setSpacing(8);
        sideBar.setPadding(new Insets(24, 0, 24, 0));
        
        // Logo et titre de l'application
        VBox header = createHeader();
        
        // Profil organisateur connecté
        VBox organizerProfile = createOrganizerProfile();
        
        // Boutons de navigation
        Button btnDashboard = createNavButton("📊", "Tableau de bord", "nav-button");
        Button btnMyEvents = createNavButton("📅", "Mes événements", "nav-button");
        Button btnCreateEvent = createNavButton("➕", "Créer un événement", "nav-button");
        Button btnParticipants = createNavButton("👥", "Participants", "nav-button");
        Button btnReports = createNavButton("📈", "Rapports", "nav-button");
        
        // Actions des boutons avec gestion d'erreurs
        btnDashboard.setOnAction(e -> {
            setSelectedButton(btnDashboard);
            showDashboard();
        });
        
        btnMyEvents.setOnAction(e -> {
            setSelectedButton(btnMyEvents);
            showMyEvents();
        });
        
        btnCreateEvent.setOnAction(e -> {
            setSelectedButton(btnCreateEvent);
            showCreateEvent();
        });
        
        btnParticipants.setOnAction(e -> {
            setSelectedButton(btnParticipants);
            showParticipants();
        });
        
        btnReports.setOnAction(e -> {
            setSelectedButton(btnReports);
            showReports();
        });
        
        // Spacer pour pousser les boutons vers le bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Boutons paramètres
        Button btnSettings = createNavButton("⚙️", "Paramètres", "nav-button-secondary");
        Button btnSupport = createNavButton("❓", "Support", "nav-button-secondary");
        Button btnLogout = createNavButton("🚪", "Déconnexion", "nav-button-secondary");
        
        // Actions des boutons
        btnSettings.setOnAction(e -> showSettings());
        btnSupport.setOnAction(e -> showSupport());
        btnLogout.setOnAction(e -> logout());
        
        sideBar.getChildren().addAll(
            header,
            new Separator(),
            organizerProfile,
            new Separator(),
            btnDashboard,
            btnMyEvents,
            btnCreateEvent,
            btnParticipants,
            btnReports,
            spacer,
            new Separator(),
            btnSettings,
            btnSupport,
            btnLogout
        );
        
        // Sélectionner le premier bouton par défaut
        selectedButton = btnDashboard;
        btnDashboard.getStyleClass().add("nav-button-selected");
        
        // Wrap sidebar dans un ScrollPane stylisé
        sideBarScrollPane = new ScrollPane(sideBar);
        sideBarScrollPane.getStyleClass().add("sidebar-scroll");
        sideBarScrollPane.setFitToWidth(true);
        sideBarScrollPane.setFitToHeight(true);
        sideBarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideBarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sideBarScrollPane.setPrefWidth(280);
        sideBarScrollPane.setMaxWidth(280);
        sideBarScrollPane.setMinWidth(280);
        
        // Style personnalisé pour le scrollpane
        sideBarScrollPane.setStyle(
            "-fx-background: #f8fafc;" +
            "-fx-background-color: #f8fafc;" +
            "-fx-border-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
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
        
        Label appSubtitle = new Label("Organisateur");
        appSubtitle.getStyleClass().add("app-subtitle");
        
        Label appStatus = new Label("v2.0 - Responsive");
        appStatus.getStyleClass().add("text-secondary");
        appStatus.setStyle("-fx-font-size: 10px;");
        
        header.getChildren().addAll(appIcon, appTitle, appSubtitle, appStatus);
        return header;
    }
    
    private VBox createOrganizerProfile() {
        VBox profile = new VBox();
        profile.setAlignment(Pos.CENTER);
        profile.setSpacing(8);
        profile.setPadding(new Insets(16, 24, 16, 24));
        profile.getStyleClass().add("organizer-card");
        
        Label avatar = new Label("👨‍💼");
        avatar.setStyle("-fx-font-size: 32px;");
        
        Label name = new Label(currentUser.getNom());
        name.getStyleClass().add("organizer-name");
        name.setStyle("-fx-font-size: 14px;");
        
        Label role = new Label(currentUser.getRole().toString());
        role.getStyleClass().add("organizer-role");
        role.setStyle("-fx-font-size: 12px;");
        
        Label company = new Label(currentUser.getOrganisation() != null ? 
                                 currentUser.getOrganisation() : "EventPro");
        company.getStyleClass().add("text-secondary");
        company.setStyle("-fx-font-size: 11px;");
        
        // Statut de connexion
        globalStatusLabel = new Label("✅ En ligne");
        globalStatusLabel.getStyleClass().add("status-active");
        globalStatusLabel.setStyle("-fx-font-size: 11px;");
        
        profile.getChildren().addAll(avatar, name, role, company, globalStatusLabel);
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
    
    // Méthodes pour changer le contenu avec scroll automatique en haut
    private void showDashboard() {
        updateContentHeader("Tableau de bord", "Vue d'ensemble de vos événements et performances");
        dashboardContainer = createDashboardView();
        contentArea.setCenter(dashboardContainer);
        scrollToTop();
    }
    
    private void showMyEvents() {
        updateContentHeader("Mes événements", "Gérez vos événements créés et organisés");
        myEventsContainer = createMyEventsView();
        contentArea.setCenter(myEventsContainer);
        scrollToTop();
    }
    
    private void showCreateEvent() {
        updateContentHeader("Créer un événement", "Organisez votre prochain événement");
        contentArea.setCenter(createEventFormView());
        scrollToTop();
    }
    
    private void showParticipants() {
        updateContentHeader("Participants", "Gérez les participants de vos événements");
        participantsContainer = createParticipantsView();
        contentArea.setCenter(participantsContainer);
        scrollToTop();
    }
    
    private void showReports() {
        updateContentHeader("Rapports", "Analysez les performances de vos événements");
        reportsContainer = createReportsView();
        contentArea.setCenter(reportsContainer);
        scrollToTop();
    }
    
    private void scrollToTop() {
        // Remonter automatiquement en haut lors du changement de vue
        javafx.application.Platform.runLater(() -> {
            contentScrollPane.setVvalue(0);
            contentScrollPane.setHvalue(0);
        });
    }
    
    private VBox createDashboardView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Statistiques rapides (données en temps réel)
        HBox statsBar = createOrganizerStatsBar();
        
        // Actions rapides
        HBox quickActions = createQuickActions();
        
        // Événements récents
        VBox recentEvents = createRecentEventsSection();
        
        // Activité récente
        VBox recentActivity = createRecentActivitySection();
        
        view.getChildren().addAll(statsBar, quickActions, recentEvents, recentActivity);
        return view;
    }
    
    private HBox createOrganizerStatsBar() {
        HBox statsBar = new HBox();
        statsBar.getStyleClass().add("stats-bar");
        statsBar.setSpacing(24);
        
        // Calculer les statistiques en temps réel
        int totalEvents = currentOrganizer.getEvenementsOrganises().size();
        long activeEvents = currentOrganizer.getEvenementsOrganises().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .count();
        
        int totalParticipants = currentOrganizer.getEvenementsOrganises().stream()
            .mapToInt(e -> e.getParticipants().size())
            .sum();
        
        int estimatedRevenue = totalParticipants * 50; // Calcul simplifié
        
        VBox totalEventsCard = createStatCard(String.valueOf(totalEvents), "Événements créés", "total-stat");
        VBox activeEventsCard = createStatCard(String.valueOf(activeEvents), "Événements actifs", "positive-stat");
        VBox participantsCard = createStatCard(String.valueOf(totalParticipants), "Participants totaux", "info-stat");
        VBox revenueCard = createStatCard(estimatedRevenue + "€", "Revenus estimés", "success-stat");
        
        statsBar.getChildren().addAll(totalEventsCard, activeEventsCard, participantsCard, revenueCard);
        return statsBar;
    }
    
    private HBox createQuickActions() {
        HBox actions = new HBox();
        actions.setSpacing(16);
        actions.setPadding(new Insets(16, 0, 16, 0));
        
        Button createEventBtn = new Button("➕ Nouvel Événement");
        createEventBtn.getStyleClass().add("primary-button");
        createEventBtn.setOnAction(e -> showCreateEventDialog());
        
        Button viewParticipantsBtn = new Button("👥 Voir Participants");
        viewParticipantsBtn.getStyleClass().add("secondary-button");
        viewParticipantsBtn.setOnAction(e -> showParticipants());
        
        Button exportBtn = new Button("📊 Exporter Données");
        exportBtn.getStyleClass().add("secondary-button");
        exportBtn.setOnAction(e -> exportData());
        
        actions.getChildren().addAll(createEventBtn, viewParticipantsBtn, exportBtn);
        return actions;
    }
    
    private VBox createRecentEventsSection() {
        VBox section = new VBox();
        section.getStyleClass().add("events-list");
        section.setSpacing(16);
        
        Label title = new Label("Mes événements récents");
        title.getStyleClass().add("section-title");
        
        VBox eventsContainer = new VBox();
        eventsContainer.setSpacing(8);
        
        // Récupérer les événements de l'organisateur en temps réel
        List<Evenement> recentEvents = currentOrganizer.getEvenementsOrganises().stream()
            .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate())) // Trier par date décroissante
            .limit(5)
            .collect(Collectors.toList());
        
        if (recentEvents.isEmpty()) {
            Label noEvents = new Label("Aucun événement créé. Commencez par créer votre premier événement !");
            noEvents.getStyleClass().add("text-secondary");
            eventsContainer.getChildren().add(noEvents);
        } else {
            for (Evenement event : recentEvents) {
                HBox eventCard = createEventManagementCard(event);
                eventsContainer.getChildren().add(eventCard);
            }
        }
        
        section.getChildren().addAll(title, eventsContainer);
        return section;
    }
    
    private VBox createRecentActivitySection() {
        VBox section = new VBox();
        section.getStyleClass().add("events-list");
        section.setSpacing(16);
        
        Label title = new Label("Activité récente");
        title.getStyleClass().add("section-title");
        
        VBox activityContainer = new VBox();
        activityContainer.setSpacing(8);
        
        // Générer des activités récentes basées sur les données réelles
        List<String> activities = generateRecentActivities();
        
        if (activities.isEmpty()) {
            Label noActivity = new Label("Aucune activité récente.");
            noActivity.getStyleClass().add("text-secondary");
            activityContainer.getChildren().add(noActivity);
        } else {
            for (String activity : activities) {
                HBox activityCard = createActivityCard(activity);
                activityContainer.getChildren().add(activityCard);
            }
        }
        
        section.getChildren().addAll(title, activityContainer);
        return section;
    }
    
    private List<String> generateRecentActivities() {
        List<String> activities = new java.util.ArrayList<>();
        
        // Générer des activités basées sur les événements réels
        for (Evenement event : currentOrganizer.getEvenementsOrganises()) {
            if (event.getParticipants().size() > 0) {
                activities.add("📝|Nouvelle inscription|" + event.getParticipants().get(event.getParticipants().size() - 1).getNom() + 
                             " s'est inscrit à " + event.getNom() + "|Il y a " + (Math.random() * 24) + "h");
            }
            
            if (event.getDate().isAfter(LocalDateTime.now())) {
                activities.add("📅|Événement à venir|" + event.getNom() + " dans " + 
                             java.time.Duration.between(LocalDateTime.now(), event.getDate()).toDays() + " jours|" +
                             event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }
        
        return activities.stream().limit(5).collect(Collectors.toList());
    }
    
    private VBox createMyEventsView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Barre d'actions
        HBox actionBar = createEventActionBar();
        
        // Filtres
        HBox filters = createEventFilters();
        
        // Liste des événements
        VBox eventsList = new VBox();
        eventsList.setSpacing(12);
        
        // Récupérer tous les événements de l'organisateur
        List<Evenement> myEvents = currentOrganizer.getEvenementsOrganises();
        
        if (myEvents.isEmpty()) {
            VBox emptyState = createEmptyEventsState();
            eventsList.getChildren().add(emptyState);
        } else {
            for (Evenement event : myEvents) {
                HBox eventCard = createDetailedEventCard(event);
                eventsList.getChildren().add(eventCard);
            }
        }
        
        view.getChildren().addAll(actionBar, filters, eventsList);
        return view;
    }
    
    private HBox createEventActionBar() {
        HBox actionBar = new HBox();
        actionBar.setSpacing(16);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        Button createBtn = new Button("➕ Nouvel Événement");
        createBtn.getStyleClass().add("primary-button");
        createBtn.setOnAction(e -> showCreateEventDialog());
        
        Button templateBtn = new Button("📄 Utiliser un modèle");
        templateBtn.getStyleClass().add("secondary-button");
        templateBtn.setOnAction(e -> showTemplates());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher un événement...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterMyEvents(newText));
        
        actionBar.getChildren().addAll(createBtn, templateBtn, spacer, searchField);
        return actionBar;
    }
    
    private HBox createEventFilters() {
        HBox filters = new HBox();
        filters.setSpacing(12);
        
        long totalEvents = currentOrganizer.getEvenementsOrganises().size();
        long activeEvents = currentOrganizer.getEvenementsOrganises().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .count();
        long pastEvents = totalEvents - activeEvents;
        
        Button allBtn = new Button("Tous (" + totalEvents + ")");
        allBtn.getStyleClass().add("primary-button-small");
        
        Button activeBtn = new Button("Actifs (" + activeEvents + ")");
        activeBtn.getStyleClass().add("secondary-button");
        activeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 12px;");
        
        Button pastBtn = new Button("Terminés (" + pastEvents + ")");
        pastBtn.getStyleClass().add("secondary-button");
        pastBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 12px;");
        
        filters.getChildren().addAll(allBtn, activeBtn, pastBtn);
        return filters;
    }
    
    private VBox createEmptyEventsState() {
        VBox emptyState = new VBox();
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setSpacing(16);
        emptyState.setPadding(new Insets(40));
        
        Label emptyIcon = new Label("📅");
        emptyIcon.setStyle("-fx-font-size: 48px;");
        
        Label emptyTitle = new Label("Aucun événement créé");
        emptyTitle.getStyleClass().add("section-title");
        
        Label emptyDescription = new Label("Commencez par créer votre premier événement pour engager votre audience.");
        emptyDescription.getStyleClass().add("text-secondary");
        emptyDescription.setStyle("-fx-text-alignment: center;");
        
        Button createFirstBtn = new Button("Créer mon premier événement");
        createFirstBtn.getStyleClass().add("primary-button");
        createFirstBtn.setOnAction(e -> showCreateEventDialog());
        
        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyDescription, createFirstBtn);
        return emptyState;
    }
    
    private VBox createEventFormView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Formulaire de création d'événement intégré
        VBox form = new VBox();
        form.getStyleClass().add("events-list");
        form.setSpacing(20);
        
        Label formTitle = new Label("Créer un nouvel événement");
        formTitle.getStyleClass().add("section-title");
        
        Label formDescription = new Label("Remplissez les informations ci-dessous pour créer votre événement.");
        formDescription.getStyleClass().add("section-description");
        
        // Champs du formulaire
        VBox formFields = createEventFormFields();
        
        form.getChildren().addAll(formTitle, formDescription, formFields);
        view.getChildren().add(form);
        return view;
    }
    
    private VBox createEventFormFields() {
        VBox formFields = new VBox();
        formFields.setSpacing(16);
        
        // Type d'événement
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Conférence", "Concert");
        typeCombo.setValue("Conférence");
        typeCombo.getStyleClass().add("search-field");
        
        // Nom
        TextField nameField = new TextField();
        nameField.setPromptText("Nom de l'événement");
        nameField.getStyleClass().add("search-field");
        
        // Description
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description de l'événement");
        descriptionArea.getStyleClass().add("search-field");
        descriptionArea.setPrefRowCount(3);
        
        // Date et prix
        HBox datePrice = new HBox();
        datePrice.setSpacing(16);
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        
        TextField timeField = new TextField();
        timeField.setPromptText("Heure (HH:mm)");
        timeField.getStyleClass().add("search-field");
        
        TextField priceField = new TextField();
        priceField.setPromptText("Prix (€) - optionnel");
        priceField.getStyleClass().add("search-field");
        
        datePrice.getChildren().addAll(datePicker, timeField, priceField);
        
        // Lieu et capacité
        HBox locationCapacity = new HBox();
        locationCapacity.setSpacing(16);
        
        TextField locationField = new TextField();
        locationField.setPromptText("Lieu");
        locationField.getStyleClass().add("search-field");
        
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacité maximale");
        capacityField.getStyleClass().add("search-field");
        
        locationCapacity.getChildren().addAll(locationField, capacityField);
        
        // Champs spécifiques selon le type
        TextField themeField = new TextField();
        themeField.setPromptText("Thème (pour conférence)");
        themeField.getStyleClass().add("search-field");
        
        TextField artisteField = new TextField();
        artisteField.setPromptText("Artiste (pour concert)");
        artisteField.getStyleClass().add("search-field");
        artisteField.setVisible(false);
        
        TextField genreField = new TextField();
        genreField.setPromptText("Genre musical (pour concert)");
        genreField.getStyleClass().add("search-field");
        genreField.setVisible(false);
        
        // Gestion de l'affichage des champs selon le type
        typeCombo.setOnAction(e -> {
            boolean isConference = "Conférence".equals(typeCombo.getValue());
            themeField.setVisible(isConference);
            artisteField.setVisible(!isConference);
            genreField.setVisible(!isConference);
        });
        
        // Boutons d'action
        HBox actions = new HBox();
        actions.setSpacing(16);
        actions.setPadding(new Insets(16, 0, 0, 0));
        
        Button saveBtn = new Button("💾 Enregistrer comme brouillon");
        saveBtn.getStyleClass().add("secondary-button");
        saveBtn.setOnAction(e -> saveDraftEvent(typeCombo, nameField, descriptionArea, datePicker, timeField, 
                                               locationField, capacityField, themeField, artisteField, genreField));
        
        Button publishBtn = new Button("🚀 Publier l'événement");
        publishBtn.getStyleClass().add("primary-button");
        publishBtn.setOnAction(e -> publishEvent(typeCombo, nameField, descriptionArea, datePicker, timeField, 
                                                locationField, capacityField, themeField, artisteField, genreField));
        
        Button clearBtn = new Button("🔄 Effacer");
        clearBtn.getStyleClass().add("icon-button-danger");
        clearBtn.setStyle("-fx-padding: 12px 24px;");
        clearBtn.setOnAction(e -> clearForm(nameField, descriptionArea, datePicker, timeField, 
                                           locationField, capacityField, themeField, artisteField, genreField));
        
        actions.getChildren().addAll(saveBtn, publishBtn, clearBtn);
        
        formFields.getChildren().addAll(
            new Label("Type d'événement:"), typeCombo,
            new Label("Nom:"), nameField,
            new Label("Description:"), descriptionArea,
            new Label("Date et heure:"), datePrice,
            new Label("Lieu et capacité:"), locationCapacity,
            new Label("Détails spécifiques:"), themeField, artisteField, genreField,
            actions
        );
        
        return formFields;
    }
    
    private VBox createParticipantsView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Sélecteur d'événement
        HBox eventSelector = createEventSelector();
        
        // Statistiques des participants pour l'événement sélectionné
        HBox participantStats = createParticipantStatsBar();
        
        // Table des participants
        VBox participantsTable = createParticipantsTable();
        
        view.getChildren().addAll(eventSelector, participantStats, participantsTable);
        return view;
    }
    
    private HBox createEventSelector() {
        HBox eventSelector = new HBox();
        eventSelector.setSpacing(16);
        eventSelector.setAlignment(Pos.CENTER_LEFT);
        
        Label eventLabel = new Label("Événement:");
        eventLabel.getStyleClass().add("section-title");
        eventLabel.setStyle("-fx-font-size: 16px;");
        
        ComboBox<Evenement> eventCombo = new ComboBox<>();
        eventCombo.getItems().addAll(currentOrganizer.getEvenementsOrganises());
        if (!currentOrganizer.getEvenementsOrganises().isEmpty()) {
            eventCombo.setValue(currentOrganizer.getEvenementsOrganises().get(0));
        }
        eventCombo.getStyleClass().add("search-field");
        eventCombo.setPrefWidth(300);
        
        // Personnaliser l'affichage des événements
        eventCombo.setConverter(new javafx.util.StringConverter<Evenement>() {
            @Override
            public String toString(Evenement event) {
                return event != null ? event.getNom() : "";
            }
            
            @Override
            public Evenement fromString(String string) {
                return null;
            }
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportBtn = new Button("📊 Exporter liste");
        exportBtn.getStyleClass().add("secondary-button");
        exportBtn.setOnAction(e -> exportParticipantsList(eventCombo.getValue()));
        
        eventSelector.getChildren().addAll(eventLabel, eventCombo, spacer, exportBtn);
        return eventSelector;
    }
    
    private HBox createParticipantStatsBar() {
        HBox participantStats = new HBox();
        participantStats.getStyleClass().add("stats-bar");
        participantStats.setSpacing(24);
        
        // Calculer les stats pour tous les événements de l'organisateur
        int totalConfirmed = currentOrganizer.getEvenementsOrganises().stream()
            .mapToInt(e -> e.getParticipants().size())
            .sum();
        
        double avgAge = 32.5; // Simulation
        int totalRevenue = totalConfirmed * 50; // Simulation
        
        VBox confirmedStat = createStatCard(String.valueOf(totalConfirmed), "Participants confirmés", "positive-stat");
        VBox pendingStat = createStatCard("0", "En attente", "info-stat");
        VBox revenueStat = createStatCard(totalRevenue + "€", "Revenus générés", "success-stat");
        VBox averageAgeStat = createStatCard(String.format("%.1f", avgAge), "Âge moyen", "total-stat");
        
        participantStats.getChildren().addAll(confirmedStat, pendingStat, revenueStat, averageAgeStat);
        return participantStats;
    }
    
    private VBox createParticipantsTable() {
        VBox table = new VBox();
        table.getStyleClass().add("data-table");
        table.setSpacing(0);
        
        // En-tête
        HBox header = createParticipantsTableHeader();
        
        // Lignes de données
        VBox rows = new VBox();
        rows.setSpacing(0);
        
        // Récupérer tous les participants des événements de l'organisateur
        List<Participant> allParticipants = currentOrganizer.getEvenementsOrganises().stream()
            .flatMap(e -> e.getParticipants().stream())
            .distinct()
            .collect(Collectors.toList());
        
        if (allParticipants.isEmpty()) {
            Label noParticipants = new Label("Aucun participant inscrit pour le moment.");
            noParticipants.getStyleClass().add("text-secondary");
            noParticipants.setPadding(new Insets(20));
            rows.getChildren().add(noParticipants);
        } else {
            for (Participant participant : allParticipants) {
                HBox row = createParticipantRow(participant);
                rows.getChildren().add(row);
            }
        }
        
        table.getChildren().addAll(header, rows);
        return table;
    }
    
    private HBox createParticipantsTableHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("table-header");
        header.setSpacing(16);
        header.setPadding(new Insets(16));
        
        Label nameHeader = new Label("Nom");
        nameHeader.getStyleClass().add("table-header-text");
        nameHeader.setPrefWidth(200);
        
        Label emailHeader = new Label("Email");
        emailHeader.getStyleClass().add("table-header-text");
        emailHeader.setPrefWidth(250);
        
        Label eventsHeader = new Label("Événements inscrits");
        eventsHeader.getStyleClass().add("table-header-text");
        eventsHeader.setPrefWidth(150);
        
        Label statusHeader = new Label("Statut");
        statusHeader.getStyleClass().add("table-header-text");
        statusHeader.setPrefWidth(100);
        
        Label actionsHeader = new Label("Actions");
        actionsHeader.getStyleClass().add("table-header-text");
        actionsHeader.setPrefWidth(100);
        
        header.getChildren().addAll(nameHeader, emailHeader, eventsHeader, statusHeader, actionsHeader);
        return header;
    }
    
    private HBox createParticipantRow(Participant participant) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setSpacing(16);
        row.setPadding(new Insets(12, 16, 12, 16));
        
        Label nameLabel = new Label(participant.getNom());
        nameLabel.setPrefWidth(200);
        
        Label emailLabel = new Label(participant.getEmail());
        emailLabel.setPrefWidth(250);
        emailLabel.getStyleClass().add("text-secondary");
        
        // Compter les événements de l'organisateur où ce participant est inscrit
        long eventCount = currentOrganizer.getEvenementsOrganises().stream()
            .filter(e -> e.getParticipants().contains(participant))
            .count();
        
        Label eventsLabel = new Label(String.valueOf(eventCount));
        eventsLabel.setPrefWidth(150);
        
        Label statusLabel = new Label("Confirmé");
        statusLabel.setPrefWidth(100);
        statusLabel.getStyleClass().add("status-active");
        
        HBox actions = new HBox();
        actions.setSpacing(4);
        actions.setPrefWidth(100);
        
        Button contactBtn = new Button("✉️");
        contactBtn.getStyleClass().add("icon-button");
        contactBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4px;");
        contactBtn.setOnAction(e -> contactParticipant(participant));
        
        Button viewBtn = new Button("👁️");
        viewBtn.getStyleClass().add("icon-button");
        viewBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4px;");
        viewBtn.setOnAction(e -> viewParticipantDetails(participant));
        
        actions.getChildren().addAll(contactBtn, viewBtn);
        
        row.getChildren().addAll(nameLabel, emailLabel, eventsLabel, statusLabel, actions);
        return row;
    }
    
    private VBox createReportsView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Résumé global
        HBox globalStats = createGlobalStatsBar();
        
        // Graphiques et analyses
        VBox chartsSection = createChartsSection();
        
        // Rapport détaillé
        VBox detailedReport = createDetailedReport();
        
        view.getChildren().addAll(globalStats, chartsSection, detailedReport);
        return view;
    }
    
    private HBox createGlobalStatsBar() {
        HBox globalStats = new HBox();
        globalStats.getStyleClass().add("stats-bar");
        globalStats.setSpacing(24);
        
        // Calculer les statistiques globales
        int totalEvents = currentOrganizer.getEvenementsOrganises().size();
        int totalParticipants = currentOrganizer.getEvenementsOrganises().stream()
            .mapToInt(e -> e.getParticipants().size())
            .sum();
        double avgParticipants = totalEvents > 0 ? (double) totalParticipants / totalEvents : 0;
        int totalRevenue = totalParticipants * 50;
        
        VBox totalRevenueCard = createStatCard(totalRevenue + "€", "Revenus totaux", "success-stat");
        VBox avgParticipantsCard = createStatCard(String.format("%.1f", avgParticipants), "Participants moyens", "info-stat");
        VBox successRateCard = createStatCard("94%", "Taux de réussite", "positive-stat");
        VBox satisfactionCard = createStatCard("4.7/5", "Satisfaction", "total-stat");
        
        globalStats.getChildren().addAll(totalRevenueCard, avgParticipantsCard, successRateCard, satisfactionCard);
        return globalStats;
    }
    
    private VBox createChartsSection() {
        VBox chartsSection = new VBox();
        chartsSection.getStyleClass().add("events-list");
        chartsSection.setSpacing(16);
        
        Label chartsTitle = new Label("Analyses détaillées");
        chartsTitle.getStyleClass().add("section-title");
        
        // Placeholder pour les graphiques avec données réelles
        VBox chartPlaceholder = new VBox();
        chartPlaceholder.setAlignment(Pos.CENTER);
        chartPlaceholder.setSpacing(16);
        chartPlaceholder.setPadding(new Insets(40));
        chartPlaceholder.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8px;");
        
        Label chartLabel = new Label("📊");
        chartLabel.setStyle("-fx-font-size: 48px;");
        
        Label chartText = new Label("Graphiques d'analyse basés sur vos données");
        chartText.getStyleClass().add("section-description");
        
        StringBuilder chartDesc = new StringBuilder();
        chartDesc.append("Données disponibles :\n");
        chartDesc.append("• ").append(currentOrganizer.getEvenementsOrganises().size()).append(" événements organisés\n");
        chartDesc.append("• Évolution des inscriptions par mois\n");
        chartDesc.append("• Taux de participation par type d'événement\n");
        chartDesc.append("• Analyse de satisfaction clients");
        
        Label chartDescLabel = new Label(chartDesc.toString());
        chartDescLabel.getStyleClass().add("text-secondary");
        
        chartPlaceholder.getChildren().addAll(chartLabel, chartText, chartDescLabel);
        
        chartsSection.getChildren().addAll(chartsTitle, chartPlaceholder);
        return chartsSection;
    }
    
    private VBox createDetailedReport() {
        VBox reportSection = new VBox();
        reportSection.getStyleClass().add("events-list");
        reportSection.setSpacing(16);
        
        Label reportTitle = new Label("Rapport détaillé");
        reportTitle.getStyleClass().add("section-title");
        
        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT DE PERFORMANCE ===\n\n");
        report.append("Organisateur: ").append(currentOrganizer.getNom()).append("\n");
        report.append("Email: ").append(currentOrganizer.getEmail()).append("\n");
        report.append("Entreprise: ").append(currentOrganizer).append("\n\n");
        
        report.append("RÉSUMÉ DES ÉVÉNEMENTS:\n");
        report.append("Total événements organisés: ").append(currentOrganizer.getEvenementsOrganises().size()).append("\n");
        
        long futureEvents = currentOrganizer.getEvenementsOrganises().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .count();
        report.append("Événements à venir: ").append(futureEvents).append("\n");
        report.append("Événements passés: ").append(currentOrganizer.getEvenementsOrganises().size() - futureEvents).append("\n\n");
        
        int totalParticipants = currentOrganizer.getEvenementsOrganises().stream()
            .mapToInt(e -> e.getParticipants().size())
            .sum();
        report.append("PARTICIPANTS:\n");
        report.append("Total participants: ").append(totalParticipants).append("\n");
        
        if (!currentOrganizer.getEvenementsOrganises().isEmpty()) {
            double avgParticipants = (double) totalParticipants / currentOrganizer.getEvenementsOrganises().size();
            report.append("Moyenne par événement: ").append(String.format("%.1f", avgParticipants)).append("\n");
        }
        
        report.append("\nDÉTAIL PAR ÉVÉNEMENT:\n");
        for (Evenement event : currentOrganizer.getEvenementsOrganises()) {
            report.append("• ").append(event.getNom()).append(": ");
            report.append(event.getParticipants().size()).append("/").append(event.getCapaciteMax());
            report.append(" participants (").append(event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append(")\n");
        }
        
        TextArea reportArea = new TextArea(report.toString());
        reportArea.setEditable(false);
        reportArea.setPrefRowCount(15);
        reportArea.getStyleClass().add("search-field");
        
        Button exportReportBtn = new Button("📊 Exporter le rapport");
        exportReportBtn.getStyleClass().add("primary-button");
        exportReportBtn.setOnAction(e -> exportReport(report.toString()));
        
        reportSection.getChildren().addAll(reportTitle, reportArea, exportReportBtn);
        return reportSection;
    }
    
    // Méthodes utilitaires pour créer les composants
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
    
    private HBox createEventManagementCard(Evenement evenement) {
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
        
        Label participantsLabel = new Label(evenement.getParticipants().size() + "/" + 
                                          evenement.getCapaciteMax() + " participants");
        participantsLabel.getStyleClass().add("event-participants");
        
        info.getChildren().addAll(titleLabel, dateLabel, participantsLabel);
        
        VBox statusActions = new VBox();
        statusActions.setAlignment(Pos.CENTER_RIGHT);
        statusActions.setSpacing(8);
        
        String status = evenement.getDate().isAfter(LocalDateTime.now()) ? "Actif" : "Terminé";
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add(status.equals("Actif") ? "status-active" : "status-inactive");
        
        HBox buttons = new HBox();
        buttons.setSpacing(8);
        
        Button editBtn = new Button("✏️");
        editBtn.getStyleClass().add("icon-button");
        editBtn.setOnAction(e -> editEvent(evenement));
        
        Button viewBtn = new Button("👁️");
        viewBtn.getStyleClass().add("icon-button");
        viewBtn.setOnAction(e -> viewEventDetails(evenement));
        
        buttons.getChildren().addAll(editBtn, viewBtn);
        
        statusActions.getChildren().addAll(statusLabel, buttons);
        
        card.getChildren().addAll(info, statusActions);
        return card;
    }
    
    private HBox createActivityCard(String activityData) {
        String[] parts = activityData.split("\\|");
        String icon = parts.length > 0 ? parts[0] : "📝";
        String title = parts.length > 1 ? parts[1] : "Activité";
        String description = parts.length > 2 ? parts[2] : "Description";
        String time = parts.length > 3 ? parts[3] : "Récemment";
        
        HBox card = new HBox();
        card.getStyleClass().add("event-card");
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        VBox info = new VBox();
        info.setSpacing(2);
        VBox.setVgrow(info, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("event-title");
        titleLabel.setStyle("-fx-font-size: 14px;");
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("event-date");
        
        info.getChildren().addAll(titleLabel, descLabel);
        
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("text-secondary");
        timeLabel.setStyle("-fx-font-size: 11px;");
        
        card.getChildren().addAll(iconLabel, info, timeLabel);
        return card;
    }
    
    private HBox createDetailedEventCard(Evenement evenement) {
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
        info.setPrefWidth(350);
        
        String icon = evenement instanceof Conference ? "🎤" : "🎵";
        Label titleLabel = new Label(icon + " " + evenement.getNom());
        titleLabel.getStyleClass().add("event-title");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(evenement.getDate().format(formatter) + " • " + evenement.getLieu());
        dateLabel.getStyleClass().add("event-date");
        
        Label participantsLabel = new Label(evenement.getParticipants().size() + "/" + 
                                          evenement.getCapaciteMax() + " participants inscrits");
        participantsLabel.getStyleClass().add("event-participants");
        
        info.getChildren().addAll(titleLabel, dateLabel, participantsLabel);
        
        VBox statusBox = new VBox();
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setSpacing(4);
        statusBox.setPrefWidth(100);
        
        String status = evenement.getDate().isAfter(LocalDateTime.now()) ? "Actif" : "Terminé";
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add(status.equals("Actif") ? "status-active" : "status-inactive");
        
        statusBox.getChildren().add(statusLabel);
        
        VBox revenueBox = new VBox();
        revenueBox.setAlignment(Pos.CENTER);
        revenueBox.setSpacing(4);
        revenueBox.setPrefWidth(100);
        
        int revenue = evenement.getParticipants().size() * 50;
        Label revenueLabel = new Label(revenue + "€");
        revenueLabel.getStyleClass().add("stat-value");
        revenueLabel.setStyle("-fx-font-size: 16px;");
        
        Label revenueText = new Label("Revenus");
        revenueText.getStyleClass().add("stat-label");
        
        revenueBox.getChildren().addAll(revenueLabel, revenueText);
        
        HBox actions = new HBox();
        actions.setSpacing(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("✏️");
        editBtn.getStyleClass().add("icon-button");
        editBtn.setOnAction(e -> editEvent(evenement));
        
        Button viewBtn = new Button("👁️");
        viewBtn.getStyleClass().add("icon-button");
        viewBtn.setOnAction(e -> viewEventDetails(evenement));
        
        Button participantsBtn = new Button("👥");
        participantsBtn.getStyleClass().add("icon-button");
        participantsBtn.setOnAction(e -> viewEventParticipants(evenement));
        
        if (evenement.getDate().isAfter(LocalDateTime.now())) {
            Button cancelBtn = new Button("🗑️");
            cancelBtn.getStyleClass().add("icon-button-danger");
            cancelBtn.setOnAction(e -> cancelEvent(evenement));
            actions.getChildren().addAll(editBtn, viewBtn, participantsBtn, cancelBtn);
        } else {
            actions.getChildren().addAll(editBtn, viewBtn, participantsBtn);
        }
        
        card.getChildren().addAll(info, statusBox, revenueBox, actions);
        return card;
    }
    
    // Actions et interactions
    private void showCreateEventDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Créer un nouvel événement");
        
        VBox content = createEventDialogContent(dialog, null);
        
        Scene scene = new Scene(content, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private VBox createEventDialogContent(Stage dialog, Evenement existingEvent) {
        VBox content = new VBox();
        content.setSpacing(16);
        content.setPadding(new Insets(20));
        
        // Champs du formulaire
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Conférence", "Concert");
        typeCombo.setValue(existingEvent instanceof Conference ? "Conférence" : "Concert");
        typeCombo.getStyleClass().add("search-field");
        
        TextField nomField = new TextField();
        nomField.setPromptText("Nom de l'événement");
        nomField.getStyleClass().add("search-field");
        if (existingEvent != null) nomField.setText(existingEvent.getNom());
        
        TextField lieuField = new TextField();
        lieuField.setPromptText("Lieu");
        lieuField.getStyleClass().add("search-field");
        if (existingEvent != null) lieuField.setText(existingEvent.getLieu());
        
        TextField capaciteField = new TextField();
        capaciteField.setPromptText("Capacité maximale");
        capaciteField.getStyleClass().add("search-field");
        if (existingEvent != null) capaciteField.setText(String.valueOf(existingEvent.getCapaciteMax()));
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        if (existingEvent != null) datePicker.setValue(existingEvent.getDate().toLocalDate());
        
        TextField heureField = new TextField();
        heureField.setPromptText("Heure (HH:mm)");
        heureField.getStyleClass().add("search-field");
        if (existingEvent != null) heureField.setText(existingEvent.getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        // Champs spécifiques
        TextField themeField = new TextField();
        themeField.setPromptText("Thème (pour conférence)");
        themeField.getStyleClass().add("search-field");
        
        TextField artisteField = new TextField();
        artisteField.setPromptText("Artiste (pour concert)");
        artisteField.getStyleClass().add("search-field");
        
        TextField genreField = new TextField();
        genreField.setPromptText("Genre musical (pour concert)");
        genreField.getStyleClass().add("search-field");
        
        // Remplir les champs spécifiques si édition
        if (existingEvent instanceof Conference) {
            themeField.setText(((Conference) existingEvent).getTheme());
        } else if (existingEvent instanceof Concert) {
            Concert concert = (Concert) existingEvent;
            artisteField.setText(concert.getArtiste());
            genreField.setText(concert.getGenreMusical());
        }
        
        // Gestion de l'affichage des champs
        typeCombo.setOnAction(e -> {
            boolean isConference = "Conférence".equals(typeCombo.getValue());
            themeField.setVisible(isConference);
            artisteField.setVisible(!isConference);
            genreField.setVisible(!isConference);
        });
        
        // Affichage initial
        boolean isConference = "Conférence".equals(typeCombo.getValue());
        themeField.setVisible(isConference);
        artisteField.setVisible(!isConference);
        genreField.setVisible(!isConference);
        
        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnSave = new Button(existingEvent == null ? "Créer" : "Modifier");
        btnSave.getStyleClass().add("primary-button");
        btnSave.setOnAction(e -> {
            try {
                if (existingEvent == null) {
                    createEventFromDialog(typeCombo.getValue(), nomField.getText(), lieuField.getText(),
                                        capaciteField.getText(), datePicker.getValue().toString() + " " + heureField.getText(),
                                        themeField.getText(), artisteField.getText(), genreField.getText());
                } else {
                    updateEventFromDialog(existingEvent, typeCombo.getValue(), nomField.getText(), lieuField.getText(),
                                        capaciteField.getText(), datePicker.getValue().toString() + " " + heureField.getText(),
                                        themeField.getText(), artisteField.getText(), genreField.getText());
                }
                dialog.close();
                refreshCurrentView();
            } catch (Exception ex) {
                showError("Erreur: " + ex.getMessage());
            }
        });
        
        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("secondary-button");
        btnCancel.setOnAction(e -> dialog.close());
        
        buttons.getChildren().addAll(btnCancel, btnSave);
        
        content.getChildren().addAll(
            new Label("Type d'événement:"), typeCombo,
            new Label("Nom:"), nomField,
            new Label("Lieu:"), lieuField,
            new Label("Capacité:"), capaciteField,
            new Label("Date:"), datePicker,
            new Label("Heure:"), heureField,
            new Label("Thème:"), themeField,
            new Label("Artiste:"), artisteField,
            new Label("Genre:"), genreField,
            buttons
        );
        
        return content;
    }
    
     private void createEventFromDialog(String type, String nom, String lieu, String capacite, 
                                     String dateHeure, String theme, String artiste, String genre) {
        try {
            // Validation des champs obligatoires
            if (nom.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de l'événement est obligatoire");
            }
            if (lieu.trim().isEmpty()) {
                throw new IllegalArgumentException("Le lieu est obligatoire");
            }
            if (capacite.trim().isEmpty()) {
                throw new IllegalArgumentException("La capacité est obligatoire");
            }
            
            String id = type.toUpperCase() + "_" + currentUser.getId() + "_" + System.currentTimeMillis();
            int cap = Integer.parseInt(capacite);
            LocalDateTime date = LocalDateTime.parse(dateHeure, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            // Vérifier que la date est dans le futur
            if (date.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("La date de l'événement doit être dans le futur");
            }
            
            Evenement evenement;
            if ("Conférence".equals(type)) {
                if (theme.trim().isEmpty()) {
                    throw new IllegalArgumentException("Le thème est obligatoire pour une conférence");
                }
                evenement = new Conference(id, nom, date, lieu, cap, theme);
            } else {
                if (artiste.trim().isEmpty()) {
                    throw new IllegalArgumentException("L'artiste est obligatoire pour un concert");
                }
                if (genre.trim().isEmpty()) {
                    throw new IllegalArgumentException("Le genre musical est obligatoire pour un concert");
                }
                evenement = new Concert(id, nom, date, lieu, cap, artiste, genre);
            }
            
            // Ajouter l'événement au système avec synchronisation
            dataSynchronizer.ajouterEvenementAvecSync(evenement);
            currentOrganizer.organiserEvenement(evenement);
            
            showSuccessMessage("Événement " + nom + " créé avec succès et sauvegardé");
            System.out.println("✅ Événement créé par " + currentUser.getNom() + ": " + nom);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La capacité doit être un nombre valide");
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date/heure invalide");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création: " + e.getMessage());
        }
    }

    
    private void updateEventFromDialog(Evenement evenement, String type, String nom, String lieu, String capacite, 
                                     String dateHeure, String theme, String artiste, String genre) {
        try {
            // Validation des champs obligatoires
            if (nom.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de l'événement est obligatoire");
            }
            
            int cap = Integer.parseInt(capacite);
            LocalDateTime date = LocalDateTime.parse(dateHeure, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            // Vérifier la capacité par rapport aux participants actuels
            if (cap < evenement.getParticipants().size()) {
                throw new CapaciteMaxAtteinteException("La nouvelle capacité (" + cap + 
                    ") est inférieure au nombre de participants actuels (" + evenement.getParticipants().size() + ")");
            }
            
            // Mettre à jour les propriétés de base
            evenement.setNom(nom);
            evenement.setLieu(lieu);
            evenement.setCapaciteMax(cap);
            evenement.setDate(date);
            
            // Mettre à jour les propriétés spécifiques
            if (evenement instanceof Conference && "Conférence".equals(type)) {
                if (theme.trim().isEmpty()) {
                    throw new IllegalArgumentException("Le thème est obligatoire pour une conférence");
                }
                ((Conference) evenement).setTheme(theme);
            } else if (evenement instanceof Concert && "Concert".equals(type)) {
                if (artiste.trim().isEmpty()) {
                    throw new IllegalArgumentException("L'artiste est obligatoire pour un concert");
                }
                if (genre.trim().isEmpty()) {
                    throw new IllegalArgumentException("Le genre musical est obligatoire pour un concert");
                }
                Concert concert = (Concert) evenement;
                concert.setArtiste(artiste);
                concert.setGenreMusical(genre);
            }
            
            // Synchroniser les modifications
            dataSynchronizer.mettreAJourEvenementAvecSync(evenement);
            
            showSuccessMessage("Événement " + nom + " modifié avec succès");
            System.out.println("✅ Événement modifié par " + currentUser.getNom() + ": " + nom);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La capacité doit être un nombre valide");
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date/heure invalide");
        } catch (CapaciteMaxAtteinteException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la modification: " + e.getMessage());
        }
    }

    
    private void saveDraftEvent(ComboBox<String> typeCombo, TextField nameField, TextArea descriptionArea, 
                               DatePicker datePicker, TextField timeField, TextField locationField, 
                               TextField capacityField, TextField themeField, TextField artisteField, TextField genreField) {
        showSuccessMessage("Brouillon sauvegardé (fonctionnalité à implémenter)");
    }
    
    private void publishEvent(ComboBox<String> typeCombo, TextField nameField, TextArea descriptionArea, 
                             DatePicker datePicker, TextField timeField, TextField locationField, 
                             TextField capacityField, TextField themeField, TextField artisteField, TextField genreField) {
        try {
            if (nameField.getText().trim().isEmpty() || locationField.getText().trim().isEmpty() ||
                capacityField.getText().trim().isEmpty() || datePicker.getValue() == null || timeField.getText().trim().isEmpty()) {
                showError("Veuillez remplir tous les champs obligatoires");
                return;
            }
            
            createEventFromDialog(typeCombo.getValue(), nameField.getText(), locationField.getText(),
                                capacityField.getText(), datePicker.getValue().toString() + " " + timeField.getText(),
                                themeField.getText(), artisteField.getText(), genreField.getText());
            
            // Effacer le formulaire après création
            clearForm(nameField, descriptionArea, datePicker, timeField, locationField, capacityField, 
                     themeField, artisteField, genreField);
                     
        } catch (Exception e) {
            showError("Erreur lors de la publication: " + e.getMessage());
        }
    }
    
    private void clearForm(TextField nameField, TextArea descriptionArea, DatePicker datePicker, 
                          TextField timeField, TextField locationField, TextField capacityField, 
                          TextField themeField, TextField artisteField, TextField genreField) {
        nameField.clear();
        descriptionArea.clear();
        datePicker.setValue(null);
        timeField.clear();
        locationField.clear();
        capacityField.clear();
        themeField.clear();
        artisteField.clear();
        genreField.clear();
    }
    
    private void editEvent(Evenement evenement) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modifier l'événement");
        
        VBox content = createEventDialogContent(dialog, evenement);
        
        Scene scene = new Scene(content, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    

private void viewEventDetails(Evenement evenement) {
    // Créer un contenu riche pour le dialog personnalisé
    VBox content = new VBox();
    content.setSpacing(16);
    content.setPadding(new Insets(20));
    content.setMaxWidth(500);
    
    // Informations principales avec style moderne
    VBox mainInfo = new VBox();
    mainInfo.getStyleClass().add("form-section");
    mainInfo.setSpacing(12);
    
    Label mainTitle = new Label("Informations générales");
    mainTitle.getStyleClass().add("form-section-title");
    
    // Grille d'informations stylisée
    GridPane infoGrid = new GridPane();
    infoGrid.setHgap(20);
    infoGrid.setVgap(8);
    infoGrid.getStyleClass().add("info-grid");
    
    int row = 0;
    addInfoRow(infoGrid, "ID:", evenement.getId(), row++);
    addInfoRow(infoGrid, "Date:", evenement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), row++);
    addInfoRow(infoGrid, "Lieu:", evenement.getLieu(), row++);
    addInfoRow(infoGrid, "Capacité:", evenement.getCapaciteMax() + " places", row++);
    addInfoRow(infoGrid, "Inscrits:", evenement.getParticipants().size() + " participants", row++);
    addInfoRow(infoGrid, "Places restantes:", (evenement.getCapaciteMax() - evenement.getParticipants().size()) + "", row++);
    
    mainInfo.getChildren().addAll(mainTitle, infoGrid);
    
    // Informations spécifiques selon le type
    VBox specificInfo = new VBox();
    specificInfo.getStyleClass().add("form-section");
    specificInfo.setSpacing(12);
    
    Label specificTitle = new Label("Détails spécifiques");
    specificTitle.getStyleClass().add("form-section-title");
    
    VBox specificDetails = new VBox();
    specificDetails.setSpacing(8);
    
    if (evenement instanceof Conference) {
        Conference conf = (Conference) evenement;
        Label typeLabel = new Label("🎤 Type: Conférence");
        typeLabel.getStyleClass().add("event-type-label");
        
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
        typeLabel.getStyleClass().add("event-type-label");
        
        Label artisteLabel = new Label("Artiste: " + concert.getArtiste());
        artisteLabel.getStyleClass().add("info-text");
        
        Label genreLabel = new Label("Genre: " + concert.getGenreMusical());
        genreLabel.getStyleClass().add("info-text");
        
        specificDetails.getChildren().addAll(typeLabel, artisteLabel, genreLabel);
    }
    
    specificInfo.getChildren().addAll(specificTitle, specificDetails);
    
    // Liste des participants avec scroll
    VBox participantsSection = new VBox();
    participantsSection.getStyleClass().add("form-section");
    participantsSection.setSpacing(12);
    
    Label participantsTitle = new Label("Participants inscrits (" + evenement.getParticipants().size() + ")");
    participantsTitle.getStyleClass().add("form-section-title");
    
    if (evenement.getParticipants().isEmpty()) {
        Label noParticipants = new Label("Aucun participant inscrit");
        noParticipants.getStyleClass().add("empty-state-description");
        participantsSection.getChildren().addAll(participantsTitle, noParticipants);
    } else {
        ListView<Participant> participantsList = new ListView<>();
        participantsList.getItems().addAll(evenement.getParticipants());
        participantsList.setPrefHeight(150);
        participantsList.getStyleClass().add("modern-list-view");
        
        participantsList.setCellFactory(lv -> new ListCell<Participant>() {
            @Override
            protected void updateItem(Participant participant, boolean empty) {
                super.updateItem(participant, empty);
                if (empty || participant == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("👤 " + participant.getNom() + " (" + participant.getEmail() + ")");
                    getStyleClass().add("participant-list-item");
                }
            }
        });
        
        participantsSection.getChildren().addAll(participantsTitle, participantsList);
    }
    
    // Boutons d'action
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    actions.setPadding(new Insets(16, 0, 0, 0));
    
    Button exportBtn = new Button("📊 Exporter");
    exportBtn.getStyleClass().add("secondary-button");
    exportBtn.setOnAction(e -> exportParticipantsList(evenement));
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("primary-button");
    
    actions.getChildren().addAll(exportBtn, closeBtn);
    
    content.getChildren().addAll(mainInfo, specificInfo, participantsSection, actions);
    
    // Créer et afficher le dialog moderne
    Stage detailsDialog = ModernNotificationUtils.createCustomDialog(
        "Détails de l'événement: " + evenement.getNom(), 
        content, 
        true
    );
    
    closeBtn.setOnAction(e -> detailsDialog.close());
    detailsDialog.show();
}

private void addInfoRow(GridPane grid, String label, String value, int row) {
    Label labelNode = new Label(label);
    labelNode.getStyleClass().addAll("form-label", "info-label");
    labelNode.setStyle("-fx-font-weight: 600; -fx-min-width: 120px;");
    
    Label valueNode = new Label(value);
    valueNode.getStyleClass().add("info-value");
    valueNode.setWrapText(true);
    
    grid.add(labelNode, 0, row);
    grid.add(valueNode, 1, row);
}

private void viewEventParticipants(Evenement evenement) {
    if (evenement.getParticipants().isEmpty()) {
        ModernNotificationUtils.showInfo(
            "Aucun participant",
            "Aucun participant n'est inscrit à cet événement pour le moment."
        );
        return;
    }
    
    // Créer un contenu riche pour la liste des participants
    VBox content = new VBox();
    content.setSpacing(20);
    content.setPadding(new Insets(20));
    content.setMaxWidth(600);
    
    // Statistiques en haut
    HBox statsBar = new HBox();
    statsBar.setSpacing(16);
    statsBar.setAlignment(Pos.CENTER);
    
    VBox totalCard = createQuickStatCard(
        String.valueOf(evenement.getParticipants().size()),
        "Participants",
        "#2196f3"
    );
    
    double fillRate = (double) evenement.getParticipants().size() / evenement.getCapaciteMax() * 100;
    VBox fillRateCard = createQuickStatCard(
        String.format("%.1f%%", fillRate),
        "Taux de remplissage",
        fillRate > 80 ? "#4caf50" : fillRate > 50 ? "#ff9800" : "#f44336"
    );
    
    VBox remainingCard = createQuickStatCard(
        String.valueOf(evenement.getCapaciteMax() - evenement.getParticipants().size()),
        "Places restantes",
        "#9c27b0"
    );
    
    statsBar.getChildren().addAll(totalCard, fillRateCard, remainingCard);
    
    // Liste des participants avec numérotation
    VBox participantsList = new VBox();
    participantsList.getStyleClass().add("form-section");
    participantsList.setSpacing(12);
    
    Label listTitle = new Label("Liste des participants");
    listTitle.getStyleClass().add("form-section-title");
    
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.getStyleClass().add("content-scroll");
    scrollPane.setPrefHeight(300);
    scrollPane.setFitToWidth(true);
    
    VBox participantsContainer = new VBox();
    participantsContainer.setSpacing(8);
    
    int index = 1;
    for (Participant p : evenement.getParticipants()) {
        HBox participantCard = new HBox();
        participantCard.getStyleClass().add("participant-card");
        participantCard.setSpacing(12);
        participantCard.setAlignment(Pos.CENTER_LEFT);
        participantCard.setPadding(new Insets(12));
        
        Label numberLabel = new Label(String.valueOf(index++));
        numberLabel.getStyleClass().add("participant-number");
        numberLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; " +
                           "-fx-background-radius: 50%; -fx-min-width: 30px; -fx-min-height: 30px; " +
                           "-fx-alignment: center; -fx-font-weight: 600;");
        
        VBox participantInfo = new VBox();
        participantInfo.setSpacing(4);
        
        Label nameLabel = new Label(p.getNom());
        nameLabel.getStyleClass().add("participant-name");
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        
        Label emailLabel = new Label(p.getEmail());
        emailLabel.getStyleClass().add("participant-email");
        emailLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        participantInfo.getChildren().addAll(nameLabel, emailLabel);
        
        Button contactBtn = new Button("✉️");
        contactBtn.getStyleClass().add("icon-button");
        contactBtn.setOnAction(e -> contactParticipant(p));
        ModernNotificationUtils.addModernTooltip(contactBtn, "Contacter " + p.getNom());
        
        participantCard.getChildren().addAll(numberLabel, participantInfo, contactBtn);
        HBox.setHgrow(participantInfo, Priority.ALWAYS);
        
        participantsContainer.getChildren().add(participantCard);
    }
    
    scrollPane.setContent(participantsContainer);
    participantsList.getChildren().addAll(listTitle, scrollPane);
    
    // Actions
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button exportBtn = new Button("📊 Exporter liste");
    exportBtn.getStyleClass().add("secondary-button");
    exportBtn.setOnAction(e -> exportParticipantsList(evenement));
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("primary-button");
    
    actions.getChildren().addAll(exportBtn, closeBtn);
    
    content.getChildren().addAll(statsBar, participantsList, actions);
    
    // Créer et afficher le dialog
    Stage participantsDialog = ModernNotificationUtils.createCustomDialog(
        evenement.getNom() + " - " + evenement.getParticipants().size() + " participant(s)",
        content,
        true
    );
    
    closeBtn.setOnAction(e -> participantsDialog.close());
    participantsDialog.show();
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

private void cancelEvent(Evenement evenement) {
    boolean confirmed = ModernNotificationUtils.showConfirmation(
        "Confirmer l'annulation",
        "Êtes-vous sûr de vouloir annuler \"" + evenement.getNom() + "\" ?\n\n" +
        "⚠️ Cette action notifiera tous les participants inscrits et supprimera l'événement.\n" +
        "Cette action est irréversible."
    );
    
    if (confirmed) {
        // Annuler l'événement avec notification de progression
        Stage progressDialog = ModernNotificationUtils.createProgressDialog(
            "Annulation en cours",
            "Notification des participants et suppression de l'événement..."
        );
        progressDialog.show();
        
        // Simulation d'une tâche asynchrone
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulation du traitement
                
                Platform.runLater(() -> {
                    progressDialog.close();
                    
                    // Effectuer l'annulation
                    evenement.annuler();
                    currentOrganizer.getEvenementsOrganises().remove(evenement);
                    dataSynchronizer.supprimerEvenementAvecSync(evenement.getId());
                    
                    // Notification avec possibilité d'annulation
                    ModernNotificationUtils.showSnackbar(
                        "Événement \"" + evenement.getNom() + "\" annulé avec succès",
                        "RESTAURER",
                        () -> {
                            // Action de restauration (simulation)
                            currentOrganizer.getEvenementsOrganises().add(evenement);
                            ModernNotificationUtils.showSuccessToast("Événement restauré");
                            refreshCurrentView();
                        }
                    );
                    
                    refreshCurrentView();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    progressDialog.close();
                    ModernNotificationUtils.showErrorToast("Annulation interrompue");
                });
            }
        }).start();
    }
}

private void contactParticipant(Participant participant) {
    // Créer un dialog de contact moderne
    VBox content = new VBox();
    content.setSpacing(16);
    content.setPadding(new Insets(20));
    content.setMaxWidth(400);
    
    // Informations du participant
    HBox participantInfo = new HBox();
    participantInfo.getStyleClass().add("form-section");
    participantInfo.setSpacing(12);
    participantInfo.setAlignment(Pos.CENTER_LEFT);
    participantInfo.setPadding(new Insets(16));
    
    Label avatar = new Label("👤");
    avatar.setStyle("-fx-font-size: 32px;");
    
    VBox info = new VBox();
    info.setSpacing(4);
    
    Label nameLabel = new Label(participant.getNom());
    nameLabel.getStyleClass().add("organizer-name");
    
    Label emailLabel = new Label(participant.getEmail());
    emailLabel.getStyleClass().add("organizer-role");
    
    info.getChildren().addAll(nameLabel, emailLabel);
    participantInfo.getChildren().addAll(avatar, info);
    
    // Options de contact
    VBox contactOptions = new VBox();
    contactOptions.setSpacing(12);
    
    Label optionsTitle = new Label("Options de contact");
    optionsTitle.getStyleClass().add("form-section-title");
    
    Button emailBtn = new Button("📧 Envoyer un email");
    emailBtn.getStyleClass().add("primary-button");
    emailBtn.setOnAction(e -> {
        ModernNotificationUtils.showInfoToast("Ouverture du client email...");
        // Ici vous pourriez ouvrir le client email par défaut
        // Desktop.getDesktop().mail(URI.create("mailto:" + participant.getEmail()));
    });
    
    Button messageBtn = new Button("💬 Message rapide");
    messageBtn.getStyleClass().add("secondary-button");
    messageBtn.setOnAction(e -> showQuickMessageDialog(participant));
    
    contactOptions.getChildren().addAll(optionsTitle, emailBtn, messageBtn);
    
    // Note informative
    Label note = new Label("💡 Fonctionnalité de messagerie intégrée à implémenter.\n" +
                          "Pour l'instant, utilisez l'email direct.");
    note.getStyleClass().add("form-help-text");
    note.setWrapText(true);
    note.setStyle("-fx-background-color: #fff3cd; -fx-padding: 12px; " +
                  "-fx-border-color: #ffeaa7; -fx-border-radius: 4px; " +
                  "-fx-background-radius: 4px;");
    
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("secondary-button");
    
    actions.getChildren().add(closeBtn);
    
    content.getChildren().addAll(participantInfo, contactOptions, note, actions);
    
    Stage contactDialog = ModernNotificationUtils.createCustomDialog(
        "Contacter " + participant.getNom(),
        content,
        true
    );
    
    closeBtn.setOnAction(e -> contactDialog.close());
    contactDialog.show();
}

private void showQuickMessageDialog(Participant participant) {
    // Dialog pour message rapide (simulation)
    VBox messageContent = new VBox();
    messageContent.setSpacing(16);
    messageContent.setPadding(new Insets(20));
    
    Label title = new Label("Message rapide à " + participant.getNom());
    title.getStyleClass().add("form-section-title");
    
    TextArea messageArea = new TextArea();
    messageArea.setPromptText("Tapez votre message ici...");
    messageArea.setPrefRowCount(4);
    messageArea.getStyleClass().add("search-field");
    
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button sendBtn = new Button("📤 Envoyer");
    sendBtn.getStyleClass().add("primary-button");
    sendBtn.setOnAction(e -> {
        if (!messageArea.getText().trim().isEmpty()) {
            ModernNotificationUtils.showSuccessToast("Message envoyé à " + participant.getNom());
        } else {
            ModernNotificationUtils.showWarningToast("Veuillez saisir un message");
        }
    });
    
    Button cancelBtn = new Button("Annuler");
    cancelBtn.getStyleClass().add("secondary-button");
    
    actions.getChildren().addAll(cancelBtn, sendBtn);
    messageContent.getChildren().addAll(title, messageArea, actions);
    
    Stage messageDialog = ModernNotificationUtils.createCustomDialog(
        "Nouveau message",
        messageContent,
        true
    );
    
    cancelBtn.setOnAction(e -> messageDialog.close());
    sendBtn.setOnAction(e -> {
        if (!messageArea.getText().trim().isEmpty()) {
            messageDialog.close();
            ModernNotificationUtils.showSuccessToast("Message envoyé à " + participant.getNom());
        } else {
            ModernNotificationUtils.showWarningToast("Veuillez saisir un message");
        }
    });
    
    messageDialog.show();
}

private void viewParticipantDetails(Participant participant) {
    // Compter les événements de cet organisateur où le participant est inscrit
    List<Evenement> participantEvents = currentOrganizer.getEvenementsOrganises().stream()
        .filter(e -> e.getParticipants().contains(participant))
        .collect(Collectors.toList());
    
    // Créer un contenu détaillé
    VBox content = new VBox();
    content.setSpacing(20);
    content.setPadding(new Insets(20));
    content.setMaxWidth(500);
    
    // Profil du participant
    HBox profileSection = new HBox();
    profileSection.getStyleClass().add("form-section");
    profileSection.setSpacing(16);
    profileSection.setAlignment(Pos.CENTER_LEFT);
    profileSection.setPadding(new Insets(20));
    
    Label avatar = new Label("👤");
    avatar.setStyle("-fx-font-size: 48px;");
    
    VBox profileInfo = new VBox();
    profileInfo.setSpacing(8);
    
    Label nameLabel = new Label(participant.getNom());
    nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600;");
    
    Label emailLabel = new Label(participant.getEmail());
    emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
    
    Label idLabel = new Label("ID: " + participant.getId());
    idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
    
    profileInfo.getChildren().addAll(nameLabel, emailLabel, idLabel);
    
    Button contactBtn = new Button("✉️ Contacter");
    contactBtn.getStyleClass().add("primary-button");
    contactBtn.setOnAction(e -> contactParticipant(participant));
    
    profileSection.getChildren().addAll(avatar, profileInfo, contactBtn);
    HBox.setHgrow(profileInfo, Priority.ALWAYS);
    
    // Statistiques
    HBox statsSection = new HBox();
    statsSection.setSpacing(16);
    statsSection.setAlignment(Pos.CENTER);
    
    VBox eventsCard = createQuickStatCard(
        String.valueOf(participantEvents.size()),
        "Événements",
        "#2196f3"
    );
    
    // Calculer la date du premier événement
    String firstEventDate = participantEvents.stream()
        .map(e -> e.getDate())
        .min(LocalDateTime::compareTo)
        .map(date -> date.format(DateTimeFormatter.ofPattern("MM/yy")))
        .orElse("N/A");
    
    VBox memberCard = createQuickStatCard(
        firstEventDate,
        "Membre depuis",
        "#4caf50"
    );
    
    long upcomingEvents = participantEvents.stream()
        .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
        .count();
    
    VBox upcomingCard = createQuickStatCard(
        String.valueOf(upcomingEvents),
        "À venir",
        "#ff9800"
    );
    
    statsSection.getChildren().addAll(eventsCard, memberCard, upcomingCard);
    
    // Liste des événements
    VBox eventsSection = new VBox();
    eventsSection.getStyleClass().add("form-section");
    eventsSection.setSpacing(12);
    
    Label eventsTitle = new Label("Événements inscrits chez vous");
    eventsTitle.getStyleClass().add("form-section-title");
    
    if (participantEvents.isEmpty()) {
        Label noEvents = new Label("Ce participant n'est inscrit à aucun de vos événements");
        noEvents.getStyleClass().add("empty-state-description");
        eventsSection.getChildren().addAll(eventsTitle, noEvents);
    } else {
        VBox eventsList = new VBox();
        eventsList.setSpacing(8);
        
        for (Evenement event : participantEvents) {
            HBox eventCard = new HBox();
            eventCard.getStyleClass().add("event-card");
            eventCard.setSpacing(12);
            eventCard.setAlignment(Pos.CENTER_LEFT);
            eventCard.setPadding(new Insets(12));
            
            String icon = event instanceof Conference ? "🎤" : "🎵";
            Label eventIcon = new Label(icon);
            eventIcon.setStyle("-fx-font-size: 20px;");
            
            VBox eventInfo = new VBox();
            eventInfo.setSpacing(4);
            
            Label eventName = new Label(event.getNom());
            eventName.setStyle("-fx-font-weight: 600;");
            
            Label eventDate = new Label(event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            eventDate.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            
            eventInfo.getChildren().addAll(eventName, eventDate);
            
            Label statusLabel = new Label(event.getDate().isAfter(LocalDateTime.now()) ? "À venir" : "Terminé");
            statusLabel.getStyleClass().add(event.getDate().isAfter(LocalDateTime.now()) ? "status-active" : "status-inactive");
            
            eventCard.getChildren().addAll(eventIcon, eventInfo, statusLabel);
            HBox.setHgrow(eventInfo, Priority.ALWAYS);
            
            eventsList.getChildren().add(eventCard);
        }
        
        ScrollPane eventsScroll = new ScrollPane(eventsList);
        eventsScroll.getStyleClass().add("content-scroll");
        eventsScroll.setPrefHeight(200);
        eventsScroll.setFitToWidth(true);
        
        eventsSection.getChildren().addAll(eventsTitle, eventsScroll);
    }
    
    // Actions
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("primary-button");
    
    actions.getChildren().add(closeBtn);
    
    content.getChildren().addAll(profileSection, statsSection, eventsSection, actions);
    
    Stage detailsDialog = ModernNotificationUtils.createCustomDialog(
        "Profil de " + participant.getNom(),
        content,
        true
    );
    
    closeBtn.setOnAction(e -> detailsDialog.close());
    detailsDialog.show();
}

private void filterMyEvents(String searchText) {
    showMyEvents();
    if (!searchText.trim().isEmpty()) {
        ModernNotificationUtils.showInfoToast("Recherche appliquée: " + searchText);
    }
}

private void showTemplates() {
    // Créer un dialog moderne pour les modèles
    VBox content = new VBox();
    content.setSpacing(20);
    content.setPadding(new Insets(20));
    content.setMaxWidth(500);
    
    Label title = new Label("Modèles d'événements prédéfinis");
    title.getStyleClass().add("form-section-title");
    
    VBox templatesList = new VBox();
    templatesList.setSpacing(12);
    
    String[] templates = {
        "🏢|Conférence d'entreprise|Modèle standard pour conférences professionnelles",
        "🎓|Atelier de formation|Sessions de formation et workshops",
        "🎵|Concert en plein air|Événements musicaux extérieurs",
        "💼|Séminaire professionnel|Séminaires et présentations business",
        "🤝|Événement networking|Rencontres professionnelles et networking"
    };
    
    for (String template : templates) {
        String[] parts = template.split("\\|");
        String icon = parts[0];
        String name = parts[1];
        String description = parts[2];
        
        HBox templateCard = new HBox();
        templateCard.getStyleClass().add("event-card");
        templateCard.setSpacing(16);
        templateCard.setAlignment(Pos.CENTER_LEFT);
        templateCard.setPadding(new Insets(16));
        
        Label templateIcon = new Label(icon);
        templateIcon.setStyle("-fx-font-size: 24px;");
        
        VBox templateInfo = new VBox();
        templateInfo.setSpacing(4);
        
        Label templateName = new Label(name);
        templateName.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        
        Label templateDesc = new Label(description);
        templateDesc.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        templateDesc.setWrapText(true);
        
        templateInfo.getChildren().addAll(templateName, templateDesc);
        
        Button useBtn = new Button("Utiliser");
        useBtn.getStyleClass().add("primary-button-small");
        useBtn.setOnAction(e -> {
            ModernNotificationUtils.showInfoToast("Modèle \"" + name + "\" sélectionné");
            // Ici vous pourriez implémenter la logique pour utiliser le modèle
        });
        
        templateCard.getChildren().addAll(templateIcon, templateInfo, useBtn);
        HBox.setHgrow(templateInfo, Priority.ALWAYS);
        
        templatesList.getChildren().add(templateCard);
    }
    
    ScrollPane templatesScroll = new ScrollPane(templatesList);
    templatesScroll.getStyleClass().add("content-scroll");
    templatesScroll.setPrefHeight(300);
    templatesScroll.setFitToWidth(true);
    
    Label note = new Label("💡 Fonctionnalité à implémenter - Ces modèles pourront être utilisés pour créer rapidement de nouveaux événements");
    note.getStyleClass().add("form-help-text");
    note.setWrapText(true);
    note.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 12px; " +
                  "-fx-border-color: #bbdefb; -fx-border-radius: 4px; " +
                  "-fx-background-radius: 4px;");
    
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("primary-button");
    
    actions.getChildren().add(closeBtn);
    
    content.getChildren().addAll(title, templatesScroll, note, actions);
    
    Stage templatesDialog = ModernNotificationUtils.createCustomDialog(
        "Modèles d'événements",
        content,
        true
    );
    
    closeBtn.setOnAction(e -> templatesDialog.close());
    templatesDialog.show();
}

private void exportParticipantsList(Evenement evenement) {
    if (evenement == null) {
        ModernNotificationUtils.showWarningToast("Veuillez sélectionner un événement");
        return;
    }
    
    // Simulation d'export avec progression
    Stage progressDialog = ModernNotificationUtils.createProgressDialog(
        "Export en cours",
        "Génération de la liste des participants..."
    );
    progressDialog.show();
    
    new Thread(() -> {
        try {
            Thread.sleep(1500); // Simulation
            
            Platform.runLater(() -> {
                progressDialog.close();
                
                StringBuilder export = new StringBuilder();
                export.append("Liste des participants - ").append(evenement.getNom()).append("\n");
                export.append("Date: ").append(evenement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
                export.append("Lieu: ").append(evenement.getLieu()).append("\n\n");
                export.append("Participants (").append(evenement.getParticipants().size()).append("):\n\n");
                
                for (Participant p : evenement.getParticipants()) {
                    export.append(p.getNom()).append(",").append(p.getEmail()).append("\n");
                }
                
                // Notification avec action pour voir le contenu
                ModernNotificationUtils.showSnackbar(
                    "Liste exportée avec succès (" + evenement.getParticipants().size() + " participants)",
                    "VOIR",
                    () -> showExportPreview("Liste des participants", export.toString())
                );
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> {
                progressDialog.close();
                ModernNotificationUtils.showErrorToast("Export interrompu");
            });
        }
    }).start();
}

private void showExportPreview(String title, String content) {
    VBox previewContent = new VBox();
    previewContent.setSpacing(16);
    previewContent.setPadding(new Insets(20));
    previewContent.setMaxWidth(600);
    
    Label previewTitle = new Label("Aperçu de l'export");
    previewTitle.getStyleClass().add("form-section-title");
    
    TextArea previewArea = new TextArea(content);
    previewArea.setEditable(false);
    previewArea.setPrefRowCount(15);
    previewArea.getStyleClass().add("search-field");
    
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button saveBtn = new Button("💾 Sauvegarder");
    saveBtn.getStyleClass().add("primary-button");
    saveBtn.setOnAction(e -> ModernNotificationUtils.showInfoToast("Fichier sauvegardé"));
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("secondary-button");
    
    actions.getChildren().addAll(saveBtn, closeBtn);
    previewContent.getChildren().addAll(previewTitle, previewArea, actions);
    
    Stage previewDialog = ModernNotificationUtils.createCustomDialog(title, previewContent, true);
    closeBtn.setOnAction(e -> previewDialog.close());
    previewDialog.show();
}

private void exportData() {
    ModernNotificationUtils.showInfoToast("Export des données en cours...");
    
    // Simulation d'une tâche d'export
    new Thread(() -> {
        try {
            Thread.sleep(2000);
            Platform.runLater(() -> {
                ModernNotificationUtils.showSnackbar(
                    "Export terminé avec succès",
                    "OUVRIR",
                    () -> ModernNotificationUtils.showInfoToast("Ouverture du fichier...")
                );
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }).start();
}

private void exportReport(String reportContent) {
    Stage progressDialog = ModernNotificationUtils.createProgressDialog(
        "Génération du rapport",
        "Compilation des données et création du fichier..."
    );
    progressDialog.show();
    
    new Thread(() -> {
        try {
            Thread.sleep(2000);
            Platform.runLater(() -> {
                progressDialog.close();
                ModernNotificationUtils.showSnackbar(
                    "Rapport généré (" + reportContent.length() + " caractères)",
                    "APERÇU",
                    () -> showExportPreview("Rapport exporté", reportContent)
                );
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> {
                progressDialog.close();
                ModernNotificationUtils.showErrorToast("Génération interrompue");
            });
        }
    }).start();
}

private void showSettings() {
    // Créer un dialog de paramètres moderne avec onglets
    VBox content = new VBox();
    content.setSpacing(20);
    content.setPadding(new Insets(20));
    content.setMaxWidth(500);
    
    // Profil utilisateur
    VBox profileSection = new VBox();
    profileSection.getStyleClass().add("form-section");
    profileSection.setSpacing(12);
    
    Label profileTitle = new Label("👤 Informations du compte");
    profileTitle.getStyleClass().add("form-section-title");
    
    GridPane profileGrid = new GridPane();
    profileGrid.setHgap(16);
    profileGrid.setVgap(8);
    
    addInfoRow(profileGrid, "Nom:", currentUser.getNom(), 0);
    addInfoRow(profileGrid, "Email:", currentUser.getEmail(), 1);
    addInfoRow(profileGrid, "Entreprise:", currentUser.getOrganisation(), 2);
    //addInfoRow(profileGrid, "Rôle:", currentUser.getRole(), 3);
    
    profileSection.getChildren().addAll(profileTitle, profileGrid);
    
    // Paramètres système
    VBox systemSection = new VBox();
    systemSection.getStyleClass().add("form-section");
    systemSection.setSpacing(12);
    
    Label systemTitle = new Label("⚙️ Configuration système");
    systemTitle.getStyleClass().add("form-section-title");
    
    VBox systemItems = new VBox();
    systemItems.setSpacing(8);
    
    String[] systemSettings = {
        "✅ Notifications: Activées",
        "✅ Synchronisation: Temps réel",
        "✅ Interface responsive: Activée",
        "✅ Theme: Material Design",
        "✅ Sauvegarde automatique: Activée"
    };
    
    for (String setting : systemSettings) {
        Label settingLabel = new Label(setting);
        settingLabel.getStyleClass().add("info-text");
        systemItems.getChildren().add(settingLabel);
    }
    
    systemSection.getChildren().addAll(systemTitle, systemItems);
    
    // Actions
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button editBtn = new Button("✏️ Modifier profil");
    editBtn.getStyleClass().add("secondary-button");
    editBtn.setOnAction(e -> ModernNotificationUtils.showInfoToast("Édition du profil à implémenter"));
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("primary-button");
    
    actions.getChildren().addAll(editBtn, closeBtn);
    
    content.getChildren().addAll(profileSection, systemSection, actions);
    
    Stage settingsDialog = ModernNotificationUtils.createCustomDialog(
        "Paramètres de configuration",
        content,
        true
    );
    
    closeBtn.setOnAction(e -> settingsDialog.close());
    settingsDialog.show();
}

private void showSupport() {
    // Dialog de support moderne avec liens d'action
    VBox content = new VBox();
    content.setSpacing(20);
    content.setPadding(new Insets(20));
    content.setMaxWidth(500);
    
    Label title = new Label("🆘 Aide et support");
    title.getStyleClass().add("form-section-title");
    
    VBox supportOptions = new VBox();
    supportOptions.setSpacing(12);
    
    String[][] supportItems = {
        {"📖", "Guide d'utilisation", "Documentation complète", "CONSULTER"},
        {"💬", "Support technique", "support@eventpro.com", "CONTACTER"},
        {"🔴", "Chat en direct", "Disponible 24/7", "DÉMARRER"},
        {"❓", "Base de connaissances", "FAQ et tutoriels", "PARCOURIR"}
    };
    
    for (String[] item : supportItems) {
        HBox supportCard = new HBox();
        supportCard.getStyleClass().add("event-card");
        supportCard.setSpacing(16);
        supportCard.setAlignment(Pos.CENTER_LEFT);
        supportCard.setPadding(new Insets(16));
        
        Label icon = new Label(item[0]);
        icon.setStyle("-fx-font-size: 24px;");
        
        VBox itemInfo = new VBox();
        itemInfo.setSpacing(4);
        
        Label itemTitle = new Label(item[1]);
        itemTitle.setStyle("-fx-font-weight: 600;");
        
        Label itemDesc = new Label(item[2]);
        itemDesc.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        itemInfo.getChildren().addAll(itemTitle, itemDesc);
        
        Button actionBtn = new Button(item[3]);
        actionBtn.getStyleClass().add("primary-button-small");
        actionBtn.setOnAction(e -> ModernNotificationUtils.showInfoToast(item[1] + " - Fonctionnalité à implémenter"));
        
        supportCard.getChildren().addAll(icon, itemInfo, actionBtn);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);
        
        supportOptions.getChildren().add(supportCard);
    }
    
    // Informations système
    VBox systemInfo = new VBox();
    systemInfo.getStyleClass().add("form-section");
    systemInfo.setSpacing(8);
    
    Label systemTitle = new Label("ℹ️ Informations système");
    systemTitle.getStyleClass().add("form-section-title");
    
    Label versionInfo = new Label(
        "Version connectée avec pattern Observer\n" +
        "Interface responsive avec ScrollPane stylisés\n" +
        "Synchronisation temps réel active"
    );
    versionInfo.getStyleClass().add("form-help-text");
    versionInfo.setWrapText(true);
    
    systemInfo.getChildren().addAll(systemTitle, versionInfo);
    
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("primary-button");
    
    actions.getChildren().add(closeBtn);
    
    content.getChildren().addAll(title, supportOptions, systemInfo, actions);
    
    Stage supportDialog = ModernNotificationUtils.createCustomDialog(
        "Aide et support",
        content,
        true
    );
    
    closeBtn.setOnAction(e -> supportDialog.close());
    supportDialog.show();
}

private void logout() {
    boolean confirmed = ModernNotificationUtils.showConfirmation(
        "Confirmer la déconnexion",
        "Êtes-vous sûr de vouloir vous déconnecter ?\n\n" +
        "✅ Toutes les modifications ont été sauvegardées automatiquement.\n" +
        "🔄 Vos données sont synchronisées en temps réel."
    );
    
    if (confirmed) {
        // Animation de déconnexion
        ModernNotificationUtils.showInfoToast("Déconnexion en cours...");
        
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    // Nettoyer les observers
                    dataSynchronizer.removeGlobalObserver(organizerUIObserver);
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
    // Message de bienvenue moderne avec statistiques
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
    
    Label welcomeTitle = new Label("Bienvenue " + currentUser.getNom() + " !");
    welcomeTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 600;");
    
    Label welcomeSubtitle = new Label("Espace Organisateur - Connecté en Temps Réel & Responsive");
    welcomeSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
    
    welcomeHeader.getChildren().addAll(welcomeIcon, welcomeTitle, welcomeSubtitle);
    
    // Statistiques actuelles
    VBox statsSection = new VBox();
    statsSection.getStyleClass().add("form-section");
    statsSection.setSpacing(12);
    
    Label statsTitle = new Label("📊 Votre tableau de bord actuel");
    statsTitle.getStyleClass().add("form-section-title");
    
    HBox statsBar = new HBox();
    statsBar.setSpacing(16);
    statsBar.setAlignment(Pos.CENTER);
    
    int eventsCount = currentOrganizer.getEvenementsOrganises().size();
    int participantsCount = currentOrganizer.getEvenementsOrganises().stream()
        .mapToInt(e -> e.getParticipants().size())
        .sum();
    
    VBox eventsCard = createQuickStatCard(String.valueOf(eventsCount), "Événements", "#2196f3");
    VBox participantsCard = createQuickStatCard(String.valueOf(participantsCount), "Participants", "#4caf50");
    VBox statusCard = createQuickStatCard("ACTIF", "Statut", "#ff9800");
    
    statsBar.getChildren().addAll(eventsCard, participantsCard, statusCard);
    
    statsSection.getChildren().addAll(statsTitle, statsBar);
    
    // Fonctionnalités actives
    VBox featuresSection = new VBox();
    featuresSection.getStyleClass().add("form-section");
    featuresSection.setSpacing(12);
    
    Label featuresTitle = new Label("✨ Fonctionnalités actives");
    featuresTitle.getStyleClass().add("form-section-title");
    
    VBox featuresList = new VBox();
    featuresList.setSpacing(8);
    
    String[] features = {
        "✅ Synchronisation Observer active",
        "✅ Interface responsive active",
        "✅ ScrollPane stylisés",
        "✅ Notifications en temps réel",
        "✅ Sauvegarde automatique",
        "✅ Design Material moderne"
    };
    
    for (String feature : features) {
        Label featureLabel = new Label(feature);
        featureLabel.getStyleClass().add("info-text");
        featuresList.getChildren().add(featureLabel);
    }
    
    featuresSection.getChildren().addAll(featuresTitle, featuresList);
    
    // Message d'encouragement
    Label encouragement = new Label(
        "🚀 Vous pouvez maintenant créer, modifier et gérer vos événements " +
        "avec des mises à jour automatiques et une interface adaptative."
    );
    encouragement.getStyleClass().add("form-help-text");
    encouragement.setWrapText(true);
    encouragement.setStyle("-fx-background-color: #e8f5e8; -fx-padding: 16px; " +
                          "-fx-border-color: #4caf50; -fx-border-radius: 8px; " +
                          "-fx-background-radius: 8px; -fx-text-fill: #2e7d32;");
    
    HBox actions = new HBox();
    actions.setSpacing(12);
    actions.setAlignment(Pos.CENTER_RIGHT);
    
    Button startBtn = new Button("🚀 Commencer");
    startBtn.getStyleClass().add("primary-button");
    
    Button closeBtn = new Button("Fermer");
    closeBtn.getStyleClass().add("secondary-button");
    
    actions.getChildren().addAll(startBtn, closeBtn);
    
    content.getChildren().addAll(welcomeHeader, statsSection, featuresSection, encouragement, actions);
    
    Stage welcomeDialog = ModernNotificationUtils.createCustomDialog(
        "Bienvenue dans votre espace",
        content,
        true
    );
    
    startBtn.setOnAction(e -> {
        welcomeDialog.close();
        ModernNotificationUtils.showSuccessToast("Prêt à organiser de superbes événements !");
    });
    
    closeBtn.setOnAction(e -> welcomeDialog.close());
    welcomeDialog.show();
}

private void refreshCurrentView() {
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
                globalStatusLabel.setText("✅ En ligne");
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
        if (dataSynchronizer != null && organizerUIObserver != null) {
            dataSynchronizer.removeGlobalObserver(organizerUIObserver);
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}