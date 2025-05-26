package com.gestion.evenements.ui.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.gestion.evenements.model.*;
import com.gestion.evenements.ui.factories.ComponentFactory;
import com.gestion.evenements.ui.managers.ExportManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur pour la génération de rapports et statistiques
 */
public class ReportController {
    
    private final Organisateur currentOrganizer;
    
    public ReportController(Organisateur currentOrganizer) {
        this.currentOrganizer = currentOrganizer;
    }
    
    public HBox createGlobalStatsBar(ComponentFactory componentFactory) {
        HBox globalStats = new HBox();
        globalStats.getStyleClass().add("stats-bar");
        globalStats.setSpacing(24);
        
        int totalEvents = currentOrganizer.getEvenementsOrganises().size();
        int totalParticipants = currentOrganizer.getEvenementsOrganises().stream()
            .mapToInt(e -> e.getParticipants().size())
            .sum();
        double avgParticipants = totalEvents > 0 ? (double) totalParticipants / totalEvents : 0;
        int totalRevenue = totalParticipants * 50;
        
        VBox totalRevenueCard = componentFactory.createStatCard(totalRevenue + "€", "Revenus totaux", "success-stat");
        VBox avgParticipantsCard = componentFactory.createStatCard(String.format("%.1f", avgParticipants), "Participants moyens", "info-stat");
        VBox successRateCard = componentFactory.createStatCard("94%", "Taux de réussite", "positive-stat");
        VBox satisfactionCard = componentFactory.createStatCard("4.7/5", "Satisfaction", "total-stat");
        
        globalStats.getChildren().addAll(totalRevenueCard, avgParticipantsCard, successRateCard, satisfactionCard);
        return globalStats;
    }
    
    public VBox createChartsSection() {
        VBox chartsSection = new VBox();
        chartsSection.getStyleClass().add("events-list");
        chartsSection.setSpacing(16);
        
        Label chartsTitle = new Label("Analyses détaillées");
        chartsTitle.getStyleClass().add("section-title");
        
        VBox chartPlaceholder = new VBox();
        chartPlaceholder.setAlignment(Pos.CENTER);
        chartPlaceholder.setSpacing(16);
        chartPlaceholder.setPadding(new Insets(40));
        chartPlaceholder.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8px;");
        
        Label chartLabel = new Label("📊");
        chartLabel.setStyle("-fx-font-size: 48px;");
        
        Label chartText = new Label("Graphiques d'analyse basés sur vos données");
        chartText.getStyleClass().add("section-description");
        
        StringBuilder chartDesc = new StringBuilder();
        chartDesc.append("Données disponibles :\n");
        chartDesc.append("• ").append(currentOrganizer.getEvenementsOrganises().size()).append(" événements organisés\n");
        chartDesc.append("• Évolution des inscriptions par mois\n");
        chartDesc.append("• Taux de participation par type d'événement\n");
        chartDesc.append("• Analyse de satisfaction clients");
        
        Label chartDescLabel = new Label(chartDesc.toString());
        chartDescLabel.getStyleClass().add("text-secondary");
        
        chartPlaceholder.getChildren().addAll(chartLabel, chartText, chartDescLabel);
        chartsSection.getChildren().addAll(chartsTitle, chartPlaceholder);
        return chartsSection;
    }
    
    public VBox createDetailedReportSection(ExportManager exportManager) {
        VBox reportSection = new VBox();
        reportSection.getStyleClass().add("events-list");
        reportSection.setSpacing(16);
        
        Label reportTitle = new Label("Rapport détaillé");
        reportTitle.getStyleClass().add("section-title");
        
        String reportContent = generateDetailedReport();
        
        TextArea reportArea = new TextArea(reportContent);
        reportArea.setEditable(false);
        reportArea.setPrefRowCount(15);
        reportArea.getStyleClass().add("search-field");
        
        Button exportReportBtn = new Button("📊 Exporter le rapport");
        exportReportBtn.getStyleClass().add("primary-button");
        exportReportBtn.setOnAction(e -> exportManager.exportReport(reportContent));
        
        reportSection.getChildren().addAll(reportTitle, reportArea, exportReportBtn);
        return reportSection;
    }
    
    private String generateDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT DE PERFORMANCE ===\n\n");
        report.append("Organisateur: ").append(currentOrganizer.getNom()).append("\n");
        report.append("Email: ").append(currentOrganizer.getEmail()).append("\n\n");
        
        report.append("RÉSUMÉ DES ÉVÉNEMENTS:\n");
        report.append("Total événements organisés: ").append(currentOrganizer.getEvenementsOrganises().size()).append("\n");
        
        long futureEvents = currentOrganizer.getEvenementsOrganises().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .count();
        report.append("Événements à venir: ").append(futureEvents).append("\n");
        report.append("Événements passés: ").append(currentOrganizer.getEvenementsOrganises().size() - futureEvents).append("\n\n");
        
        int totalParticipants = currentOrganizer.getEvenementsOrganises().stream()
            .mapToInt(e -> e.getParticipants().size())
            .sum();
        report.append("PARTICIPANTS:\n");
        report.append("Total participants: ").append(totalParticipants).append("\n");
        
        if (!currentOrganizer.getEvenementsOrganises().isEmpty()) {
            double avgParticipants = (double) totalParticipants / currentOrganizer.getEvenementsOrganises().size();
            report.append("Moyenne par événement: ").append(String.format("%.1f", avgParticipants)).append("\n");
        }
        
        report.append("\nDÉTAIL PAR ÉVÉNEMENT:\n");
        for (Evenement event : currentOrganizer.getEvenementsOrganises()) {
            report.append("• ").append(event.getNom()).append(": ");
            report.append(event.getParticipants().size()).append("/").append(event.getCapaciteMax());
            report.append(" participants (").append(event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append(")\n");
        }
        
        return report.toString();
    }
}