package com.gestion.evenements.ui.managers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import com.gestion.evenements.model.*;

/**
 * Gestionnaire centralisé pour tous les dialogs et modales
 */
public class DialogManager {
    
    private final NotificationManager notificationManager;
    private final AnimationManager animationManager;
    
    public DialogManager(NotificationManager notificationManager, AnimationManager animationManager) {
        this.notificationManager = notificationManager;
        this.animationManager = animationManager;
    }
    
    public Stage createCustomDialog(String title, VBox content, boolean modal) {
        Stage dialog = new Stage();
        if (modal) {
            dialog.initModality(Modality.APPLICATION_MODAL);
        }
        dialog.setTitle(title);
        dialog.setResizable(false);
        
        // Animation d'ouverture
        animationManager.scaleIn(content, javafx.util.Duration.millis(200));
        
        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        dialog.setScene(scene);
        
        return dialog;
    }
    
    public void showWelcomeDialog(Organisateur organizer) {
        VBox content = createWelcomeContent(organizer);
        Stage dialog = createCustomDialog("Bienvenue", content, false);
        dialog.show();
    }
    
    public void showEventDialog(Evenement existingEvent, EventDialogCallback callback) {
        VBox content = createEventDialogContent(existingEvent, callback);
        Stage dialog = createCustomDialog(
            existingEvent == null ? "Créer un événement" : "Modifier l'événement", 
            content, 
            true
        );
        dialog.show();
    }
    
    public Stage createProgressDialog(String title, String message) {
        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(16);
        content.setPadding(new Insets(30));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(50, 50);
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("dialog-message");
        
        content.getChildren().addAll(titleLabel, progress, messageLabel);
        
        return createCustomDialog(title, content, true);
    }
    
    private VBox createWelcomeContent(Organisateur organizer) {
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        
        Label icon = new Label("🎉");
        icon.setStyle("-fx-font-size: 48px;");
        
        Label welcome = new Label("Bienvenue " + organizer.getNom() + " !");
        welcome.getStyleClass().add("welcome-title");
        
        Label subtitle = new Label("Votre espace organisateur est prêt");
        subtitle.getStyleClass().add("welcome-subtitle");
        
        Button startBtn = new Button("Commencer");
        startBtn.getStyleClass().add("primary-button");
        startBtn.setOnAction(e -> {
            ((Stage) startBtn.getScene().getWindow()).close();
            notificationManager.showSuccessToast("Prêt à organiser de superbes événements !");
        });
        
        content.getChildren().addAll(icon, welcome, subtitle, startBtn);
        return content;
    }
    
    private VBox createEventDialogContent(Evenement existingEvent, EventDialogCallback callback) {
        VBox content = new VBox();
        content.setSpacing(16);
        content.setPadding(new Insets(20));
        content.setMaxWidth(500);
        
        // Formulaire de création/modification d'événement
        // ... (code du formulaire existant simplifié)
        
        return content;
    }
    
    // Interface pour les callbacks des dialogs
    @FunctionalInterface
    public interface EventDialogCallback {
        void onEventSaved(Evenement event);
    }
}