package testsysteme;

import com.gestion.evenements.model.*;
import com.gestion.evenements.model.evenementparticulier.*;
import com.gestion.evenements.observer.*;
import com.gestion.evenements.util.DataSynchronizer;
import com.gestion.evenements.exception.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Classe de test pour vérifier la connexion entre Front-end et Back-end
 * via le pattern Observer et la synchronisation temps réel
 */
public class SystemConnectionTest {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    
    private DataSynchronizer dataSynchronizer;
    private GestionEvenements gestionEvenements;
    private AtomicInteger notificationCount;
    private AtomicBoolean testPassed;
    
    public static void main(String[] args) {
        SystemConnectionTest test = new SystemConnectionTest();
        test.runAllTests();
    }
    
    public SystemConnectionTest() {
        this.dataSynchronizer = DataSynchronizer.getInstance();
        this.gestionEvenements = GestionEvenements.getInstance();
        this.notificationCount = new AtomicInteger(0);
        this.testPassed = new AtomicBoolean(true);
    }
    
    public void runAllTests() {
        printHeader("TESTS DE CONNEXION FRONT-END / BACK-END");
        
        try {
            // Test 1: Initialisation du système
            testSystemInitialization();
            
            // Test 2: Pattern Observer basic
            testObserverPattern();
            
            // Test 3: Synchronisation UI
            testUIObserverIntegration();
            
            // Test 4: Synchronisation temps réel
            testRealTimeSync();
            
            // Test 5: Gestion des participants
            testParticipantManagement();
            
            // Test 6: Gestion des organisateurs
            testOrganizerManagement();
            
            // Test 7: DataSynchronizer
            testDataSynchronizer();
            
            // Test 8: Stress test
            testConcurrentOperations();
            
            // Résumé final
            printFinalResults();
            
        } catch (Exception e) {
            printError("Erreur fatale pendant les tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testSystemInitialization() {
        printTestHeader("Test 1: Initialisation du Système");
        
        try {
            // Vérifier l'état initial
            DataSynchronizer.SystemStats initialStats = dataSynchronizer.getSystemStats();
            
            printInfo("État initial du système:");
            printInfo("  • Événements: " + initialStats.getTotalEvents());
            printInfo("  • Participants: " + initialStats.getTotalParticipants());
            printInfo("  • Inscriptions: " + initialStats.getTotalInscriptions());
            
            // Vérifier que les données de demo sont chargées
            assert initialStats.getTotalEvents() > 0 : "Aucun événement chargé";
            assert initialStats.getTotalParticipants() > 0 : "Aucun participant chargé";
            
            printSuccess("✅ Système initialisé correctement");
            
        } catch (Exception e) {
            printError("❌ Échec initialisation: " + e.getMessage());
            testPassed.set(false);
        }
    }
    
    private void testObserverPattern() {
        printTestHeader("Test 2: Pattern Observer Basique");
        
        try {
            // Créer un observer de test
            TestObserver testObserver = new TestObserver();
            
            // Créer un événement
            Conference conference = new Conference(
                "TEST_CONF_001",
                "Conférence Test Observer",
                LocalDateTime.now().plusDays(10),
                "Salle Test",
                50,
                "Test du pattern Observer"
            );
            
            // Ajouter l'observer
            conference.ajouterObservateur(testObserver);
            
            // Créer un participant
            Participant participant = new Participant("TEST_PART_001", "Test User", "test@email.com");
            
            // Ajouter le participant (doit déclencher l'observer)
            conference.ajouterParticipant(participant);
            
            // Vérifier que l'observer a été notifié
            assert testObserver.getNotificationCount() > 0 : "Observer non notifié";
            assert testObserver.getLastMessage().contains("Nouveau participant ajouté") : "Message incorrect";
            
            printSuccess("✅ Pattern Observer fonctionne correctement");
            printInfo("  • Notifications reçues: " + testObserver.getNotificationCount());
            printInfo("  • Dernier message: " + testObserver.getLastMessage());
            
        } catch (Exception e) {
            printError("❌ Échec pattern Observer: " + e.getMessage());
            testPassed.set(false);
        }
    }
    
    private void testUIObserverIntegration() {
        printTestHeader("Test 3: Intégration UIObserver");
        
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean uiUpdated = new AtomicBoolean(false);
            
            // Créer un UIObserver de test
            UIObserver uiObserver = new UIObserver(() -> {
                uiUpdated.set(true);
                latch.countDown();
                printInfo("  • Callback UI exécuté ✅");
            });
            
            // Créer un événement
            Concert concert = new Concert(
                "TEST_CONCERT_001",
                "Concert Test UI",
                LocalDateTime.now().plusDays(20),
                "Salle Concert",
                100,
                "Artiste Test",
                "Rock"
            );
            
            // Ajouter l'UIObserver
            concert.ajouterObservateur(uiObserver);
            
            // Ajouter l'événement au système
            gestionEvenements.ajouterEvenement(concert);
            
            // Ajouter un participant pour déclencher les notifications
            Participant participant = new Participant("TEST_PART_UI", "UI Test User", "ui.test@email.com");
            concert.ajouterParticipant(participant);
            
            // Attendre la mise à jour UI (avec timeout)
            boolean updated = latch.await(2, TimeUnit.SECONDS);
            
            assert updated : "UI non mise à jour dans les temps";
            assert uiUpdated.get() : "Callback UI non exécuté";
            
            printSuccess("✅ UIObserver intégré correctement");
            
        } catch (Exception e) {
            printError("❌ Échec intégration UIObserver: " + e.getMessage());
            testPassed.set(false);
        }
    }
    
    private void testRealTimeSync() {
        printTestHeader("Test 4: Synchronisation Temps Réel");
        
        try {
            // Créer plusieurs observers pour simuler plusieurs interfaces
            TestObserver adminObserver = new TestObserver("Admin");
            TestObserver organizerObserver = new TestObserver("Organizer");
            TestObserver participantObserver = new TestObserver("Participant");
            
            // Créer un événement
            Conference conference = new Conference(
                "TEST_SYNC_001",
                "Test Synchronisation",
                LocalDateTime.now().plusDays(5),
                "Centre Sync",
                30,
                "Synchronisation temps réel"
            );
            
            // Ajouter tous les observers
            conference.ajouterObservateur(adminObserver);
            conference.ajouterObservateur(organizerObserver);
            conference.ajouterObservateur(participantObserver);
            
            // Effectuer plusieurs opérations
            Participant p1 = new Participant("SYNC_P1", "User 1", "user1@sync.com");
            Participant p2 = new Participant("SYNC_P2", "User 2", "user2@sync.com");
            
            conference.ajouterParticipant(p1);
            conference.ajouterParticipant(p2);
            conference.retirerParticipant(p1);
            
            // Vérifier que tous les observers ont été notifiés
            assert adminObserver.getNotificationCount() >= 3 : "Admin observer manque notifications";
            assert organizerObserver.getNotificationCount() >= 3 : "Organizer observer manque notifications";
            assert participantObserver.getNotificationCount() >= 3 : "Participant observer manque notifications";
            
            printSuccess("✅ Synchronisation temps réel fonctionnelle");
            printInfo("  • Admin notifications: " + adminObserver.getNotificationCount());
            printInfo("  • Organizer notifications: " + organizerObserver.getNotificationCount());
            printInfo("  • Participant notifications: " + participantObserver.getNotificationCount());
            
        } catch (Exception e) {
            printError("❌ Échec synchronisation temps réel: " + e.getMessage());
            testPassed.set(false);
        }
    }
    
    private void testParticipantManagement() {
        printTestHeader("Test 5: Gestion des Participants");
        
        try {
            // Obtenir les stats avant
            DataSynchronizer.SystemStats statsBefore = dataSynchronizer.getSystemStats();
            
            // Créer un événement
            Conference conference = new Conference(
                "TEST_PART_MGT",
                "Test Gestion Participants",
                LocalDateTime.now().plusDays(15),
                "Salle Gestion",
                5, // Petite capacité pour tester les limites
                "Gestion des participants"
            );
            
            gestionEvenements.ajouterEvenement(conference);
            
            // Ajouter des participants jusqu'à la limite
            for (int i = 1; i <= 5; i++) {
                Participant p = new Participant("PART_MGT_" + i, "Participant " + i, "part" + i + "@test.com");
                conference.ajouterParticipant(p);
            }
            
            // Vérifier la capacité max
            assert conference.getParticipants().size() == 5 : "Nombre participants incorrect";
            
            // Tenter d'ajouter un participant de trop (doit lever exception)
            boolean exceptionThrown = false;
            try {
                Participant overflow = new Participant("OVERFLOW", "Overflow User", "overflow@test.com");
                conference.ajouterParticipant(overflow);
            } catch (CapaciteMaxAtteinteException e) {
                exceptionThrown = true;
                printInfo("  • Exception capacité max correctement levée");
            }
            
            assert exceptionThrown : "Exception capacité max non levée";
            
            // Retirer un participant
            Participant toRemove = conference.getParticipants().get(0);
            conference.retirerParticipant(toRemove);
            
            assert conference.getParticipants().size() == 4 : "Participant non retiré";
            
            printSuccess("✅ Gestion des participants fonctionnelle");
            printInfo("  • Participants finaux: " + conference.getParticipants().size() + "/5");
            
        } catch (Exception e) {
            printError("❌ Échec gestion participants: " + e.getMessage());
            testPassed.set(false);
        }
    }
    
    private void testOrganizerManagement() {
        printTestHeader("Test 6: Gestion des Organisateurs");
        
        try {
            // Créer un organisateur
            Organisateur organizer = new Organisateur("ORG_TEST", "Test Organizer", "organizer@test.com");
            
            // Créer des événements
            Conference conf = new Conference("ORG_CONF", "Conf Organisateur", 
                LocalDateTime.now().plusDays(25), "Lieu Org", 40, "Test organisateur");
            Concert concert = new Concert("ORG_CONCERT", "Concert Organisateur", 
                LocalDateTime.now().plusDays(30), "Salle Org", 60, "Artiste Org", "Jazz");
            
            // Assigner les événements
            organizer.organiserEvenement(conf);
            organizer.organiserEvenement(concert);
            
            // Vérifications
            assert organizer.getEvenementsOrganises().size() == 2 : "Événements non assignés";
            assert organizer.getEvenementsOrganises().contains(conf) : "Conférence non assignée";
            assert organizer.getEvenementsOrganises().contains(concert) : "Concert non assigné";
            
            // Ajouter des participants aux événements
            Participant p1 = new Participant("ORG_P1", "Participant Org 1", "orgp1@test.com");
            Participant p2 = new Participant("ORG_P2", "Participant Org 2", "orgp2@test.com");
            
            conf.ajouterParticipant(p1);
            concert.ajouterParticipant(p2);
            
            // Vérifier les participants
            assert conf.getParticipants().size() == 1 : "Participant conférence non ajouté";
            assert concert.getParticipants().size() == 1 : "Participant concert non ajouté";
            
            printSuccess("✅ Gestion des organisateurs fonctionnelle");
            printInfo("  • Événements organisés: " + organizer.getEvenementsOrganises().size());
            printInfo("  • Total participants: " + 
                     organizer.getEvenementsOrganises().stream()
                             .mapToInt(e -> e.getParticipants().size())
                             .sum());
            
        } catch (Exception e) {
            printError("❌ Échec gestion organisateurs: " + e.getMessage());
            testPassed.set(false);
        }
    }
    
    private void testDataSynchronizer() {
        printTestHeader("Test 7: DataSynchronizer");
        
        try {
            // Tester les statistiques
            DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
            
            assert stats.getTotalEvents() >= 0 : "Nombre événements invalide";
            assert stats.getTotalParticipants() >= 0 : "Nombre participants invalide";
            assert stats.getTotalInscriptions() >= 0 : "Nombre inscriptions invalide";
            
            // Tester la création d'événement via DataSynchronizer
            int eventsBefore = stats.getTotalEvents();
            
            Evenement newEvent = dataSynchronizer.createEventWithParticipants(
                "conference",
                "Test DataSynchronizer",
                LocalDateTime.now().plusDays(45),
                "Lieu DataSync",
                25,
                "sync1@test.com",
                "sync2@test.com"
            );
            
            // Vérifier la création
            assert newEvent != null : "Événement non créé";
            assert newEvent.getParticipants().size() == 2 : "Participants non ajoutés automatiquement";
            
            // Vérifier les stats mises à jour
            DataSynchronizer.SystemStats newStats = dataSynchronizer.getSystemStats();
            assert newStats.getTotalEvents() == eventsBefore + 1 : "Statistiques non mises à jour";
            
            // Tester la recherche
            var foundEvents = dataSynchronizer.searchEventsByName("Test DataSynchronizer");
            assert !foundEvents.isEmpty() : "Recherche événement échouée";
            assert foundEvents.get(0).getNom().equals("Test DataSynchronizer") : "Événement incorrect trouvé";
            
            printSuccess("✅ DataSynchronizer fonctionnel");
            printInfo("  • Événement créé avec " + newEvent.getParticipants().size() + " participants");
            printInfo("  • Recherche fonctionnelle");
            
        } catch (Exception e) {
            printError("❌ Échec DataSynchronizer: " + e.getMessage());
            testPassed.set(false);
        }
    }
    
    private void testConcurrentOperations() {
        printTestHeader("Test 8: Opérations Concurrentes (Stress Test)");
        
        try {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(3);
            AtomicInteger totalNotifications = new AtomicInteger(0);
            
            // Créer un événement pour le stress test
            Conference stressConf = new Conference(
                "STRESS_TEST",
                "Test Stress",
                LocalDateTime.now().plusDays(60),
                "Salle Stress",
                100,
                "Test de stress"
            );
            
            // Observer pour compter les notifications
            ParticipantObserver stressObserver = message -> totalNotifications.incrementAndGet();
            stressConf.ajouterObservateur(stressObserver);
            
            // Thread 1: Ajouter des participants
            Thread addThread = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 10; i++) {
                        Participant p = new Participant("STRESS_ADD_" + i, "Stress User " + i, "stress" + i + "@test.com");
                        stressConf.ajouterParticipant(p);
                        Thread.sleep(10); // Petite pause
                    }
                } catch (Exception e) {
                    printError("Erreur thread ajout: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
            
            // Thread 2: Modifier l'événement
            Thread modifyThread = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 5; i++) {
                        stressConf.setNom("Test Stress Modifié " + i);
                        Thread.sleep(20);
                    }
                } catch (Exception e) {
                    printError("Erreur thread modification: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
            
            // Thread 3: Ajouter/retirer des participants
            Thread toggleThread = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 5; i++) {
                        Participant p = new Participant("TOGGLE_" + i, "Toggle User " + i, "toggle" + i + "@test.com");
                        stressConf.ajouterParticipant(p);
                        Thread.sleep(15);
                        stressConf.retirerParticipant(p);
                        Thread.sleep(15);
                    }
                } catch (Exception e) {
                    printError("Erreur thread toggle: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
            
            // Démarrer tous les threads
            addThread.start();
            modifyThread.start();
            toggleThread.start();
            
            printInfo("  • Démarrage opérations concurrentes...");
            startLatch.countDown(); // Signal de démarrage
            
            // Attendre la fin de tous les threads
            boolean finished = endLatch.await(10, TimeUnit.SECONDS);
            assert finished : "Threads non terminés dans les temps";
            
            // Vérifier l'état final
            assert stressConf.getParticipants().size() == 10 : "État final participants incorrect";
            assert totalNotifications.get() > 0 : "Aucune notification pendant stress test";
            
            printSuccess("✅ Stress test réussi");
            printInfo("  • Participants finaux: " + stressConf.getParticipants().size());
            printInfo("  • Notifications totales: " + totalNotifications.get());
            
        } catch (Exception e) {
            printError("❌ Échec stress test: " + e.getMessage());
            testPassed.set(false);
        }
    }
    
    private void printFinalResults() {
        printHeader("RÉSULTATS FINAUX DES TESTS");
        
        if (testPassed.get()) {
            printSuccess("🎉 TOUS LES TESTS SONT PASSÉS AVEC SUCCÈS !");
            printSuccess("✅ Connexion Front-end/Back-end fonctionnelle");
            printSuccess("✅ Pattern Observer opérationnel");
            printSuccess("✅ Synchronisation temps réel active");
            printSuccess("✅ Intégration UI validée");
        } else {
            printError("❌ CERTAINS TESTS ONT ÉCHOUÉ");
            printError("Vérifiez les messages d'erreur ci-dessus");
        }
        
        // Statistiques finales
        DataSynchronizer.SystemStats finalStats = dataSynchronizer.getSystemStats();
        printInfo("\n📊 ÉTAT FINAL DU SYSTÈME:");
        printInfo("  • Événements totaux: " + finalStats.getTotalEvents());
        printInfo("  • Participants uniques: " + finalStats.getTotalParticipants());
        printInfo("  • Inscriptions totales: " + finalStats.getTotalInscriptions());
        printInfo("  • Événements à venir: " + finalStats.getFutureEvents());
        
        printHeader("FIN DES TESTS - SYSTÈME PRÊT POUR UTILISATION");
    }
    
    // Classe d'observer de test
    private static class TestObserver implements ParticipantObserver {
        private int notificationCount = 0;
        private String lastMessage = "";
        private final String name;
        
        public TestObserver() {
            this.name = "Test";
        }
        
        public TestObserver(String name) {
            this.name = name;
        }
        
        @Override
        public void notifier(String message) {
            notificationCount++;
            lastMessage = message;
            System.out.println(ANSI_PURPLE + "  🔔 [" + name + "] Notification #" + notificationCount + ": " + message + ANSI_RESET);
        }
        
        public int getNotificationCount() { return notificationCount; }
        public String getLastMessage() { return lastMessage; }
    }
    
    // Méthodes utilitaires d'affichage
    private void printHeader(String title) {
        System.out.println("\n" + ANSI_BLUE + "=".repeat(60) + ANSI_RESET);
        System.out.println(ANSI_BLUE + " " + title + ANSI_RESET);
        System.out.println(ANSI_BLUE + "=".repeat(60) + ANSI_RESET);
    }
    
    private void printTestHeader(String testName) {
        System.out.println("\n" + ANSI_YELLOW + "🧪 " + testName + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "-".repeat(40) + ANSI_RESET);
    }
    
    private void printSuccess(String message) {
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }
    
    private void printError(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }
    
    private void printInfo(String message) {
        System.out.println(message);
    }
}