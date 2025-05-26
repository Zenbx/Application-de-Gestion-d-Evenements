package com.gestion.evenements.ui.controllers;

import com.gestion.evenements.model.Organisateur;
import com.gestion.evenements.ui.managers.AnimationManager;
import com.gestion.evenements.ui.managers.ViewManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Contrôleur pour la navigation et le layout principal
 */
public class NavigationController {
    
    private final ViewManager viewManager;
    private final AnimationManager animationManager;
    private Button selectedButton;
    private ScrollPane contentScrollPane;
    private String currentView = "dashboard";
    
    public NavigationController(ViewManager viewManager, AnimationManager animationManager) {
        this.viewManager = viewManager;
        this.animationManager = animationManager;
    }
    
    public ScrollPane createSidebar(Organisateur organizer) {
        VBox sideBar = new VBox();
        sideBar.getStyleClass().add("sidebar");
        sideBar.setPrefWidth(280);
        sideBar.setSpacing(8);
        sideBar.setPadding(new Insets(24, 0, 24, 0));
        
        // Header, profil, boutons de navigation
        VBox header = createHeader();
        VBox profile = createOrganizerProfile(organizer);
        VBox navigation = createNavigationButtons();
        VBox settingsButtons = createSettingsButtons();
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        sideBar.getChildren().addAll(
            header,
            new Separator(),
            profile,
            new Separator(),
            navigation,
            spacer,
            new Separator(),
            settingsButtons
        );
        
        ScrollPane scrollPane = new ScrollPane(sideBar);
        scrollPane.getStyleClass().add("sidebar-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefWidth(280);
        
        return scrollPane;
    }
    
    public ScrollPane createContentArea() {
        BorderPane contentArea = new BorderPane();
        contentArea.getStyleClass().add("content-area");
        
        contentScrollPane = new ScrollPane(contentArea);
        contentScrollPane.getStyleClass().add("content-scroll");
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(false);
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        return contentScrollPane;
    }
    
    public void showDashboard() {
        switchView(() -> viewManager.createDashboardView());
    }
    
    public void showMyEvents() {
        switchView(() -> viewManager.createMyEventsView());
    }
    
    public void showCreateEvent() {
        switchView(() -> viewManager.createEventFormView());
    }
    
    public void showParticipants() {
        switchView(() -> viewManager.createParticipantsView());
    }
    
    public void showReports() {
        switchView(() -> viewManager.createReportsView());
    }
    
    private void switchView(ViewSupplier viewSupplier) {
        VBox newView = viewSupplier.get();
        
        BorderPane contentArea = (BorderPane) contentScrollPane.getContent();
        contentArea.setCenter(newView);
        
        // Animation de transition
        animationManager.fadeIn(newView, javafx.util.Duration.millis(300));
        animationManager.scrollToTop(contentScrollPane);
    }
    
    private VBox createNavigationButtons() {
        VBox navigation = new VBox();
        navigation.setSpacing(8);
        
        Button btnDashboard = createNavButton("📊", "Tableau de bord", "nav-button");
        Button btnMyEvents = createNavButton("📅", "Mes événements", "nav-button");
        Button btnCreateEvent = createNavButton("➕", "Créer un événement", "nav-button");
        Button btnParticipants = createNavButton("👥", "Participants", "nav-button");
        Button btnReports = createNavButton("📈", "Rapports", "nav-button");
        
        // Configuration des actions
        btnDashboard.setOnAction(e -> { setSelectedButton(btnDashboard); showDashboard(); });
        btnMyEvents.setOnAction(e -> { setSelectedButton(btnMyEvents); showMyEvents(); });
        btnCreateEvent.setOnAction(e -> { setSelectedButton(btnCreateEvent); showCreateEvent(); });
        btnParticipants.setOnAction(e -> { setSelectedButton(btnParticipants); showParticipants(); });
        btnReports.setOnAction(e -> { setSelectedButton(btnReports); showReports(); });
        
        // Sélection par défaut
        selectedButton = btnDashboard;
        btnDashboard.getStyleClass().add("nav-button-selected");
        
        navigation.getChildren().addAll(btnDashboard, btnMyEvents, btnCreateEvent, btnParticipants, btnReports);
        return navigation;
    }
    
    private Button createNavButton(String icon, String text, String styleClass) {
        Button button = new Button();
        button.getStyleClass().add(styleClass);
        button.setPrefWidth(240);
        button.setMaxWidth(Double.MAX_VALUE);
        
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
    
    private void setSelectedButton(Button button) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("nav-button-selected");
        }
        selectedButton = button;
        button.getStyleClass().add("nav-button-selected");
        
        // Animation de sélection
        animationManager.highlightNode(button);
    }
    
    private VBox createHeader() {
        // Implémentation du header
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(12);
        
        Label appIcon = new Label("🎯");
        appIcon.getStyleClass().add("app-icon");
        
        Label appTitle = new Label("Espace Organisateur");
        appTitle.getStyleClass().add("app-title");
        
        header.getChildren().addAll(appIcon, appTitle);
        return header;
    }
    
    private VBox createOrganizerProfile(Organisateur organizer) {
        // Implémentation du profil
        VBox profile = new VBox();
        profile.setAlignment(Pos.CENTER);
        profile.setSpacing(8);
        profile.getStyleClass().add("organizer-card");
        
        Label avatar = new Label("👨‍💼");
        avatar.setStyle("-fx-font-size: 32px;");
        
        Label name = new Label(organizer.getNom());
        name.getStyleClass().add("organizer-name");
        
        profile.getChildren().addAll(avatar, name);
        return profile;
    }
    
    private VBox createSettingsButtons() {
        // Implémentation des boutons de paramètres
        VBox settings = new VBox();
        settings.setSpacing(8);
        
        Button btnSettings = createNavButton("⚙️", "Paramètres", "nav-button-secondary");
        Button btnLogout = createNavButton("🚪", "Déconnexion", "nav-button-secondary");
        
        settings.getChildren().addAll(btnSettings, btnLogout);
        return settings;
    }
    
    @FunctionalInterface
    private interface ViewSupplier {
        VBox get();
    }
    
    public void refreshCurrentView() {
    try {
        switch (currentView) {
            case "dashboard": showDashboard(); break;
            case "events": showMyEvents(); break;
            case "create": showCreateEvent(); break;
            case "participants": showParticipants(); break;
            case "reports": showReports(); break;
            default: showDashboard();
        }
        System.out.println("✅ Vue rafraîchie: " + currentView);
    } catch (Exception e) {
        System.err.println("❌ Erreur rafraîchissement: " + e.getMessage());
    }
}
}