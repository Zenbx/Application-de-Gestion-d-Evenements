package com.gestion.evenements.serialization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.gestion.evenements.model.Evenement;
import com.gestion.evenements.auth.User;

/**
 * Gestionnaire de sérialisation pour les événements et utilisateurs
 * Supporte uniquement le format JSON avec sauvegarde automatique
 */
public class SerializationManager {
    private static final ObjectMapper objectMapper;
    private static final String DEFAULT_BACKUP_DIR = "backups";
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(
            com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
        );
        objectMapper.configure(
            com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT, true
        );
    }
    
    // ================================
    // MÉTHODES JSON
    // ================================
    
    /**
     * Sauvegarde les événements au format JSON avec écriture atomique et fusion des données existantes
     */
    public static void sauvegarderEvenementsJSON(Map<String, Evenement> evenements, String fichier) 
            throws IOException {
        try {
            // Charger les événements existants (s'il y en a)
            Map<String, Evenement> evenementsExistants = chargerEvenementsJSON(fichier);
            
            // Fusionner les nouvelles données avec les existantes
            evenementsExistants.putAll(evenements);
            
            // Créer une sauvegarde du fichier existant
            createBackupIfExists(fichier);
            
            File file = new File(fichier);
            File tempFile = new File(fichier + ".tmp");
            
            // Créer le répertoire parent si nécessaire
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            
            // Écrire la map fusionnée dans un fichier temporaire
            objectMapper.writerFor(new TypeReference<Map<String, Evenement>>(){})
                .withAttribute("type", "Evenement")
                .writeValue(tempFile, evenementsExistants);
            
            // Remplacer le fichier final de manière atomique
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("✅ Événements fusionnés et sauvegardés en JSON: " + fichier);
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur de traitement JSON: " + e.getMessage());
            throw new IOException("Échec de la sérialisation JSON", e);
        } catch (IOException e) {
            System.err.println("❌ Erreur d'entrée/sortie lors de l'écriture JSON: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Charge les événements depuis un fichier JSON
     */
    public static Map<String, Evenement> chargerEvenementsJSON(String fichier) throws IOException {
        File file = new File(fichier);
        if (!file.exists()) {
            System.out.println("⚠️ Fichier JSON non trouvé: " + fichier + " - Création d'une map vide");
            return new HashMap<>();
        }
        
        try {
            Map<String, Evenement> evenements = objectMapper.readValue(file, 
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Evenement.class));
            System.out.println("✅ " + evenements.size() + " événements chargés depuis JSON: " + fichier);
            return evenements;
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur de traitement JSON lors du chargement: " + e.getMessage());
            throw new IOException("Échec de la désérialisation JSON", e);
        } catch (IOException e) {
            System.err.println("❌ Erreur d'entrée/sortie lors de la lecture JSON: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Sauvegarde les utilisateurs au format JSON avec écriture atomique et fusion des données existantes
     */
    public static void sauvegarderUtilisateursJSON(Map<String, User> utilisateurs, String fichier) 
            throws IOException {
        try {
            // Charger les utilisateurs existants (s'il y en a)
            Map<String, User> utilisateursExistants = chargerUtilisateursJSON(fichier);
            
            // Fusionner les nouvelles données avec les existantes
            utilisateursExistants.putAll(utilisateurs);
            
            // Créer une sauvegarde du fichier existant
            createBackupIfExists(fichier);
            
            File file = new File(fichier);
            File tempFile = new File(fichier + ".tmp");
            
            // Créer le répertoire parent si nécessaire
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            
            // Écrire la map fusionnée dans un fichier temporaire
            objectMapper.writerFor(new TypeReference<Map<String, User>>(){})
                .withAttribute("type", "User")
                .writeValue(tempFile, utilisateursExistants);
            
            // Remplacer le fichier final de manière atomique
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("✅ Utilisateurs fusionnés et sauvegardés en JSON: " + fichier);
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur de traitement JSON: " + e.getMessage());
            throw new IOException("Échec de la sérialisation JSON", e);
        } catch (IOException e) {
            System.err.println("❌ Erreur d'entrée/sortie lors de l'écriture JSON: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Charge les utilisateurs depuis un fichier JSON
     */
    public static Map<String, User> chargerUtilisateursJSON(String fichier) throws IOException {
        File file = new File(fichier);
        if (!file.exists()) {
            System.out.println("⚠️ Fichier JSON non trouvé: " + fichier + " - Création d'une map vide");
            return new HashMap<>();
        }
        
        try {
            Map<String, User> utilisateurs = objectMapper.readValue(file, 
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, User.class));
            System.out.println("✅ " + utilisateurs.size() + " utilisateurs chargés depuis JSON: " + fichier);
            return utilisateurs;
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur de traitement JSON lors du chargement: " + e.getMessage());
            throw new IOException("Échec de la désérialisation JSON", e);
        } catch (IOException e) {
            System.err.println("❌ Erreur d'entrée/sortie lors de la lecture JSON: " + e.getMessage());
            throw e;
        }
    }
    
    // ================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ================================
    
    private static void createBackupIfExists(String fichier) {
        File file = new File(fichier);
        if (file.exists()) {
            try {
                // Créer le répertoire de sauvegarde s'il n'existe pas
                File backupDir = new File(DEFAULT_BACKUP_DIR);
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }
                
                // Créer le nom de sauvegarde avec timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String backupName = file.getName().replaceFirst("\\.", "_backup_" + timestamp + ".");
                File backupFile = new File(backupDir, backupName);
                
                // Copier le fichier
                Files.copy(file.toPath(), backupFile.toPath());
                System.out.println("📁 Sauvegarde JSON créée: " + backupFile.getPath());
            } catch (IOException e) {
                System.err.println("⚠️ Impossible de créer la sauvegarde JSON: " + e.getMessage());
            }
        }
    }
}