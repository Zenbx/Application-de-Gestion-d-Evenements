package com.gestion.evenements.ui.factories;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.gestion.evenements.model.Evenement;
import com.gestion.evenements.model.Participant;

/**
 * Factory pour créer des composants UI réutilisables
 */
public class ComponentFactory {
    
    public VBox createStatCard(String value, String label, String styleClass) {
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
    
    public HBox createEventCard(Evenement evenement, Runnable onEdit, Runnable onView, Runnable onCancel) {
        HBox card = new HBox();
        card.getStyleClass().add("event-card");
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        VBox.setVgrow(info, Priority.ALWAYS);
        
        String icon = evenement instanceof com.gestion.evenements.model.evenementparticulier.Conference ? "🎤" : "🎵";
        Label titleLabel = new Label(icon + " " + evenement.getNom());
        titleLabel.getStyleClass().add("event-title");
        
        Label dateLabel = new Label(evenement.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                                   + " • " + evenement.getLieu());
        dateLabel.getStyleClass().add("event-date");
        
        Label participantsLabel = new Label(evenement.getParticipants().size() + "/" + 
                                          evenement.getCapaciteMax() + " participants");
        participantsLabel.getStyleClass().add("event-participants");
        
        info.getChildren().addAll(titleLabel, dateLabel, participantsLabel);
        
        HBox actions = new HBox();
        actions.setSpacing(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("✏️");
        editBtn.getStyleClass().add("icon-button");
        editBtn.setOnAction(e -> onEdit.run());
        
        Button viewBtn = new Button("👁️");
        viewBtn.getStyleClass().add("icon-button");
        viewBtn.setOnAction(e -> onView.run());
        
        actions.getChildren().addAll(editBtn, viewBtn);
        
        if (evenement.getDate().isAfter(java.time.LocalDateTime.now())) {
            Button cancelBtn = new Button("🗑️");
            cancelBtn.getStyleClass().add("icon-button-danger");
            cancelBtn.setOnAction(e -> onCancel.run());
            actions.getChildren().add(cancelBtn);
        }
        
        card.getChildren().addAll(info, actions);
        return card;
    }
    
    public HBox createParticipantRow(Participant participant, Runnable onContact, Runnable onView) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setSpacing(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        
        Label nameLabel = new Label(participant.getNom());
        nameLabel.setPrefWidth(200);
        
        Label emailLabel = new Label(participant.getEmail());
        emailLabel.setPrefWidth(250);
        emailLabel.getStyleClass().add("text-secondary");
        
        Label statusLabel = new Label("Confirmé");
        statusLabel.setPrefWidth(100);
        statusLabel.getStyleClass().add("status-active");
        
        HBox actions = new HBox();
        actions.setSpacing(4);
        actions.setPrefWidth(100);
        
        Button contactBtn = new Button("✉️");
        contactBtn.getStyleClass().add("icon-button");
        contactBtn.setOnAction(e -> onContact.run());
        
        Button viewBtn = new Button("👁️");
        viewBtn.getStyleClass().add("icon-button");
        viewBtn.setOnAction(e -> onView.run());
        
        actions.getChildren().addAll(contactBtn, viewBtn);
        
        row.getChildren().addAll(nameLabel, emailLabel, statusLabel, actions);
        return row;
    }
    
    public TextField createStyledTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.getStyleClass().add("search-field");
        return field;
    }
    
    public Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }
    
    public VBox createFormSection(String title) {
        VBox section = new VBox();
        section.getStyleClass().add("form-section");
        section.setSpacing(12);
        
        if (title != null) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("form-section-title");
            section.getChildren().add(titleLabel);
        }
        
        return section;
    }
}