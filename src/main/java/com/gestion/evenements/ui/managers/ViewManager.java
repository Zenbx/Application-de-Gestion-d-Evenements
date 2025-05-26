package com.gestion.evenements.ui.managers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.gestion.evenements.ui.controllers.*;
import com.gestion.evenements.ui.factories.ComponentFactory;
import com.gestion.evenements.model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gestionnaire pour la création et gestion des différentes vues
 */
public class ViewManager {
    
    private final ComponentFactory componentFactory;
    private final EventController eventController;
    private final ParticipantController participantController;
    private final ReportController reportController;
    private final ExportManager exportManager;
    private final NotificationManager notificationManager;
    
    public ViewManager(ComponentFactory componentFactory, EventController eventController,
                      ParticipantController participantController, ReportController reportController,
                      ExportManager exportManager, NotificationManager notificationManager) {
        this.componentFactory = componentFactory;
        this.eventController = eventController;
        this.participantController = participantController;
        this.reportController = reportController;
        this.exportManager = exportManager;
        this.notificationManager = notificationManager;
    }
    
    public VBox createDashboardView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Statistiques rapides
        HBox statsBar = createStatsBar();
        
        // Actions rapides
        HBox quickActions = createQuickActions();
        
        // Événements récents
        VBox recentEvents = createRecentEventsSection();
        
        // Activité récente
        VBox recentActivity = createRecentActivitySection();
        
