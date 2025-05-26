package com.gestion.evenements.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

/**
 * Interface de bienvenue et présentation de l'application
 * Page d'accueil avec présentation des fonctionnalités et boutons de connexion/inscription
 * Point d'entrée principal de l'application
 */
public class WelcomeView extends Application {

    private BorderPane mainLayout;
    private VBox centerContent;
    private Label titleLabel;
    private Label subtitleLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("EventPro - Gestion d'Événements Professionnelle");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        createWelcomeInterface();
        addAnimations();
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Afficher message d'information au démarrage
        showStartupInfo();
    }
    
    private void createWelcomeInterface() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("welcome-container");
        
        // Header avec navigation
        HBox header = createHeader();
        
        // Contenu central
        centerContent = createCenterContent();
        
        // Fonctionnalités
        VBox features = createFeaturesSection();
        
        // Footer
        HBox footer = createFooter();
        
        // Assemblage avec la section fonctionnalités intégrée
        VBox fullCenter = new VBox();
        fullCenter.getChildren().addAll(centerContent, features);
        
        mainLayout.setTop(header);
        mainLayout.setCenter(fullCenter);
        mainLayout.setBottom(footer);
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("welcome-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 40, 20, 40));
        header.setSpacing(20);
        
        // Logo et nom de l'application
        HBox logo = new HBox();
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setSpacing(15);
        
        Label logoIcon = new Label("🎯");
        logoIcon.setStyle("-fx-font-size: 32px;");
        
        VBox logoText = new VBox();
        logoText.setSpacing(2);
        
        Label appName = new Label("EventPro");
        appName.getStyleClass().add("welcome-logo-title");
        appName.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label appTagline = new Label("Gestion d'Événements Connectée");
        appTagline.getStyleClass().add("welcome-logo-subtitle");
        appTagline.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        logoText.getChildren().addAll(appName, appTagline);
        logo.getChildren().addAll(logoIcon, logoText);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Boutons de navigation
        HBox navButtons = new HBox();
        navButtons.setSpacing(15);
        navButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button loginBtn = new Button("Se connecter");
        loginBtn.getStyleClass().add("secondary-button");
        loginBtn.setStyle("-fx-padding: 12px 24px; -fx-font-size: 14px;");
        loginBtn.setOnAction(e -> openLoginView());
        
        Button registerBtn = new Button("S'inscrire");
        registerBtn.getStyleClass().add("primary-button");
        registerBtn.setStyle("-fx-padding: 12px 24px; -fx-font-size: 14px;");
        registerBtn.setOnAction(e -> openRegisterView());
        
        Button demoBtn = new Button("Mode Démo");
        demoBtn.getStyleClass().add("icon-button");
        demoBtn.setStyle("-fx-padding: 12px 24px; -fx-font-size: 14px;");
        demoBtn.setOnAction(e -> openDemoMode());
        
        navButtons.getChildren().addAll(loginBtn, registerBtn, demoBtn);
        
        header.getChildren().addAll(logo, spacer, navButtons);
        return header;
    }
    
    private VBox createCenterContent() {
        VBox center = new VBox();
        center.setAlignment(Pos.CENTER);
        center.setSpacing(40);
        center.setPadding(new Insets(60, 40, 40, 40));
        
        // Titre principal
        titleLabel = new Label("Gérez vos événements en temps réel");
        titleLabel.getStyleClass().add("welcome-title");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-text-alignment: center;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(800);
        
        // Sous-titre
        subtitleLabel = new Label("Plateforme connectée avec synchronisation en temps réel - Pattern Observer intégré");
        subtitleLabel.getStyleClass().add("welcome-subtitle");
        subtitleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(600);
        
        // Boutons d'action principaux
        HBox actionButtons = createMainActionButtons();
        
        center.getChildren().addAll(titleLabel, subtitleLabel, actionButtons);
        return center;
    }
    
    private HBox createMainActionButtons() {
        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(20);
        
        Button startBtn = new Button("🚀 Commencer");
        startBtn.getStyleClass().add("primary-button");
        startBtn.setStyle("-fx-font-size: 18px; -fx-padding: 16px 32px; -fx-background-radius: 8px;");
        startBtn.setOnAction(e -> openLoginView());
        
        Button organizerBtn = new Button("👨‍💼 Espace Organisateur");
        organizerBtn.getStyleClass().add("secondary-button");
        organizerBtn.setStyle("-fx-font-size: 18px; -fx-padding: 16px 32px; -fx-background-radius: 8px;");
        organizerBtn.setOnAction(e -> {
            Alert info = createInfoAlert("Espace Organisateur", 
                "Connectez-vous avec un compte organisateur pour :\n\n" +
                "• Créer et gérer vos événements\n" +
                "• Suivre vos participants en temps réel\n" +
                "• Générer des rapports détaillés\n" +
                "• Synchronisation automatique des données");
            info.showAndWait();
            openLoginView();
        });
        
        Button participantBtn = new Button("👤 Espace Participant");
        participantBtn.getStyleClass().add("icon-button");
        participantBtn.setStyle("-fx-font-size: 18px; -fx-padding: 16px 32px; -fx-background-radius: 8px;");
        participantBtn.setOnAction(e -> {
            Alert info = createInfoAlert("Espace Participant",
                "Connectez-vous avec un compte participant pour :\n\n" +
                "• Découvrir les événements disponibles\n" +
                "• S'inscrire en temps réel\n" +
                "• Suivre vos inscriptions\n" +
                "• Recevoir des notifications automatiques");
            info.showAndWait();
            openLoginView();
        });
        
        buttons.getChildren().addAll(startBtn, organizerBtn, participantBtn);
        return buttons;
    }
    
    private VBox createFeaturesSection() {
        VBox features = new VBox();
        features.setAlignment(Pos.CENTER);
        features.setSpacing(30);
        features.setPadding(new Insets(40, 40, 60, 40));
        features.setStyle("-fx-background-color: #f8fafc;");
        
        Label featuresTitle = new Label("Fonctionnalités connectées");
        featuresTitle.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        HBox featuresGrid = new HBox();
        featuresGrid.setAlignment(Pos.CENTER);
        featuresGrid.setSpacing(40);
        
        VBox feature1 = createFeatureCard("🔄", "Temps Réel", 
            "Synchronisation automatique avec pattern Observer - Mises à jour instantanées pour tous les utilisateurs");
        
        VBox feature2 = createFeatureCard("💾", "Persistance", 
            "Sauvegarde automatique JSON/XML - Vos données sont toujours protégées et synchronisées");
        
        VBox feature3 = createFeatureCard("🔔", "Notifications", 
            "Alertes en temps réel - Restez informé des changements et mises à jour importantes");
        
        VBox feature4 = createFeatureCard("🎭", "Multi-Rôles", 
            "Interfaces spécialisées - Organisateurs et participants avec fonctionnalités adaptées");
        
        featuresGrid.getChildren().addAll(feature1, feature2, feature3, feature4);
        
        features.getChildren().addAll(featuresTitle, featuresGrid);
        return features;
    }
    
    private VBox createFeatureCard(String icon, String title, String description) {
        VBox card = new VBox();
        card.getStyleClass().add("feature-card");
        card.setAlignment(Pos.CENTER);
        card.setSpacing(15);
        card.setPadding(new Insets(30));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(200);
        
        card.getChildren().addAll(iconLabel, titleLabel, descLabel);
        return card;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.getStyleClass().add("welcome-footer");
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(30, 40, 30, 40));
        footer.setStyle("-fx-background-color: #2c3e50;");
        
        Label footerText = new Label("© 2025 EventPro - Plateforme connectée avec synchronisation temps réel");
        footerText.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox footerLinks = new HBox();
        footerLinks.setSpacing(20);
        footerLinks.setAlignment(Pos.CENTER_RIGHT);
        
        Button aboutBtn = new Button("À propos");
        aboutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        aboutBtn.setOnAction(e -> showTechnicalInfo());
        
        Button helpBtn = new Button("Aide");
        helpBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        helpBtn.setOnAction(e -> showHelpInfo());
        
        footerLinks.getChildren().addAll(aboutBtn, helpBtn);
        
        footer.getChildren().addAll(footerText, spacer, footerLinks);
        return footer;
    }
    
    private void addAnimations() {
        // Animation du titre
        FadeTransition titleFade = new FadeTransition(Duration.millis(1000), titleLabel);
        titleFade.setFromValue(0.0);
        titleFade.setToValue(1.0);
        titleFade.play();
        
        // Animation du sous-titre
        FadeTransition subtitleFade = new FadeTransition(Duration.millis(1200), subtitleLabel);
        subtitleFade.setFromValue(0.0);
        subtitleFade.setToValue(1.0);
        subtitleFade.setDelay(Duration.millis(300));
        subtitleFade.play();
        
        // Animation du contenu central
        ScaleTransition centerScale = new ScaleTransition(Duration.millis(800), centerContent);
        centerScale.setFromX(0.9);
        centerScale.setFromY(0.9);
        centerScale.setToX(1.0);
        centerScale.setToY(1.0);
        centerScale.play();
    }
    
    // Actions des boutons - Connexion au système
    private void openLoginView() {
        try {
            // Fermer la fenêtre de bienvenue
            Stage currentStage = (Stage) mainLayout.getScene().getWindow();
            currentStage.close();
            
            // Ouvrir la vue de connexion
            Stage loginStage = new Stage();
            LoginView loginView = new LoginView();
            loginView.start(loginStage);
            
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la connexion", e.getMessage());
        }
    }
    
    private void openRegisterView() {
        try {
            // Fermer la fenêtre de bienvenue
            Stage currentStage = (Stage) mainLayout.getScene().getWindow();
            currentStage.close();
            
            // Ouvrir la vue d'inscription
            Stage registerStage = new Stage();
            RegisterView registerView = new RegisterView();
            registerView.start(registerStage);
            
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de l'inscription", e.getMessage());
        }
    }
    
    private void openDemoMode() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mode Démonstration");
        confirm.setHeaderText("Accéder au mode démonstration");
        confirm.setContentText("Le mode démonstration vous permet d'explorer l'interface participant " +
                              "avec des fonctionnalités limitées.\n\n" +
                              "Voulez-vous continuer en mode démo ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Fermer la fenêtre de bienvenue
                    Stage currentStage = (Stage) mainLayout.getScene().getWindow();
                    currentStage.close();
                    
                    // Ouvrir l'interface participant en mode démo
                    Stage demoStage = new Stage();
                    ParticipantApp participantApp = new ParticipantApp();
                    participantApp.setCurrentUser(null); // Mode invité
                    participantApp.start(demoStage);
                    
                } catch (Exception e) {
                    showError("Erreur lors de l'ouverture du mode démo", e.getMessage());
                }
            }
        });
    }
    
    private void showStartupInfo() {
        Alert info = createInfoAlert("EventPro - Application Connectée",
            "🎯 SYSTÈME CONNECTÉ EN TEMPS RÉEL\n\n" +
            "✅ Pattern Observer implémenté\n" +
            "✅ Synchronisation automatique des données\n" +
            "✅ Persistance JSON/XML\n" +
            "✅ Notifications en temps réel\n" +
            "✅ Interface responsive\n\n" +
            "Connectez-vous pour accéder à toutes les fonctionnalités !");
        info.showAndWait();
    }
    
    private void showTechnicalInfo() {
        Alert about = createInfoAlert("EventPro v2.0 - Technical Info",
            "🔧 ARCHITECTURE TECHNIQUE\n\n" +
            "• Java 17 + JavaFX\n" +
            "• Pattern Observer pour synchronisation temps réel\n" +
            "• Architecture MVC avec DataSynchronizer\n" +
            "• Sérialisation JSON/XML automatique\n" +
            "• Gestion d'exceptions métier\n" +
            "• Interface responsive avec ScrollPane stylisés\n" +
            "• Authentification multi-rôles\n\n" +
            "Tous les événements créés par les organisateurs sont " +
            "automatiquement visibles par les participants en temps réel.");
        about.showAndWait();
    }
    
    private void showHelpInfo() {
        Alert help = createInfoAlert("Comment utiliser EventPro",
            "🚀 GUIDE DE DÉMARRAGE\n\n" +
            "1️⃣ CRÉER UN COMPTE\n" +
            "• Cliquez sur 'S'inscrire'\n" +
            "• Choisissez votre rôle (Organisateur/Participant)\n" +
            "• Remplissez les informations requises\n\n" +
            "2️⃣ SE CONNECTER\n" +
            "• Utilisez vos identifiants\n" +
            "• Accédez à votre interface personnalisée\n\n" +
            "3️⃣ FONCTIONNALITÉS\n" +
            "• Organisateurs : Créez et gérez vos événements\n" +
            "• Participants : Découvrez et inscrivez-vous\n" +
            "• Synchronisation automatique en temps réel\n\n" +
            "💡 Testez le mode démo pour explorer sans création de compte !");
        help.showAndWait();
    }
    
    private Alert createInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setPrefWidth(500);
        return alert;
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}