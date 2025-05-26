package testsysteme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.gestion.evenements.model.GestionEvenements;
import com.gestion.evenements.model.Participant;
import com.gestion.evenements.model.evenementparticulier.Conference;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceTest {

    @Test
    @DisplayName("Test performance ajout multiple participants")
    void testPerformanceAjoutParticipants() throws Exception {
        Conference grandeConference = new Conference("BIG001", "Grande Conférence", 
            LocalDateTime.now().plusDays(30), "Grand Centre", 10000, "Performance");

        long startTime = System.currentTimeMillis();
        
        // Ajouter 1000 participants
        for (int i = 0; i < 1000; i++) {
            Participant p = new Participant("P" + i, "Participant " + i, "p" + i + "@test.com");
            grandeConference.ajouterParticipant(p);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertEquals(1000, grandeConference.getParticipants().size());
        assertTrue(duration < 5000, "L'ajout de 1000 participants devrait prendre moins de 5 secondes");
        
        System.out.println("Temps d'ajout de 1000 participants: " + duration + "ms");
    }

    @Test
    @DisplayName("Test performance recherche avec Streams")
    void testPerformanceRecherche() throws Exception {
        GestionEvenements gestion = GestionEvenements.getInstance();
        gestion.getEvenements().clear();
        
        // Ajouter 100 événements
        for (int i = 0; i < 100; i++) {
            Conference conf = new Conference("CONF" + i, "Conference " + i, 
                LocalDateTime.now().plusDays(i), "Lieu " + (i % 10), 100, "Theme " + i);
            gestion.ajouterEvenement(conf);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Effectuer 100 recherches
        for (int i = 0; i < 100; i++) {
            gestion.rechercherParLieu("Lieu " + (i % 10));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 1000, "100 recherches devraient prendre moins de 1 seconde");
        
        System.out.println("Temps de 100 recherches: " + duration + "ms");
    }
}