        view.getChildren().addAll(statsBar, quickActions, recentEvents, recentActivity);
        return view;
    }
    
    public VBox createMyEventsView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Barre d'actions
        HBox actionBar = createEventActionBar();
        
        // Filtres
        HBox filters = createEventFilters();
        
        // Liste des événements
        VBox eventsList = createEventsList();
        
        view.getChildren().addAll(actionBar, filters, eventsList);
        return view;
    }
    
    public VBox createEventFormView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        VBox form = componentFactory.createFormSection("Créer un nouvel événement");
        VBox formFields = createEventFormFields();
        
        form.getChildren().add(formFields);
        view.getChildren().add(form);
        return view;
    }
    
    public VBox createParticipantsView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Sélecteur d'événement
        HBox eventSelector = createEventSelector();
        
        // Statistiques des participants
        HBox participantStats = createParticipantStatsBar();
        
        // Table des participants
        VBox participantsTable = createParticipantsTable();
        
        view.getChildren().addAll(eventSelector, participantStats, participantsTable);
        return view;
    }
    
    public VBox createReportsView() {
        VBox view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Statistiques globales
        HBox globalStats = reportController.createGlobalStatsBar(componentFactory);
        
        // Section des graphiques
        VBox chartsSection = reportController.createChartsSection();
        
        // Rapport détaillé
        VBox detailedReport = reportController.createDetailedReportSection(exportManager);
        
        view.getChildren().addAll(globalStats, chartsSection, detailedReport);
        return view;
    }
    
    private HBox createStatsBar() {
        HBox statsBar = new HBox();
        statsBar.getStyleClass().add("stats-bar");
        statsBar.setSpacing(24);
        
        List<Evenement> events = eventController.getOrganizerEvents();
        int totalEvents = events.size();
        long activeEvents = eventController.getActiveEvents().size();
        int totalParticipants = events.stream().mapToInt(e -> e.getParticipants().size()).sum();
        int estimatedRevenue = totalParticipants * 50;
        
        VBox totalEventsCard = componentFactory.createStatCard(String.valueOf(totalEvents), "Événements créés", "total-stat");
        VBox activeEventsCard = componentFactory.createStatCard(String.valueOf(activeEvents), "Événements actifs", "positive-stat");
        VBox participantsCard = componentFactory.createStatCard(String.valueOf(totalParticipants), "Participants totaux", "info-stat");
        VBox revenueCard = componentFactory.createStatCard(estimatedRevenue + "€", "Revenus estimés", "success-stat");
        
        statsBar.getChildren().addAll(totalEventsCard, activeEventsCard, participantsCard, revenueCard);
        return statsBar;
    }
    
    private HBox createQuickActions() {
        HBox actions = new HBox();
        actions.setSpacing(16);
        actions.setPadding(new Insets(16, 0, 16, 0));
        
        Button createEventBtn = componentFactory.createStyledButton("➕ Nouvel Événement", "primary-button");
        createEventBtn.setOnAction(e -> showCreateEventDialog());
        
        Button viewParticipantsBtn = componentFactory.createStyledButton("👥 Voir Participants", "secondary-button");
        viewParticipantsBtn.setOnAction(e -> notificationManager.showInfoToast("Navigation vers participants"));
        
        Button exportBtn = componentFactory.createStyledButton("📊 Exporter Données", "secondary-button");
        exportBtn.setOnAction(e -> exportManager.exportAllData());
        
        actions.getChildren().addAll(createEventBtn, viewParticipantsBtn, exportBtn);
        return actions;
    }
    
    private VBox createRecentEventsSection() {
        VBox section = componentFactory.createFormSection("Mes événements récents");
        
        List<Evenement> recentEvents = eventController.getOrganizerEvents().stream()
            .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
        
        VBox eventsContainer = new VBox();
        eventsContainer.setSpacing(8);
        
        if (recentEvents.isEmpty()) {
            Label noEvents = new Label("Aucun événement créé. Commencez par créer votre premier événement !");
            noEvents.getStyleClass().add("text-secondary");
            eventsContainer.getChildren().add(noEvents);
        } else {
            for (Evenement event : recentEvents) {
                HBox eventCard = componentFactory.createEventCard(
                    event,
                    () -> eventController.updateEvent(event, event.getNom(), event.getLieu(), 
                                                    event.getCapaciteMax(), event.getDate(), "", "", ""),
                    () -> showEventDetails(event),
                    () -> eventController.cancelEvent(event)
                );
                eventsContainer.getChildren().add(eventCard);
            }
        }
        
        section.getChildren().add(eventsContainer);
        return section;
    }
    
    private VBox createRecentActivitySection() {
        VBox section = componentFactory.createFormSection("Activité récente");
        
        // Génération d'activités récentes
        VBox activityContainer = new VBox();
        activityContainer.setSpacing(8);
        
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
        
        section.getChildren().add(activityContainer);
        return section;
    }
    
    private HBox createEventActionBar() {
        HBox actionBar = new HBox();
        actionBar.setSpacing(16);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        Button createBtn = componentFactory.createStyledButton("➕ Nouvel Événement", "primary-button");
        createBtn.setOnAction(e -> showCreateEventDialog());
        
        Button templateBtn = componentFactory.createStyledButton("📄 Utiliser un modèle", "secondary-button");
        templateBtn.setOnAction(e -> showTemplates());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        TextField searchField = componentFactory.createStyledTextField("🔍 Rechercher un événement...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterEvents(newText));
        
        actionBar.getChildren().addAll(createBtn, templateBtn, spacer, searchField);
        return actionBar;
    }
    
    private HBox createEventFilters() {
        HBox filters = new HBox();
        filters.setSpacing(12);
        
        List<Evenement> events = eventController.getOrganizerEvents();
        long totalEvents = events.size();
        long activeEvents = eventController.getActiveEvents().size();
        long pastEvents = totalEvents - activeEvents;
        
        Button allBtn = componentFactory.createStyledButton("Tous (" + totalEvents + ")", "primary-button-small");
        Button activeBtn = componentFactory.createStyledButton("Actifs (" + activeEvents + ")", "secondary-button");
        Button pastBtn = componentFactory.createStyledButton("Terminés (" + pastEvents + ")", "secondary-button");
        
        filters.getChildren().addAll(allBtn, activeBtn, pastBtn);
        return filters;
    }
    
    private VBox createEventsList() {
        VBox eventsList = new VBox();
        eventsList.setSpacing(12);
        
        List<Evenement> events = eventController.getOrganizerEvents();
        
        if (events.isEmpty()) {
            VBox emptyState = createEmptyEventsState();
            eventsList.getChildren().add(emptyState);
        } else {
            for (Evenement event : events) {
                HBox eventCard = componentFactory.createEventCard(
                    event,
                    () -> editEvent(event),
                    () -> showEventDetails(event),
                    () -> eventController.cancelEvent(event)
                );
                eventsList.getChildren().add(eventCard);
            }
        }
        
        return eventsList;
    }
    
    private VBox createEventFormFields() {
        VBox formFields = new VBox();
        formFields.setSpacing(16);
        
        // Type d'événement
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Conférence", "Concert");
        typeCombo.setValue("Conférence");
        typeCombo.getStyleClass().add("search-field");
        
        // Champs basiques
        TextField nameField = componentFactory.createStyledTextField("Nom de l'événement");
        TextField locationField = componentFactory.createStyledTextField("Lieu");
        TextField capacityField = componentFactory.createStyledTextField("Capacité maximale");
        
        DatePicker datePicker = new DatePicker();
        TextField timeField = componentFactory.createStyledTextField("Heure (HH:mm)");
        
        // Champs spécifiques
        TextField themeField = componentFactory.createStyledTextField("Thème (pour conférence)");
        TextField artisteField = componentFactory.createStyledTextField("Artiste (pour concert)");
        TextField genreField = componentFactory.createStyledTextField("Genre musical (pour concert)");
        
        // Gestion de l'affichage des champs
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
        
        Button saveBtn = componentFactory.createStyledButton("💾 Enregistrer comme brouillon", "secondary-button");
        Button publishBtn = componentFactory.createStyledButton("🚀 Publier l'événement", "primary-button");
        Button clearBtn = componentFactory.createStyledButton("🔄 Effacer", "icon-button-danger");
        
        publishBtn.setOnAction(e -> publishEvent(typeCombo, nameField, locationField, capacityField, 
                                               datePicker, timeField, themeField, artisteField, genreField));
        
        actions.getChildren().addAll(saveBtn, publishBtn, clearBtn);
        
        formFields.getChildren().addAll(
            new Label("Type d'événement:"), typeCombo,
            new Label("Nom:"), nameField,
            new Label("Lieu:"), locationField,
            new Label("Capacité:"), capacityField,
            new Label("Date:"), datePicker,
            new Label("Heure:"), timeField,
            new Label("Détails spécifiques:"), themeField, artisteField, genreField,
            actions
        );
        
        return formFields;
    }
    
    private HBox createEventSelector() {
        HBox eventSelector = new HBox();
        eventSelector.setSpacing(16);
        eventSelector.setAlignment(Pos.CENTER_LEFT);
        
        Label eventLabel = new Label("Événement:");
        eventLabel.getStyleClass().add("section-title");
        
        ComboBox<Evenement> eventCombo = new ComboBox<>();
        eventCombo.getItems().addAll(eventController.getOrganizerEvents());
        if (!eventController.getOrganizerEvents().isEmpty()) {
            eventCombo.setValue(eventController.getOrganizerEvents().get(0));
        }
        eventCombo.getStyleClass().add("search-field");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportBtn = componentFactory.createStyledButton("📊 Exporter liste", "secondary-button");
        exportBtn.setOnAction(e -> exportManager.exportParticipantsList(eventCombo.getValue()));
        
        eventSelector.getChildren().addAll(eventLabel, eventCombo, spacer, exportBtn);
        return eventSelector;
    }
    
    private HBox createParticipantStatsBar() {
        HBox participantStats = new HBox();
        participantStats.getStyleClass().add("stats-bar");
        participantStats.setSpacing(24);
        
        int totalConfirmed = participantController.getTotalParticipants();
        double avgAge = 32.5; // Simulation
        int totalRevenue = totalConfirmed * 50;
        
        VBox confirmedStat = componentFactory.createStatCard(String.valueOf(totalConfirmed), "Participants confirmés", "positive-stat");
        VBox pendingStat = componentFactory.createStatCard("0", "En attente", "info-stat");
        VBox revenueStat = componentFactory.createStatCard(totalRevenue + "€", "Revenus générés", "success-stat");
        VBox averageAgeStat = componentFactory.createStatCard(String.format("%.1f", avgAge), "Âge moyen", "total-stat");
        
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
        
        List<Participant> allParticipants = participantController.getAllParticipants();
        
        if (allParticipants.isEmpty()) {
            Label noParticipants = new Label("Aucun participant inscrit pour le moment.");
            noParticipants.getStyleClass().add("text-secondary");
            noParticipants.setPadding(new Insets(20));
            rows.getChildren().add(noParticipants);
        } else {
            for (Participant participant : allParticipants) {
                HBox row = componentFactory.createParticipantRow(
                    participant,
                    () -> participantController.contactParticipant(participant),
                    () -> participantController.viewParticipantDetails(participant)
                );
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
        
        Button createFirstBtn = componentFactory.createStyledButton("Créer mon premier événement", "primary-button");
        createFirstBtn.setOnAction(e -> showCreateEventDialog());
        
        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyDescription, createFirstBtn);
        return emptyState;
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
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("event-date");
        
        info.getChildren().addAll(titleLabel, descLabel);
        
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("text-secondary");
        
        card.getChildren().addAll(iconLabel, info, timeLabel);
        return card;
    }
    
    // Méthodes d'action
    private void showCreateEventDialog() {
        notificationManager.showInfoToast("Ouverture du dialog de création d'événement");
        // Délégation au DialogManager
    }
    
    private void editEvent(Evenement event) {
        notificationManager.showInfoToast("Édition de l'événement: " + event.getNom());
        // Délégation au DialogManager
    }
    
    private void showEventDetails(Evenement event) {
        notificationManager.showInfoToast("Affichage des détails de: " + event.getNom());
        // Délégation au DialogManager
    }
    
    private void showTemplates() {
        notificationManager.showInfoToast("Affichage des modèles d'événements");
        // Délégation au DialogManager
    }
    
    private void filterEvents(String searchText) {
        if (!searchText.trim().isEmpty()) {
            notificationManager.showInfoToast("Recherche appliquée: " + searchText);
        }
    }
    
    private void publishEvent(ComboBox<String> typeCombo, TextField nameField, TextField locationField,
                             TextField capacityField, DatePicker datePicker, TextField timeField,
                             TextField themeField, TextField artisteField, TextField genreField) {
        try {
            if (nameField.getText().trim().isEmpty() || locationField.getText().trim().isEmpty() ||
                capacityField.getText().trim().isEmpty() || datePicker.getValue() == null || 
                timeField.getText().trim().isEmpty()) {
                notificationManager.showErrorToast("Veuillez remplir tous les champs obligatoires");
                return;
            }
            
            int capacite = Integer.parseInt(capacityField.getText());
            LocalDateTime dateTime = LocalDateTime.parse(
                datePicker.getValue().toString() + " " + timeField.getText(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            );
            
            eventController.createEvent(
                typeCombo.getValue(),
                nameField.getText(),
                locationField.getText(),
                capacite,
                dateTime,
                themeField.getText(),
                artisteField.getText(),
                genreField.getText()
            );
            
            // Effacer le formulaire après création
            clearForm(nameField, locationField, capacityField, timeField, 
                     themeField, artisteField, genreField);
                     
        } catch (Exception e) {
            notificationManager.showErrorToast("Erreur lors de la publication: " + e.getMessage());
        }
    }
    
    private void clearForm(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }
    
    private List<String> generateRecentActivities() {
        List<String> activities = new java.util.ArrayList<>();
        
        List<Evenement> events = eventController.getOrganizerEvents();
        for (Evenement event : events) {
            if (event.getParticipants().size() > 0) {
                activities.add("📝|Nouvelle inscription|" + 
                             event.getParticipants().get(event.getParticipants().size() - 1).getNom() + 
                             " s'est inscrit à " + event.getNom() + "|Il y a " + (Math.random() * 24) + "h");
            }
        }
        
        return activities.stream().limit(5).collect(java.util.stream.Collectors.toList());
    }
}
