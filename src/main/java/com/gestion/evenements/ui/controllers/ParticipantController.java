package com.gestion.evenements.ui.controllers;

import com.gestion.evenements.model.*;
import com.gestion.evenements.ui.managers.NotificationManager;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des participants
 */
public class ParticipantController {
    
    private final Organisateur currentOrganizer;
    private final NotificationManager notificationManager;
    
    public ParticipantController(Organisateur currentOrganizer, NotificationManager notificationManager) {
        this.currentOrganizer = currentOrganizer;
        this.notificationManager = notificationManager;
    }
    
    public List<Participant> getAllParticipants() {
        return currentOrganizer.getEvenementsOrganises().stream()
            .flatMap(e -> e.getParticipants().stream())
            .distinct()
            .collect(Collectors.toList());
    }
    
    public int getTotalParticipants() {
        return currentOrganizer.getEvenementsOrganises().stream()
            .mapToInt(e -> e.getParticipants().size())
            .sum();
    }
    
    public List<Participant> getParticipantsForEvent(Evenement event) {
        return event.getParticipants();
    }
    
    public void contactParticipant(Participant participant) {
        notificationManager.showInfoToast("Ouverture du dialog de contact pour: " + participant.getNom());
        // Délégation au DialogManager pour le dialog de contact
    }
    
    public void viewParticipantDetails(Participant participant) {
        notificationManager.showInfoToast("Affichage des détails de: " + participant.getNom());
        // Délégation au DialogManager pour les détails
    }
    
    public List<Evenement> getParticipantEvents(Participant participant) {
        return currentOrganizer.getEvenementsOrganises().stream()
            .filter(e -> e.getParticipants().contains(participant))
            .collect(Collectors.toList());
    }
}