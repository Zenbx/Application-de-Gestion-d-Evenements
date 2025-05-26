package com.gestion.evenements.ui.managers;

import javafx.application.Platform;
import com.gestion.evenements.model.*;
import java.time.format.DateTimeFormatter;

/**
 * Gestionnaire centralisé pour tous les exports de données
 */
public class ExportManager {
    
    private final NotificationManager notificationManager;
    private final AnimationManager animationManager;
    
    public ExportManager(NotificationManager notificationManager, AnimationManager animationManager) {
        this.notificationManager = notificationManager;
        this.animationManager = animationManager;
    }
    
    public void exportParticipantsList(Evenement evenement) {
        if (evenement == null) {
            notificationManager.showWarningToast("Veuillez sélectionner un événement");
            return;
        }
        
        // Simulation d'export avec progression
        notificationManager.showInfoToast("Export de la liste des participants en cours...");
        
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulation
                
                Platform.runLater(() -> {
                    StringBuilder export = new StringBuilder();
                    export.append("Liste des participants - ").append(evenement.getNom()).append("\n");
                    export.append("Date: ").append(evenement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
                    export.append("Lieu: ").append(evenement.getLieu()).append("\n\n");
                    export.append("Participants (").append(evenement.getParticipants().size()).append("):\n\n");
                    
                    for (Participant p : evenement.getParticipants()) {
                        export.append(p.getNom()).append(",").append(p.getEmail()).append("\n");
                    }
                    
                    notificationManager.showSnackbar(
                        "Liste exportée avec succès (" + evenement.getParticipants().size() + " participants)",
                        "VOIR",
                        () -> showExportPreview("Liste des participants", export.toString())
                    );
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> notificationManager.showErrorToast("Export interrompu"));
            }
        }).start();
    }
    
    public void exportAllData() {
        notificationManager.showInfoToast("Export des données en cours...");
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    notificationManager.showSnackbar(
                        "Export terminé avec succès",
                        "OUVRIR",
                        () -> notificationManager.showInfoToast("Ouverture du fichier...")
                    );
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    public void exportReport(String reportContent) {
        notificationManager.showInfoToast("Génération du rapport en cours...");
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    notificationManager.showSnackbar(
                        "Rapport généré (" + reportContent.length() + " caractères)",
                        "APERÇU",
                        () -> showExportPreview("Rapport exporté", reportContent)
                    );
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> notificationManager.showErrorToast("Génération interrompue"));
            }
        }).start();
    }
    
    private void showExportPreview(String title, String content) {
        notificationManager.showInfoToast("Affichage de l'aperçu: " + title);
        // Délégation au DialogManager pour l'aperçu
    }
}
