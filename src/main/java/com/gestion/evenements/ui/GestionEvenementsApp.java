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

import com.gestion.evenements.controller.*;
import com.gestion.evenements.observer.UIObserver;
import com.gestion.evenements.util.DataSynchronizer;
import com.gestion.evenements.ui.utils.ModernNotificationUtils;
import com.gestion.evenements.auth.User;
import com.gestion.evenements.auth.UserRole;

/**
 * Application principale avec interface graphique moderne - Version connectée avec utilisateur réel
 * Utilise le pattern Observer pour synchroniser les données entre le front-end et le back-end
 * Interface d'administration accessible uniquement aux administrateurs connectés
 */
public class GestionEvenementsApp extends Application {

    private BorderPane mainLayout;
    private ScrollPane sideBarScrollPane;
    private VBox sideBar;
    private ScrollPane contentScrollPane;
    private BorderPane contentArea;
    private Button selectedButton;
    private Label globalStatusLabel;
    
    // Services et synchronisation
    private DataSynchronizer dataSynchronizer;
    private UIObserver globalUIObserver;
    
    // Utilisateur connecté (réel)
    private User currentUser;
    
    // Contrôleurs connectés au back-end
    private EvenementController evenementController;
    private ParticipantController participantController;
    private OrganisateurController organisateurController;

