Pour l'instant l'application fait pratiquement tout ce qui est demandé, mais quelques erreurs sont encore à déplorer mais seront gérés 


# 📅 Application de Gestion d'Événements JavaFX

Cette application JavaFX permet de gérer des événements à travers 6 interfaces distinctes :

- 👤 **ParticipantApp** : pour les participants souhaitant s'inscrire à des événements.
- 🧑‍💼 **OrganisateurApp** : pour les organisateurs qui créent ou modifient des événements.
- 🛠 **GestionEvenementsApp** : pour la gestion administrative des événements existants.
- **LoginView** : pour la connexion à son compte dans l'application
- **RegisterView** : pour l'inscription sur l'application
- **WelcomeView** : qui est l'interface de démarrage 
---

## 🛠 Fonctionnalités principales

### 🎟 ParticipantApp
- Inscription à des événements.
- Affichage d’un message de confirmation (`Alert`) après inscription.
- Affichage d’un **toast** temporaire en bas de l’écran après action, de **dialogs** et de **notifications** 

### 🧑‍💼 OrganisateurApp
- Création et modification d'événements.
- Confirmation via boîte de dialogue (`Alert`) après succès.
- Notification par **toast**, de **dialogs** et de **notifications** en cas de mise à jour ou ajout

### 🛠 GestionEvenementsApp
- Consultation, modification ou suppression d'événements.
- Alertes informatives (`Alert`) pour confirmer les actions.
- Affichage de **toasts visuels**, de **dialogs** et de **notifications** pour les utilisateurs.

### ApplicationLauncher 
- C'est le Main de l'application ce qu'il faut compiler pour avoir accès

Dans la première version du code, il était possible de compiler chaque interface, cette fois ci, il faut se connecter en tant qu'utilisateur pour avoir accès

### ModernNotificationUtils
- Il s'agit d'une classe utilitaire pour les interfaces graphique qui me permet de personnaliser mes messages d'alertes, les notifications, les toasts et les dialogs
- Je l'appelle dans pratiquement toutes mes interfaces

### User
- Cette classe me permet de gérer les utilisateurs

### UserRole
- Gestion des rôles d'un utilisateur

### AuthenticationService
- Service d'authentification

### Les Controllers pour les interfacesEvenementController, OrganisateurController et ParticipantController
- Ce sont les controllers pour les interfaces pour les lier avec les classes du package **model**

### Les Classes Exceptions CapaciteMaxiAtteinteException, EvenementDejaExistantException, ParticipantNonTrouveException
- Des exceptions personnalisés pour des besoins spécifiques

### SerializationManager 
- Il s'agit de la classe qui gère tout ce qui est sérialization et déserialization

### Les Observers EvenementObservable, ParticipantObserver, et UIObserver
- Ce sont respectivement les observers pour les évènements , les participants et pour les interfaces

### DataSynchronizer
- C'est la classe qui gère toute la synchrinisation dans l'application
---

## 💻 Technologies utilisées

- **Java 11+**
- **JavaFX** (Scène, StackPane, Alert, Timeline…)
- **CSS personnalisé** : Material Design
- **Maven** ou exécution manuelle
- Design Pattern Observer

---

## 📷 Aperçu 

L’application affiche différentes interfaces selon le rôle de l'utilisateur. Les interfaces sont composées de boutons, champs de texte, et affichent des feedbacks utilisateur en temps réel grâce à des dialogs (`Alert`) et des toasts visuels.

Dans cette application, Nous devons donc gérer des évenements dans cette application, j"ai dans un premier temps créer les interfaces GestionEvenementApp, OrganisateurApp et ParticipantApp qui sont les interfaces respectives pour un Administrateur, un Organisateur et un Participant, ensuite j'ai créé LoginView et RegisterView et apres j'ai crée WelcomeView, puis j'ai lié le tout

Pour une bonne visualition des choses j'ai mis des Evènments et Participant par défaut qui s'affichent quelque soit l'utilisateur.

Chaque évènement lors de sa création est enregistré dans un fichier au format **JSON** et un autre au format **XML** ce qui permet la pesistance de ceux ci, j'en ai fais de même avec les utilisateurs 




Dans cette application, j'ai ajouté assez de fonctionnalités comme des rapports pour les organisateurs d'évènements mais certaines focntionnalités sont encore à implémenter et j'ai mis un message dans l"application lorsque un utilisatuer veut les utiliser, on lui fait comprendre qu'elle doivent être implémenter , certaines interfaces aussi ne sont pas encore au point, mais nétant pa sle bu réelle de l'application je n'ai jugéé nécéssaire de m'attarder 

Dans le projet, Vous constaterez , dans le packge **ui** 3 packages **factories**, **managers** et **controllers** , ceux ci contiennent des classes qui sont censés gérer plusieurs choses, comme les animations graphiques, les Notifications graphiques, les Dialogs, les Ex^portation de rapports , et faire certaines gestion, mais celle ci ne sont pas encore utiliser dans les classes pour les interfaces , ceux pourquoi le code source de celles-ci est assez long

Dans un futur proche, j'aimerais mettre sur pied une sorte de Template pour les interfaces en utilisant mon fichier CSS modernStyle.css et ces autres classes pour faciliter de futures projets , comme je l'ai fait ici , étant donné que ce CSS est un fichier que j'améliore de puis un certain temps déjà, même chose pour les autres classes d'interfaces   


---

## ▶️ Lancer l’application

Assure-toi d’avoir JavaFX installé et configuré dans ton IDE ou via Maven.

### Compilation manuelle :
```bash
javac *.java
java ApplicationLauncher.java    
