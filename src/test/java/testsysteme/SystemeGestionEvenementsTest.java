package testsysteme;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.gestion.evenements.exception.CapaciteMaxAtteinteException;
import com.gestion.evenements.exception.EvenementDejaExistantException;
import com.gestion.evenements.model.Evenement;
import com.gestion.evenements.model.GestionEvenements;
import com.gestion.evenements.model.Intervenant;
import com.gestion.evenements.model.Organisateur;
import com.gestion.evenements.model.Participant;
import com.gestion.evenements.model.evenementparticulier.Concert;
import com.gestion.evenements.model.evenementparticulier.Conference;
import com.gestion.evenements.model.notification.EmailNotificationService;
import com.gestion.evenements.observer.ParticipantObserver;
import com.gestion.evenements.serialization.SerializationManager;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;          

// ======================== TESTS DE BASE ========================

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemeGestionEvenementsTest {

    private GestionEvenements gestion;
    private Conference conference;
    private Concert concert;
    private Participant participant1;
    private Participant participant2;

    @BeforeEach
    void setUp() {
        // Réinitialiser le singleton pour chaque test
        GestionEvenements.getInstance().getEvenements().clear();
        gestion = GestionEvenements.getInstance();
        
        conference = new Conference("CONF001", "Test Conference", 
            LocalDateTime.of(2025, 6, 15, 9, 0), "Test Location", 2, "Test Theme");
        
        concert = new Concert("CONC001", "Test Concert", 
            LocalDateTime.of(2025, 5, 30, 20, 0), "Test Venue", 3, "Test Artist", "Rock");
            
        participant1 = new Participant("P001", "Alice Test", "alice@test.com");
        participant2 = new Participant("P002", "Bob Test", "bob@test.com");
    }

    // ======================== TESTS DES ÉVÉNEMENTS ========================

    @Test
    @Order(1)
    @DisplayName("Test création et ajout d'événement")
    void testAjouterEvenement() throws EvenementDejaExistantException {
        assertDoesNotThrow(() -> gestion.ajouterEvenement(conference));
        assertEquals(1, gestion.getEvenements().size());
        assertEquals(conference, gestion.rechercherEvenement("CONF001"));
    }

    @Test
    @Order(2)
    @DisplayName("Test exception événement déjà existant")
    void testEvenementDejaExistantException() throws EvenementDejaExistantException {
        gestion.ajouterEvenement(conference);
        
        Conference duplicata = new Conference("CONF001", "Autre Conference", 
            LocalDateTime.now(), "Autre lieu", 50, "Autre thème");
            
        assertThrows(EvenementDejaExistantException.class, () -> {
            gestion.ajouterEvenement(duplicata);
        });
    }

    @Test
    @Order(3)
    @DisplayName("Test suppression d'événement")
    void testSupprimerEvenement() throws EvenementDejaExistantException {
        gestion.ajouterEvenement(conference);
        assertEquals(1, gestion.getEvenements().size());
        
        gestion.supprimerEvenement("CONF001");
        assertEquals(0, gestion.getEvenements().size());
        assertNull(gestion.rechercherEvenement("CONF001"));
    }

    // ======================== TESTS DES PARTICIPANTS ========================

    @Test
    @Order(4)
    @DisplayName("Test inscription d'un participant")
    void testAjouterParticipant() throws CapaciteMaxAtteinteException {
        assertDoesNotThrow(() -> conference.ajouterParticipant(participant1));
        assertEquals(1, conference.getParticipants().size());
        assertTrue(conference.getParticipants().contains(participant1));
    }

    @Test
    @Order(5)
    @DisplayName("Test désinscription d'un participant")
    void testRetirerParticipant() throws CapaciteMaxAtteinteException {
        conference.ajouterParticipant(participant1);
        assertEquals(1, conference.getParticipants().size());
        
        conference.retirerParticipant(participant1);
        assertEquals(0, conference.getParticipants().size());
        assertFalse(conference.getParticipants().contains(participant1));
    }

    @Test
    @Order(6)
    @DisplayName("Test exception capacité maximale atteinte")
    void testCapaciteMaxAtteinteException() throws CapaciteMaxAtteinteException {
        // La conférence a une capacité de 2
        conference.ajouterParticipant(participant1);
        conference.ajouterParticipant(participant2);
        
        Participant participant3 = new Participant("P003", "Charlie Test", "charlie@test.com");
        
        assertThrows(CapaciteMaxAtteinteException.class, () -> {
            conference.ajouterParticipant(participant3);
        });
    }

    // ======================== TESTS PATTERN OBSERVER ========================

    @Test
    @Order(7)
    @DisplayName("Test pattern Observer - notifications")
    void testObserverPattern() throws CapaciteMaxAtteinteException {
        // Création d'un observateur de test
        TestObserver testObserver = new TestObserver();
        conference.ajouterObservateur(testObserver);
        
        // Action qui devrait déclencher une notification
        conference.ajouterParticipant(participant1);
        
        // Vérification que l'observateur a été notifié
        assertTrue(testObserver.hasBeenNotified());
        assertNotNull(testObserver.getLastMessage());
        assertTrue(testObserver.getLastMessage().contains("Alice Test"));
    }

    @Test
    @Order(8)
    @DisplayName("Test retrait d'observateur")
    void testRetirerObservateur() throws CapaciteMaxAtteinteException {
        TestObserver testObserver = new TestObserver();
        conference.ajouterObservateur(testObserver);
        conference.retirerObservateur(testObserver);
        
        conference.ajouterParticipant(participant1);
        
        // L'observateur ne devrait pas être notifié
        assertFalse(testObserver.hasBeenNotified());
    }

    // ======================== TESTS STREAMS ET LAMBDAS ========================

    @Test
    @Order(9)
    @DisplayName("Test recherche par lieu avec Streams")
    void testRechercheParLieu() throws EvenementDejaExistantException {
        gestion.ajouterEvenement(conference);
        gestion.ajouterEvenement(concert);
        
        var evenementsTest = gestion.rechercherParLieu("Test");
        assertEquals(2, evenementsTest.size());
        
        var evenementsLocation = gestion.rechercherParLieu("Location");
        assertEquals(1, evenementsLocation.size());
        assertEquals(conference, evenementsLocation.get(0));
    }

    @Test
    @Order(10)
    @DisplayName("Test événements futurs avec Streams")
    void testEvenementsFuturs() throws EvenementDejaExistantException {
        // Créer un événement passé
        Conference evenementPasse = new Conference("PAST001", "Événement Passé", 
            LocalDateTime.of(2020, 1, 1, 10, 0), "Lieu passé", 50, "Thème passé");
        
        gestion.ajouterEvenement(conference);
        gestion.ajouterEvenement(concert);
        gestion.ajouterEvenement(evenementPasse);
        
        var evenementsFuturs = gestion.getEvenementsFuturs();
        assertEquals(2, evenementsFuturs.size());
        
        // Vérifier que les événements sont triés par date
        assertTrue(evenementsFuturs.get(0).getDate().isBefore(evenementsFuturs.get(1).getDate()));
    }

    // ======================== TESTS SÉRIALISATION ========================

    @Test
    @Order(11)
    @DisplayName("Test sérialisation et désérialisation JSON")
    void testSerialisationJSON(@TempDir File tempDir) throws Exception {
        // Préparer les données
        gestion.ajouterEvenement(conference);
        gestion.ajouterEvenement(concert);
        
        File fichierTest = new File(tempDir, "test_evenements.json");
        
        // Test sérialisation
        assertDoesNotThrow(() -> {
            SerializationManager.sauvegarderEvenementsJSON(gestion.getEvenements(), fichierTest.getPath());
        });
        
        assertTrue(fichierTest.exists());
        assertTrue(fichierTest.length() > 0);
        
        // Test désérialisation
        Map<String, Evenement> evenementsCharges = SerializationManager.chargerEvenementsJSON(fichierTest.getPath());
        
        assertEquals(2, evenementsCharges.size());
        assertTrue(evenementsCharges.containsKey("CONF001"));
        assertTrue(evenementsCharges.containsKey("CONC001"));
        
        // Vérifier les détails
        Evenement confChargee = evenementsCharges.get("CONF001");
        assertEquals("Test Conference", confChargee.getNom());
        assertEquals("Test Location", confChargee.getLieu());
    }

    // ======================== TESTS PROGRAMMATION ASYNCHRONE ========================

    @Test
    @Order(12)
    @DisplayName("Test notifications asynchrones")
    void testNotificationsAsynchrones() throws Exception {
        EmailNotificationService notifService = new EmailNotificationService();
        
        CompletableFuture<Void> future = notifService.envoyerNotificationAsync("Test message");
        
        // Vérifier que le CompletableFuture se termine correctement
        assertDoesNotThrow(() -> {
            future.get(3, TimeUnit.SECONDS);
        });
        
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    // ======================== TESTS CLASSES SPÉCIALISÉES ========================

    @Test
    @Order(13)
    @DisplayName("Test fonctionnalités spécifiques Conference")
    void testConference() {
        Intervenant intervenant1 = new Intervenant("Dr. Smith", "IA");
        Intervenant intervenant2 = new Intervenant("Prof. Jones", "ML");
        
        conference.getIntervenants().add(intervenant1);
        conference.getIntervenants().add(intervenant2);
        
        assertEquals(2, conference.getIntervenants().size());
        assertEquals("Test Theme", conference.getTheme());
        
        // Test affichage (vérifie qu'il n'y a pas d'exception)
        assertDoesNotThrow(() -> conference.afficherDetails());
    }

    @Test
    @Order(14)
    @DisplayName("Test fonctionnalités spécifiques Concert")
    void testConcert() {
        assertEquals("Test Artist", concert.getArtiste());
        assertEquals("Rock", concert.getGenreMusical());
        
        // Test affichage (vérifie qu'il n'y a pas d'exception)
        assertDoesNotThrow(() -> concert.afficherDetails());
    }

    @Test
    @Order(15)
    @DisplayName("Test Organisateur")
    void testOrganisateur() {
        Organisateur organisateur = new Organisateur("ORG001", "Admin Test", "admin@test.com");
        
        organisateur.organiserEvenement(conference);
        organisateur.organiserEvenement(concert);
        
        assertEquals(2, organisateur.getEvenementsOrganises().size());
        assertTrue(organisateur.getEvenementsOrganises().contains(conference));
        assertTrue(organisateur.getEvenementsOrganises().contains(concert));
    }

    // ======================== TESTS SINGLETON ========================

    @Test
    @Order(16)
    @DisplayName("Test pattern Singleton")
    void testSingleton() {
        GestionEvenements instance1 = GestionEvenements.getInstance();
        GestionEvenements instance2 = GestionEvenements.getInstance();
        
        assertSame(instance1, instance2);
    }


    @Test
@Order(17)
@DisplayName("Test scénario complet inscription/désinscription")
void testScenarioCompletInscriptionDesinscription() throws Exception {
    // 1. Créer un organisateur
    Organisateur organisateur = new Organisateur("ORG001", "Jean Organisateur", "jean@org.com");
    
    // 2. Créer des événements
    Conference grandeConference = new Conference("CONF002", "Grande Conférence", 
        LocalDateTime.now().plusDays(30), "Grand Centre", 100, "Grande Thématique");
    
    organisateur.organiserEvenement(grandeConference);
    gestion.ajouterEvenement(grandeConference);
    
    // 3. Créer plusieurs participants
    List<Participant> participants = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
        participants.add(new Participant("P" + i, "Participant " + i, "p" + i + "@test.com"));
    }
    
    // 4. Inscrire tous les participants
    for (Participant p : participants) {
        grandeConference.ajouterParticipant(p);
    }
    
    assertEquals(10, grandeConference.getParticipants().size());
    
    // 5. Désinscrire la moitié
    for (int i = 0; i < 5; i++) {
        grandeConference.retirerParticipant(participants.get(i));
    }
    
    assertEquals(5, grandeConference.getParticipants().size());
    
    // 6. Test sérialisation de l'état final
    File tempFile = File.createTempFile("integration_test", ".json");
    SerializationManager.sauvegarderEvenementsJSON(gestion.getEvenements(), tempFile.getPath());
    
    // 7. Test désérialisation
    Map<String, Evenement> evenementsCharges = SerializationManager.chargerEvenementsJSON(tempFile.getPath());
    Evenement confChargee = evenementsCharges.get("CONF002");
    
    assertEquals(5, confChargee.getParticipants().size());
    
    // Nettoyage
    tempFile.delete();
}

    // ======================== CLASSE D'AIDE POUR LES TESTS ========================

    private static class TestObserver implements ParticipantObserver {
        private boolean notified = false;
        private String lastMessage;

        @Override
        public void notifier(String message) {
            this.notified = true;
            this.lastMessage = message;
        }

        public boolean hasBeenNotified() {
            return notified;
        }

        public String getLastMessage() {
            return lastMessage;
        }
    }
}


