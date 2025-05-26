// =================== NOTIFICATION MANAGER CORRIGÉ ===================
package com.gestion.evenements.ui.managers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Gestionnaire centralisé pour toutes les notifications et messages utilisateur
 * Version simplifiée et robuste
 */
public class NotificationManager {
    
    private Stage primaryStage;
    private boolean useConsoleLogging = true; // Fallback vers console si UI pas disponible
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.useConsoleLogging = false; // Activer les notifications UI si stage disponible
    }
    
    public void showSuccessToast(String message) {
        showNotification(message, "SUCCESS", "✅");
    }
    
    public void showErrorToast(String message) {
        showNotification(message, "ERROR", "❌");
    }
    
    public void showWarningToast(String message) {
        showNotification(message, "WARNING", "⚠️");
    }
    
    public void showInfoToast(String message) {
        showNotification(message, "INFO", "ℹ️");
    }
    
    private void showNotification(String message, String type, String icon) {
        // Toujours logger en console pour debug
        System.out.println("[" + type + "] " + icon + " " + message);
        
        // Si on a une interface disponible, afficher aussi un toast visuel simple
        if (!useConsoleLogging && primaryStage != null) {
            Platform.runLater(() -> {
                try {
                    showSimpleToast(message, type, icon);
                } catch (Exception e) {
                    // Si erreur UI, fallback vers console
                    System.err.println("Erreur affichage toast: " + e.getMessage());
                }
            });
        }
    }
    
    private void showSimpleToast(String message, String type, String icon) {
        // Créer un petit popup temporaire simple
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.UNDECORATED);
        toastStage.initModality(Modality.NONE);
        toastStage.setAlwaysOnTop(true);
        toastStage.setResizable(false);
        
        // Container du toast
        HBox toastContainer = new HBox();
        toastContainer.setAlignment(Pos.CENTER);
        toastContainer.setSpacing(10);
        toastContainer.setPadding(new Insets(12, 16, 12, 16));
        toastContainer.setMaxWidth(350);
        
        // Style selon le type
        String bgColor = "#333333"; // Par défaut
        switch (type) {
            case "SUCCESS":
                bgColor = "#4caf50";
                break;
            case "ERROR":
                bgColor = "#f44336";
                break;
            case "WARNING":
                bgColor = "#ff9800";
                break;
            case "INFO":
                bgColor = "#2196f3";
                break;
        }
        
        toastContainer.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 6px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );
        
        // Icône
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        
        // Message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-wrap-text: true;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);
        
        toastContainer.getChildren().addAll(iconLabel, messageLabel);
        
        // Scène
        javafx.scene.Scene toastScene = new javafx.scene.Scene(toastContainer);
        toastScene.setFill(null);
        toastStage.setScene(toastScene);
        
        // Position en haut à droite
        if (primaryStage != null) {
            toastStage.setX(primaryStage.getX() + primaryStage.getWidth() - 370);
            toastStage.setY(primaryStage.getY() + 50);
        }
        
        // Afficher et auto-fermer
        toastStage.show();
        
        // Auto-fermeture après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    if (toastStage.isShowing()) {
                        toastStage.close();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // Fermeture au clic
        toastContainer.setOnMouseClicked(e -> toastStage.close());
    }
    
    public boolean showConfirmation(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // Style moderne pour l'alert
            alert.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #ddd;" +
                "-fx-border-width: 1px;"
            );
            
            return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
        } catch (Exception e) {
            System.err.println("Erreur dialog confirmation: " + e.getMessage());
            return false;
        }
    }
    
    public void showInfo(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Erreur dialog info: " + e.getMessage());
            System.out.println("[INFO] " + title + ": " + message);
        }
    }
    
    public void showSnackbar(String message, String actionText, Runnable action) {
        // Version simplifiée de snackbar
        System.out.println("[SNACKBAR] " + message + " - Action: " + actionText);
        
        if (action != null && !useConsoleLogging) {
            // Afficher un dialog avec action
            Platform.runLater(() -> {
                try {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                    alert.setHeaderText(message);
                    alert.setContentText("Souhaitez-vous " + actionText + " ?");
                    
                    ButtonType actionButton = new ButtonType(actionText);
                    ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
                    
                    alert.getButtonTypes().setAll(actionButton, cancelButton);
                    
                    alert.showAndWait().ifPresent(response -> {
                        if (response == actionButton) {
                            action.run();
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Erreur snackbar: " + e.getMessage());
                    // Exécuter l'action quand même
                    action.run();
                }
            });
        }
    }
    
    // Méthodes de compatibilité pour éviter les erreurs
    public void showSuccessMessage(String message) {
        showSuccessToast(message);
    }
    
    public void showErrorMessage(String message) {
        showErrorToast(message);
    }
    
    public void showWarningMessage(String message) {
        showWarningToast(message);
    }
    
    public void showInfoMessage(String message) {
        showInfoToast(message);
    }
}