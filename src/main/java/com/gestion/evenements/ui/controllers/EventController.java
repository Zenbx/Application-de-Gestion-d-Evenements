package com.gestion.evenements.ui.controllers;

import com.gestion.evenements.model.*;
import com.gestion.evenements.model.evenementparticulier.*;
import com.gestion.evenements.util.DataSynchronizer;
import com.gestion.evenements.ui.managers.NotificationManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur pour la logique métier des événements
 */
public class EventController {
    
    private final DataSynchronizer dataSynchronizer;
    private final Organisateur currentOrganizer;
    private final NotificationManager notificationManager;
    
    public EventController(DataSynchronizer dataSynchronizer, Organisateur currentOrganizer, 
                          NotificationManager notificationManager) {
        this.dataSynchronizer = dataSynchronizer;
        this.currentOrganizer = currentOrganizer;
        this.notificationManager = notificationManager;
    }
    
    public void createEvent(String type, String nom, String lieu, int capacite, 
                           LocalDateTime date, String theme, String artiste, String genre) {
        try {
            String id = type.toUpperCase() + "_" + System.currentTimeMillis();
            
            Evenement evenement;
            if ("Conférence".equals(type)) {
                evenement = new Conference(id, nom, date, lieu, capacite, theme);
            } else {
                evenement = new Concert(id, nom, date, lieu, capacite, artiste, genre);
            }
            
            dataSynchronizer.ajouterEvenementAvecSync(evenement);
            currentOrganizer.organiserEvenement(evenement);
            
            notificationManager.showSuccessToast("Événement " + nom + " créé avec succès");
            
        } catch (Exception e) {
            notificationManager.showErrorToast("Erreur lors de la création: " + e.getMessage());
            throw e;
        }
    }
    
    public void updateEvent(Evenement evenement, String nom, String lieu, int capacite, 
                           LocalDateTime date, String theme, String artiste, String genre) {
        try {
            evenement.setNom(nom);
            evenement.setLieu(lieu);
            evenement.setCapaciteMax(capacite);
            evenement.setDate(date);
            
            if (evenement instanceof Conference && theme != null) {
                ((Conference) evenement).setTheme(theme);
            } else if (evenement instanceof Concert && artiste != null) {
                Concert concert = (Concert) evenement;
                concert.setArtiste(artiste);
                concert.setGenreMusical(genre);
            }
            
            notificationManager.showSuccessToast("Événement " + nom + " modifié avec succès");
            
        } catch (Exception e) {
            notificationManager.showErrorToast("Erreur lors de la modification: " + e.getMessage());
            throw e;
        }
    }
    
    public void cancelEvent(Evenement evenement) {
        boolean confirmed = notificationManager.showConfirmation(
            "Confirmer l'annulation",
            "Êtes-vous sûr de vouloir annuler \"" + evenement.getNom() + "\" ?"
        );
        
        if (confirmed) {
            evenement.annuler();
            currentOrganizer.getEvenementsOrganises().remove(evenement);
            dataSynchronizer.supprimerEvenementAvecSync(evenement.getId());
            
            notificationManager.showSnackbar(
                "Événement \"" + evenement.getNom() + "\" annulé",
                "RESTAURER",
                () -> restoreEvent(evenement)
            );
        }
    }
    
    private void restoreEvent(Evenement evenement) {
        currentOrganizer.getEvenementsOrganises().add(evenement);
        notificationManager.showSuccessToast("Événement restauré");
    }
    
    public java.util.List<Evenement> getOrganizerEvents() {
        return currentOrganizer.getEvenementsOrganises();
    }
    
    public java.util.List<Evenement> getActiveEvents() {
        return currentOrganizer.getEvenementsOrganises().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public java.util.List<Evenement> getPastEvents() {
        return currentOrganizer.getEvenementsOrganises().stream()
            .filter(e -> e.getDate().isBefore(LocalDateTime.now()))
            .collect(java.util.stream.Collectors.toList());
    }
}