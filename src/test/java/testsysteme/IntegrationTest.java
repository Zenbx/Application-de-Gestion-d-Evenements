package testsysteme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.gestion.evenements.model.Evenement;
import com.gestion.evenements.model.GestionEvenements;
import com.gestion.evenements.model.Organisateur;
import com.gestion.evenements.model.Participant;
import com.gestion.evenements.model.evenementparticulier.Concert;
import com.gestion.evenements.model.evenementparticulier.Conference;
import com.gestion.evenements.serialization.SerializationManager;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {

    @Test
    @DisplayName("Test scénario complet d'utilisation")
    void testScenarioComplet() throws Exception {
        GestionEvenements gestion = GestionEvenements.getInstance();
        gestion.getEvenements().clear();
        
        // 1. Créer un organisateur
        Organisateur organisateur = new Organisateur("ORG001", "Jean Organisateur", "jean@org.com");
        
        // 2. Créer des événements
        Conference conference = new Conference("CONF001", "AI Summit", 
            LocalDateTime.now().plusDays(30), "Paris", 100, "Intelligence Artificielle");
        Concert concert = new Concert("CONC001", "Rock Festival", 
            LocalDateTime.now().plusDays(15), "Lyon", 200, "AC/DC Tribute", "Rock");
        
        organisateur.organiserEvenement(conference);
        organisateur.organiserEvenement(concert);
        
        // 3. Ajouter les événements au système
        gestion.ajouterEvenement(conference);
        gestion.ajouterEvenement(concert);
        
        // 4. Créer des participants
        Participant alice = new Participant("P001", "Alice", "alice@test.com");
        Participant bob = new Participant("P002", "Bob", "bob@test.com");
        
        // 5. Inscrire aux notifications
        conference.ajouterObservateur(alice);
        conference.ajouterObservateur(bob);
        concert.ajouterObservateur(alice);
        
        // 6. Inscrire aux événements
        conference.ajouterParticipant(alice);
        conference.ajouterParticipant(bob);
        concert.ajouterParticipant(alice);
        
        // 7. Vérifications
        assertEquals(2, gestion.getEvenements().size());
        assertEquals(2, organisateur.getEvenementsOrganises().size());
        assertEquals(2, conference.getParticipants().size());
        assertEquals(1, concert.getParticipants().size());
        
        // 8. Test recherche
        var evenementsParis = gestion.rechercherParLieu("Paris");
        assertEquals(1, evenementsParis.size());
        assertEquals(conference, evenementsParis.get(0));
        
        // 9. Test sérialisation
        File tempFile = File.createTempFile("integration_test", ".json");
        SerializationManager.sauvegarderEvenementsJSON(gestion.getEvenements(), tempFile.getPath());
        
        Map<String, Evenement> evenementsCharges = SerializationManager.chargerEvenementsJSON(tempFile.getPath());
        assertEquals(2, evenementsCharges.size());
        
        // 10. Test annulation avec notifications
        conference.annuler();
        
        // Nettoyage
        tempFile.delete();
        
        System.out.println("✅ Scénario d'intégration complet réussi");
    }
}