    /**
     * Définit l'utilisateur connecté (appelé depuis LoginView)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getRole() == UserRole.ADMINISTRATEUR) {
            System.out.println("✅ Administrateur connecté: " + user.getNom());
        } else if (user != null) {
            throw new IllegalArgumentException("L'utilisateur doit être un administrateur pour accéder à cette interface");
        }
        // Si user est null, on permet l'accès en mode démo (pour les tests)
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialisation des services
        initializeServices();
        
        // Configuration de la fenêtre principale
        String titleSuffix = currentUser != null ? " (" + currentUser.getNom() + ")" : " (Mode Démo)";
        primaryStage.setTitle("Gestion d'Événements - Interface Administrative" + titleSuffix);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        // Chargement des polices
        loadFonts();
        
        // Initialisation des contrôleurs connectés
        initializeControllers();
        
        // Création de l'interface responsive
        createResponsiveUI();

        // Création de la scène avec CSS
        Scene scene = new Scene(mainLayout, 1008, 720);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Afficher les événements par défaut
        showEvenements();
        
        // Afficher un message de bienvenue moderne
        showWelcomeMessage();
    }
    
    private void initializeServices() {
        // Initialiser le synchronisateur de données
        dataSynchronizer = DataSynchronizer.getInstance();

        dataSynchronizer.loadAllData();
        
        // Créer un observer global pour l'interface
        globalUIObserver = new UIObserver(() -> {
            // Callback pour rafraîchir toute l'interface quand il y a des changements
            updateGlobalStatus();
        });
        
        // Ajouter l'observer global
        dataSynchronizer.addGlobalObserver(globalUIObserver);
    }
    
    private void loadFonts() {
        Font font = Font.loadFont(
            getClass().getResourceAsStream("/com/gestion/evenements/ui/fonts/Poppins-Regular.ttf"), 12
        );
        
        if (font == null) {
            System.out.println("⚠️ Police Poppins non trouvée, utilisation de la police par défaut");
        } else {
            System.out.println("✅ Police Poppins chargée avec succès");
        }
    }
    
    private void initializeControllers() {
        // Créer les contrôleurs connectés au back-end
        evenementController = new EvenementController();
        participantController = new ParticipantController();
        organisateurController = new OrganisateurController();
        
        System.out.println("✅ Contrôleurs initialisés et connectés au back-end");
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
        
        // Statut global du système
        VBox systemStatus = createSystemStatus();
        
        // Boutons de navigation
        Button btnEvenements = createNavButton("📅", "Événements", "nav-button");
        Button btnParticipants = createNavButton("👥", "Participants", "nav-button");
        Button btnOrganisateurs = createNavButton("👨‍💼", "Organisateurs", "nav-button");
        
        // Actions des boutons avec gestion des erreurs
        btnEvenements.setOnAction(e -> {
            try {
                setSelectedButton(btnEvenements);
                showEvenements();
            } catch (Exception ex) {
                showError("Erreur lors du chargement des événements", ex);
            }
        });
        
        btnParticipants.setOnAction(e -> {
            try {
                setSelectedButton(btnParticipants);
                showParticipants();
            } catch (Exception ex) {
                showError("Erreur lors du chargement des participants", ex);
            }
        });
        
        btnOrganisateurs.setOnAction(e -> {
            try {
                setSelectedButton(btnOrganisateurs);
                showOrganisateurs();
            } catch (Exception ex) {
                showError("Erreur lors du chargement des organisateurs", ex);
            }
        });
        
        // Spacer pour pousser les boutons vers le bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Boutons d'administration
        Button btnReload = createNavButton("🔄", "Recharger données", "nav-button-secondary");
        Button btnSettings = createNavButton("⚙️", "Paramètres", "nav-button-secondary");
        Button btnLogout = createNavButton("🚪", currentUser != null ? "Déconnexion" : "Connexion", "nav-button-secondary");
        
        // Actions des boutons d'administration
        btnReload.setOnAction(e -> reloadData());
        btnSettings.setOnAction(e -> showSettings());
        btnLogout.setOnAction(e -> logout());
        
        sideBar.getChildren().addAll(
            header,
            new Separator(),
            systemStatus,
            new Separator(),
            btnEvenements,
            btnParticipants,
            btnOrganisateurs,
            spacer,
            new Separator(),
            btnReload,
            btnSettings,
            btnLogout
        );
        
        // Sélectionner le premier bouton par défaut
        selectedButton = btnEvenements;
        btnEvenements.getStyleClass().add("nav-button-selected");
        
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
        updateContentHeader("Gestion des Événements", "Interface administrative connectée au système");
        
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
        Label appTitle = new Label("Administration");
        appTitle.getStyleClass().add("app-title");
        
        Label appSubtitle = new Label("Événements");
        appSubtitle.getStyleClass().add("app-subtitle");
        
        Label appVersion = new Label(currentUser != null ? "v2.0 - Connecté" : "v2.0 - Démo");
        appVersion.getStyleClass().add("text-secondary");
        appVersion.setStyle("-fx-font-size: 10px;");
        
        header.getChildren().addAll(appIcon, appTitle, appSubtitle, appVersion);
        return header;
    }
    
    private VBox createSystemStatus() {
        VBox statusBox = new VBox();
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setSpacing(8);
        statusBox.setPadding(new Insets(16, 24, 16, 24));
        statusBox.getStyleClass().add("stat-card");
        
        Label statusTitle = new Label("Utilisateur");
        statusTitle.getStyleClass().add("organizer-role");
        statusTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 600;");
        
        Label userLabel = new Label(currentUser != null ? currentUser.getNom() : "Mode Démo");
        userLabel.getStyleClass().add("organizer-name");
        userLabel.setStyle("-fx-font-size: 13px;");
        
        globalStatusLabel = new Label(currentUser != null ? "🔐 Administrateur" : "🔓 Accès libre");
        globalStatusLabel.getStyleClass().add("status-active");
        globalStatusLabel.setStyle("-fx-font-size: 11px;");
        
        // Afficher les statistiques globales
        DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
        Label statsLabel = new Label(String.format("%d événements • %d participants", 
                                                  stats.getTotalEvents(), 
                                                  stats.getTotalParticipants()));
        statsLabel.getStyleClass().add("text-secondary");
        statsLabel.setStyle("-fx-font-size: 10px;");
        
        statusBox.getChildren().addAll(statusTitle, userLabel, globalStatusLabel, statsLabel);
        return statusBox;
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
    private void showEvenements() {
        updateContentHeader("Gestion des Événements", "Créez et gérez vos événements avec le système connecté");
        contentArea.setCenter(evenementController.getView());
        scrollToTop();
        updateGlobalStatus();
    }
    
    private void showParticipants() {
        updateContentHeader("Gestion des Participants", "Gérez les participants avec synchronisation en temps réel");
        contentArea.setCenter(participantController.getView());
        scrollToTop();
        updateGlobalStatus();
    }
    
    private void showOrganisateurs() {
        updateContentHeader("Gestion des Organisateurs", "Administrez les organisateurs et leurs événements");
        contentArea.setCenter(organisateurController.getView());
        scrollToTop();
        updateGlobalStatus();
    }
    
    private void scrollToTop() {
        // Remonter automatiquement en haut lors du changement de vue
        javafx.application.Platform.runLater(() -> {
            contentScrollPane.setVvalue(0);
            contentScrollPane.setHvalue(0);
        });
    }
    
    private void updateGlobalStatus() {
        try {
            DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
            globalStatusLabel.setText(String.format("✅ %d événements • %d participants actifs", 
                                                   stats.getTotalEvents(), 
                                                   stats.getTotalParticipants()));
            globalStatusLabel.getStyleClass().clear();
            globalStatusLabel.getStyleClass().add("status-active");
        } catch (Exception e) {
            globalStatusLabel.setText("❌ Erreur de connexion");
            globalStatusLabel.getStyleClass().clear();
            globalStatusLabel.getStyleClass().add("status-inactive");
        }
    }
    
    private void reloadData() {
        // Créer un dialog de progression pour le rechargement
        Stage progressDialog = ModernNotificationUtils.createProgressDialog(
            "Rechargement en cours",
            "Actualisation des données du système..."
        );
        progressDialog.show();
        
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulation du rechargement
                
                Platform.runLater(() -> {
                    progressDialog.close();
                    
                    try {
                        dataSynchronizer.reloadDemoData();
                        
                        // Snackbar avec action pour voir les changements
                        ModernNotificationUtils.showSnackbar(
                            "Données rechargées avec succès",
                            "VOIR",
                            () -> {
                                if (selectedButton != null) {
                                    selectedButton.fire();
                                }
                                ModernNotificationUtils.showInfoToast("Interface mise à jour");
                            }
                        );
                        
                        // Rafraîchir la vue actuelle
                        if (selectedButton != null) {
                            selectedButton.fire();
                        }
                        
                        System.out.println("✅ Données rechargées par " + 
                            (currentUser != null ? currentUser.getNom() : "Mode Démo"));
                        
                    } catch (Exception e) {
                        showError("Erreur lors du rechargement des données", e);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    progressDialog.close();
                    ModernNotificationUtils.showErrorToast("Rechargement interrompu");
                });
            }
        }).start();
    }
    
    private void showSettings() {
        // Créer un dialog de paramètres moderne et détaillé
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setMaxWidth(600);
        
        // Configuration utilisateur
        VBox userSection = createUserConfigSection();
        
        // Configuration système
        VBox systemSection = createSystemConfigSection();
        
        // Configuration de l'interface
        VBox interfaceSection = createInterfaceConfigSection();
        
        // Configuration de synchronisation
        VBox syncSection = createSyncConfigSection();
        
        // Informations de version
        VBox versionSection = createVersionInfoSection();
        
        // Actions
        HBox actions = createSettingsActions();
        
        content.getChildren().addAll(userSection, systemSection, interfaceSection, syncSection, versionSection, actions);
        
        Stage settingsDialog = ModernNotificationUtils.createCustomDialog(
            "Configuration du système",
            content,
            true
        );
        
        settingsDialog.show();
    }
    
    private VBox createUserConfigSection() {
        VBox userSection = new VBox();
        userSection.getStyleClass().add("form-section");
        userSection.setSpacing(12);
        
        Label userTitle = new Label("👤 Informations utilisateur");
        userTitle.getStyleClass().add("form-section-title");
        
        GridPane userGrid = new GridPane();
        userGrid.setHgap(20);
        userGrid.setVgap(8);
        
        int row = 0;
        if (currentUser != null) {
            addInfoRow(userGrid, "Nom:", currentUser.getNom(), row++);
            addInfoRow(userGrid, "Email:", currentUser.getEmail(), row++);
            addInfoRow(userGrid, "Rôle:", currentUser.getRole().toString(), row++);
            addInfoRow(userGrid, "Organisation:", 
                      currentUser.getOrganisation() != null ? currentUser.getOrganisation() : "N/A", row++);
            addInfoRow(userGrid, "Compte actif:", currentUser.isActif() ? "Oui" : "Non", row++);
        } else {
            addInfoRow(userGrid, "Mode:", "Démonstration", row++);
            addInfoRow(userGrid, "Accès:", "Libre (non authentifié)", row++);
            addInfoRow(userGrid, "Permissions:", "Lecture/Écriture", row++);
        }
        
        userSection.getChildren().addAll(userTitle, userGrid);
        return userSection;
    }
    
    private VBox createSystemConfigSection() {
        VBox systemSection = new VBox();
        systemSection.getStyleClass().add("form-section");
        systemSection.setSpacing(12);
        
        Label systemTitle = new Label("⚙️ Configuration système");
        systemTitle.getStyleClass().add("form-section-title");
        
        VBox systemItems = new VBox();
        systemItems.setSpacing(8);
        
        String[] systemSettings = {
            "✅ Pattern Observer: Activé",
            "✅ Synchronisation temps réel: Activée", 
            "✅ Interface responsive: Activée",
            "✅ ScrollPane stylisés: Activés",
            "✅ Notifications modernes: Activées",
            "✅ Base de données: En mémoire (opérationnelle)",
            "✅ Sérialisation JSON/XML: Active",
            "✅ Sauvegarde automatique: Active"
        };
        
        for (String setting : systemSettings) {
            Label settingLabel = new Label(setting);
            settingLabel.getStyleClass().add("info-text");
            systemItems.getChildren().add(settingLabel);
        }
        
        systemSection.getChildren().addAll(systemTitle, systemItems);
        return systemSection;
    }
    
    private VBox createInterfaceConfigSection() {
        VBox interfaceSection = new VBox();
        interfaceSection.getStyleClass().add("form-section");
        interfaceSection.setSpacing(12);
        
        Label interfaceTitle = new Label("🎨 Interface utilisateur");
        interfaceTitle.getStyleClass().add("form-section-title");
        
        GridPane interfaceGrid = new GridPane();
        interfaceGrid.setHgap(20);
        interfaceGrid.setVgap(8);
        
        int row = 0;
        addInfoRow(interfaceGrid, "Thème:", "Material Design", row++);
        addInfoRow(interfaceGrid, "Police:", "Poppins (chargée)", row++);
        addInfoRow(interfaceGrid, "Animations:", "Activées", row++);
        addInfoRow(interfaceGrid, "Responsive:", "Oui", row++);
        addInfoRow(interfaceGrid, "Accessibilité:", "Optimisée", row++);
        addInfoRow(interfaceGrid, "Mode:", currentUser != null ? "Authentifié" : "Démonstration", row++);
        
        interfaceSection.getChildren().addAll(interfaceTitle, interfaceGrid);
        return interfaceSection;
    }
    
    private VBox createSyncConfigSection() {
        VBox syncSection = new VBox();
        syncSection.getStyleClass().add("form-section");
        syncSection.setSpacing(12);
        
        Label syncTitle = new Label("🔄 Synchronisation");
        syncTitle.getStyleClass().add("form-section-title");
        
        // Statistiques de synchronisation en temps réel
        DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
        
        GridPane syncGrid = new GridPane();
        syncGrid.setHgap(20);
        syncGrid.setVgap(8);
        
        int row = 0;
        addInfoRow(syncGrid, "Statut:", "Connecté ✅", row++);
        addInfoRow(syncGrid, "Événements:", String.valueOf(stats.getTotalEvents()), row++);
        addInfoRow(syncGrid, "Participants:", String.valueOf(stats.getTotalParticipants()), row++);
        addInfoRow(syncGrid, "Inscriptions:", String.valueOf(stats.getTotalInscriptions()), row++);
        addInfoRow(syncGrid, "Utilisateurs:", String.valueOf(stats.getTotalUsers()), row++);
        addInfoRow(syncGrid, "Dernière sync:", "Temps réel", row++);
        
        syncSection.getChildren().addAll(syncTitle, syncGrid);
        return syncSection;
    }
    
    private VBox createVersionInfoSection() {
        VBox versionSection = new VBox();
        versionSection.getStyleClass().add("form-section");
        versionSection.setSpacing(12);
        
        Label versionTitle = new Label("ℹ️ Informations de version");
        versionTitle.getStyleClass().add("form-section-title");
        
        Label versionInfo = new Label(
            "Version 2.0 - Interface Administrative Connectée\n" +
            "• Authentification utilisateur intégrée\n" +
            "• Pattern Observer implémenté\n" +
            "• Notifications modernes avec animations\n" +
            "• Dialogs interactifs et responsive\n" +
            "• Système de synchronisation temps réel\n" +
            "• Interface adaptative Material Design\n" +
            "• Sérialisation JSON/XML automatique\n" +
            "• Gestion des exceptions métier"
        );
        versionInfo.getStyleClass().add("form-help-text");
        versionInfo.setWrapText(true);
        versionInfo.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 16px; " +
                            "-fx-border-color: #2196f3; -fx-border-radius: 8px; " +
                            "-fx-background-radius: 8px;");
        
        versionSection.getChildren().addAll(versionTitle, versionInfo);
        return versionSection;
    }
    
    private HBox createSettingsActions() {
        HBox actions = new HBox();
        actions.setSpacing(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button exportConfigBtn = new Button("📊 Exporter config");
        exportConfigBtn.getStyleClass().add("secondary-button");
        exportConfigBtn.setOnAction(e -> {
            try {
                dataSynchronizer.exportCompleteBackup();
                ModernNotificationUtils.showSuccessToast("Configuration et données exportées");
            } catch (Exception ex) {
                ModernNotificationUtils.showErrorToast("Erreur lors de l'export: " + ex.getMessage());
            }
        });
        
        Button resetBtn = new Button("🔄 Réinitialiser");
        resetBtn.getStyleClass().add("icon-button-danger");
        resetBtn.setOnAction(e -> {
            boolean confirmed = ModernNotificationUtils.showConfirmation(
                "Réinitialiser la configuration",
                "Êtes-vous sûr de vouloir réinitialiser tous les paramètres ?\n\n" +
                "⚠️ Cette action restaurera la configuration par défaut et rechargera les données de démonstration."
            );
            
            if (confirmed) {
                reloadData();
            }
        });
        
        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("primary-button");
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });
        
        actions.getChildren().addAll(exportConfigBtn, resetBtn, closeBtn);
        return actions;
    }
    
    private void addInfoRow(GridPane grid, String label, String value, int row) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: 600; -fx-min-width: 120px;");
        
        Label valueNode = new Label(value);
        valueNode.setWrapText(true);
        valueNode.setStyle("-fx-text-fill: #2e7d32;");
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
    
    private void logout() {
        if (currentUser != null) {
            // Utilisateur connecté - déconnexion
            boolean confirmed = ModernNotificationUtils.showConfirmation(
                "Confirmer la déconnexion",
                "Êtes-vous sûr de vouloir vous déconnecter de l'interface administrative ?\n\n" +
                "✅ Toutes les modifications ont été sauvegardées automatiquement.\n" +
                "🔄 La synchronisation temps réel sera interrompue."
            );
            
            if (confirmed) {
                performLogout();
            }
        } else {
            // Mode démo - redirection vers connexion
            performLogout();
        }
    }
    
    private void performLogout() {
        // Animation de déconnexion avec progression
        Stage progressDialog = ModernNotificationUtils.createProgressDialog(
            "Déconnexion en cours",
            "Sauvegarde des paramètres et fermeture des connexions..."
        );
        progressDialog.show();
        
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Platform.runLater(() -> {
                    progressDialog.close();
                    
                    try {
                        // Nettoyer les observers
                        dataSynchronizer.removeGlobalObserver(globalUIObserver);
                        
                        ModernNotificationUtils.showSuccessToast("Déconnexion réussie");
                        
                        // Fermer la fenêtre actuelle
                        Stage currentStage = (Stage) mainLayout.getScene().getWindow();
                        currentStage.close();
                        
                        // Ouvrir la fenêtre de connexion
                        Stage loginStage = new Stage();
                        LoginView loginView = new LoginView();
                        loginView.start(loginStage);
                        
                        if (currentUser != null) {
                            System.out.println("✅ Administrateur " + currentUser.getNom() + " déconnecté");
                        } else {
                            System.out.println("✅ Session démo fermée");
                        }
                        
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors de la redirection: " + e.getMessage());
                        javafx.application.Platform.exit();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    progressDialog.close();
                    ModernNotificationUtils.showErrorToast("Déconnexion interrompue");
                });
            }
        }).start();
    }
    
    private void showWelcomeMessage() {
        // Message de bienvenue moderne et détaillé
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setMaxWidth(700);
        
        // En-tête de bienvenue
        VBox welcomeHeader = createWelcomeHeader();
        
        // Statistiques du système
        VBox systemStats = createSystemStatsSection();
        
        // Fonctionnalités modernes
        VBox featuresSection = createModernFeaturesSection();
        
        // Guide de démarrage rapide
        VBox quickStartSection = createQuickStartSection();
        
        // Actions de bienvenue
        HBox welcomeActions = createWelcomeActions();
        
        content.getChildren().addAll(welcomeHeader, systemStats, featuresSection, quickStartSection, welcomeActions);
        
        Stage welcomeDialog = ModernNotificationUtils.createCustomDialog(
            "Interface Administrative - Système Connecté",
            content,
            true
        );
        
        welcomeDialog.show();
    }
    
    private VBox createWelcomeHeader() {
        VBox welcomeHeader = new VBox();
        welcomeHeader.getStyleClass().add("form-section");
        welcomeHeader.setAlignment(Pos.CENTER);
        welcomeHeader.setSpacing(12);
        welcomeHeader.setPadding(new Insets(20));
        
        Label welcomeIcon = new Label("🎉");
        welcomeIcon.setStyle("-fx-font-size: 48px;");
        
        String welcomeText = currentUser != null ? 
            "Bienvenue " + currentUser.getNom() + " !" : 
            "Bienvenue dans l'interface administrative !";
        
        Label welcomeTitle = new Label(welcomeText);
        welcomeTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 600;");
        
        String subtitleText = currentUser != null ?
            "Interface Authentifiée • Accès Administrateur • Synchronisation Temps Réel" :
            "Mode Démonstration • Accès Libre • Synchronisation Temps Réel";
        
        Label welcomeSubtitle = new Label(subtitleText);
        welcomeSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        welcomeHeader.getChildren().addAll(welcomeIcon, welcomeTitle, welcomeSubtitle);
        return welcomeHeader;
    }
    
    private VBox createSystemStatsSection() {
        VBox systemStats = new VBox();
        systemStats.getStyleClass().add("form-section");
        systemStats.setSpacing(12);
        
        Label statsTitle = new Label("📊 État actuel du système");
        statsTitle.getStyleClass().add("form-section-title");
        
        DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
        
        HBox statsBar = new HBox();
        statsBar.setSpacing(16);
        statsBar.setAlignment(Pos.CENTER);
        
        VBox eventsCard = createQuickStatCard(
            String.valueOf(stats.getTotalEvents()),
            "Événements",
            "#2196f3"
        );
        
        VBox participantsCard = createQuickStatCard(
            String.valueOf(stats.getTotalParticipants()),
            "Participants",
            "#4caf50"
        );
        
        VBox inscriptionsCard = createQuickStatCard(
            String.valueOf(stats.getTotalInscriptions()),
            "Inscriptions",
            "#ff9800"
        );
        
        VBox usersCard = createQuickStatCard(
            String.valueOf(stats.getTotalUsers()),
            "Utilisateurs",
            "#9c27b0"
        );
        
        statsBar.getChildren().addAll(eventsCard, participantsCard, inscriptionsCard, usersCard);
        systemStats.getChildren().addAll(statsTitle, statsBar);
        return systemStats;
    }
    
    private VBox createModernFeaturesSection() {
        VBox featuresSection = new VBox();
        featuresSection.getStyleClass().add("form-section");
        featuresSection.setSpacing(12);
        
        Label featuresTitle = new Label("✨ Fonctionnalités activées");
        featuresTitle.getStyleClass().add("form-section-title");
        
        VBox featuresList = new VBox();
        featuresList.setSpacing(8);
        
        String[] features = {
            "✅ Authentification utilisateur sécurisée",
            "✅ Pattern Observer pour synchronisation temps réel",
            "✅ Notifications modernes avec animations fluides",
            "✅ Dialogs interactifs et confirmations intelligentes",
            "✅ Interface responsive s'adaptant à la taille de fenêtre",
            "✅ ScrollPane stylisés pour une navigation optimale",
            "✅ Design Material avec transitions et feedback visuel",
            "✅ Sérialisation automatique JSON/XML",
            "✅ Gestion d'exceptions métier intégrée",
            "✅ Sauvegarde automatique des données"
        };
        
        for (String feature : features) {
            Label featureLabel = new Label(feature);
            featureLabel.getStyleClass().add("info-text");
            featuresList.getChildren().add(featureLabel);
        }
        
        featuresSection.getChildren().addAll(featuresTitle, featuresList);
        return featuresSection;
    }
    
    private VBox createQuickStartSection() {
        VBox quickStartSection = new VBox();
        quickStartSection.getStyleClass().add("form-section");
        quickStartSection.setSpacing(12);
        
        Label quickStartTitle = new Label("🚀 Guide de démarrage rapide");
        quickStartTitle.getStyleClass().add("form-section-title");
        
        String guideText = currentUser != null ?
            "L'interface administrative vous permet de :\n\n" +
            "• 📅 Gérer tous les événements du système\n" +
            "• 👥 Administrer les participants et leurs inscriptions\n" +
            "• 👨‍💼 Superviser les organisateurs et leurs activités\n" +
            "• 🔄 Recharger les données en temps réel\n" +
            "• ⚙️ Configurer les paramètres du système\n" +
            "• 📊 Exporter et sauvegarder les données\n\n" +
            "En tant qu'administrateur connecté, toutes vos modifications sont " +
            "automatiquement sauvegardées et synchronisées en temps réel avec " +
            "tous les autres utilisateurs du système." :
            
            "L'interface administrative en mode démonstration vous permet de :\n\n" +
            "• 📅 Consulter et tester la gestion des événements\n" +
            "• 👥 Explorer l'administration des participants\n" +
            "• 👨‍💼 Découvrir la supervision des organisateurs\n" +
            "• 🔄 Tester le rechargement des données\n" +
            "• ⚙️ Explorer les paramètres du système\n\n" +
            "Pour accéder à toutes les fonctionnalités et sauvegarder vos " +
            "modifications, connectez-vous avec un compte administrateur.";
        
        Label quickStartText = new Label(guideText);
        quickStartText.getStyleClass().add("form-help-text");
        quickStartText.setWrapText(true);
        quickStartText.setStyle("-fx-background-color: #f3e5f5; -fx-padding: 16px; " +
                               "-fx-border-color: #9c27b0; -fx-border-radius: 8px; " +
                               "-fx-background-radius: 8px;");
        
        quickStartSection.getChildren().addAll(quickStartTitle, quickStartText);
        return quickStartSection;
    }
    
    private HBox createWelcomeActions() {
        HBox welcomeActions = new HBox();
        welcomeActions.setSpacing(12);
        welcomeActions.setAlignment(Pos.CENTER_RIGHT);
        
        Button exploreBtn = new Button("🔍 Explorer les événements");
        exploreBtn.getStyleClass().add("primary-button");
        exploreBtn.setOnAction(e -> {
            Stage stage = (Stage) exploreBtn.getScene().getWindow();
            stage.close();
            showEvenements();
            ModernNotificationUtils.showInfoToast("Chargement de la gestion des événements");
        });
        
        Button settingsBtn = new Button("⚙️ Voir paramètres");
        settingsBtn.getStyleClass().add("secondary-button");
        settingsBtn.setOnAction(e -> {
            Stage stage = (Stage) settingsBtn.getScene().getWindow();
            stage.close();
            showSettings();
        });
        
        Button startBtn = new Button("Commencer");
        startBtn.getStyleClass().add("secondary-button");
        startBtn.setOnAction(e -> {
            Stage stage = (Stage) startBtn.getScene().getWindow();
            stage.close();
            ModernNotificationUtils.showSuccessToast("Interface administrative prête !");
        });
        
        welcomeActions.getChildren().addAll(exploreBtn, settingsBtn, startBtn);
        return welcomeActions;
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
    
    private void showError(String message, Exception e) {
        // Mettre à jour le statut global
        globalStatusLabel.setText("❌ " + message);
        globalStatusLabel.getStyleClass().clear();
        globalStatusLabel.getStyleClass().add("status-inactive");
        
        // Afficher une notification d'erreur moderne
        ModernNotificationUtils.showErrorToast(message + ": " + e.getMessage());
        
        // Log de l'erreur
        System.err.println("❌ Erreur dans l'interface admin: " + message + " - " + e.getMessage());
        if (currentUser != null) {
            System.err.println("   Utilisateur: " + currentUser.getNom());
        }
        e.printStackTrace();
    }
    
    private void showSuccessMessage(String message) {
        // Mettre à jour le statut global
        globalStatusLabel.setText("✅ " + message);
        globalStatusLabel.getStyleClass().clear();
        globalStatusLabel.getStyleClass().add("status-active");
        
        // Toast pour feedback immédiat
        ModernNotificationUtils.showSuccessToast(message);
        
        // Revenir au statut normal après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(this::updateGlobalStatus);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    @Override
    public void stop() throws Exception {
        // Nettoyer les observers lors de la fermeture
        if (dataSynchronizer != null && globalUIObserver != null) {
            dataSynchronizer.removeGlobalObserver(globalUIObserver);
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}