package com.gestion.evenements.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import com.gestion.evenements.auth.AuthenticationService;
import com.gestion.evenements.auth.User;
import com.gestion.evenements.auth.UserRole;
import com.gestion.evenements.util.DataSynchronizer;

import javafx.scene.text.Font;

/**
 * Interface d'inscription moderne avec création réelle d'utilisateurs
 * Design épuré et professionnel pour la création de comptes connectés au système
 */
public class RegisterView extends Application {

    private VBox mainContainer;
    private ScrollPane scrollPane;
    private VBox formContainer;
    private TextField nomField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private ComboBox<UserRole> roleComboBox;
    private TextField organisationField;
    private HBox organisationContainer;
    private TextField telephoneField;
    private HBox acceptTermsCheckBox;
    private CheckBox checkBox;
    private Label statusLabel;
    private Button registerButton;
    private Button loginLinkButton;
    private String namePolice;
    private Font font;

    private AuthenticationService authService;
    private DataSynchronizer dataSynchronizer;

    @Override
    public void start(Stage primaryStage) {
        // Initialiser les services
        authService = AuthenticationService.getInstance();
        dataSynchronizer = DataSynchronizer.getInstance();
        
        primaryStage.setTitle("Créer un compte - EventPro");
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(700);
        
        createModernInterface();
        applyStyling();
        addSmoothAnimations();
        
        font = Font.loadFont("/com/gestion/evenements/ui/fonts/Poppins-SemiBold.ttf",12);

        //namePolice = font.getFamily();

        Scene scene = new Scene(scrollPane, 880, 720);
        scene.setFill(Color.web("#f8f9fa"));
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());

        
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    private void createModernInterface() {
        // Container principal avec scroll
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        //scrollPane.setStyle("-fx-font-family: '"+ namePolice +"';");
        scrollPane.getStyleClass().add("content-scroll");
        
        mainContainer = new VBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40, 20, 40, 20));
        mainContainer.setSpacing(0);
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");
        
        // Carte principale
        formContainer = new VBox();
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(800);
        formContainer.setSpacing(0);
        formContainer.setPadding(new Insets(48, 40, 36, 40));
        formContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 1, 0, 0, 1);"
        );
        
        // En-tête moderne
        VBox header = createGoogleStyleHeader();
        
        // Formulaire épuré
        VBox form = createCleanForm();
        
        // Actions
        VBox actions = createModernActions();
        
        formContainer.getChildren().addAll(header, form, actions);
        
        // Footer avec liens
        VBox footer = createFooterLinks();
        
        mainContainer.getChildren().addAll(formContainer, footer);
        scrollPane.setContent(mainContainer);
    }
    
    private VBox createGoogleStyleHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(8);
        header.setPadding(new Insets(0, 0, 32, 0));
        
        // Logo moderne
        Label logoLabel = new Label("EventPro");
        logoLabel.setStyle(
            "-fx-font-family: 'Google Sans', 'Roboto', sans-serif;" +
            "-fx-font-size: 24px;" +
            "-fx-font-weight: 400;" +
            "-fx-text-fill: #1a73e8;" +
            "-fx-letter-spacing: -0.5px;"
        );
        
        // Titre principal
        Label titleLabel = new Label("Créer votre compte");
        titleLabel.setStyle(
            "-fx-font-family: 'Google Sans', 'Roboto', sans-serif;" +
            "-fx-font-size: 24px;" +
            "-fx-font-weight: 400;" +
            "-fx-text-fill: #202124;" +
            "-fx-padding: 16 0 0 0;"
        );
        
        // Sous-titre
        Label subtitleLabel = new Label("pour continuer vers EventPro");
        subtitleLabel.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: 400;" +
            "-fx-text-fill: #5f6368;" +
            "-fx-padding: 0 0 0 0;"
        );
        
        header.getChildren().addAll(logoLabel, titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createCleanForm() {
        VBox form = new VBox();
        form.setSpacing(24);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(0, 0, 24, 0));
        
        // Type de compte
        VBox roleSection = createFieldSection("Type de compte", null, false);
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(UserRole.values());
        roleComboBox.setValue(UserRole.PARTICIPANT);
        styleModernComboBox(roleComboBox);
        roleComboBox.setOnAction(e -> updateFormBasedOnRole());
        roleSection.getChildren().add(roleComboBox);
        
        // Ligne 1: Nom et Email
        HBox row1 = new HBox(16);
        row1.setAlignment(Pos.CENTER_LEFT);
        
        VBox nomSection = createFieldSection("Nom complet", "Nom et prénom", true);
        nomField = createModernTextField("Entrez votre nom complet");
        nomSection.getChildren().add(nomField);
        
        VBox emailSection = createFieldSection("Email", "Adresse email", true);
        emailField = createModernTextField("votre.email@exemple.com");
        emailSection.getChildren().add(emailField);
        
        row1.getChildren().addAll(nomSection, emailSection);
        HBox.setHgrow(nomSection, Priority.ALWAYS);
        HBox.setHgrow(emailSection, Priority.ALWAYS);
        
        // Téléphone
        VBox telephoneSection = createFieldSection("Téléphone", "Numéro de téléphone", false);
        telephoneField = createModernTextField("+237 6 23 45 67 89");
        telephoneSection.getChildren().add(telephoneField);
        
        // Organisation (conditionnelle)
        organisationContainer = new HBox();
        VBox organisationSection = createFieldSection("Organisation", "Nom de votre organisation", false);
        organisationField = createModernTextField("Entrez le nom de votre organisation");
        organisationSection.getChildren().add(organisationField);
        organisationContainer.getChildren().add(organisationSection);
        organisationContainer.setVisible(false);
        organisationContainer.setManaged(false);
        HBox.setHgrow(organisationSection, Priority.ALWAYS);
        
        // Ligne 2: Mots de passe
        HBox row2 = new HBox(16);
        row2.setAlignment(Pos.CENTER_LEFT);
        
        VBox passwordSection = createFieldSection("Mot de passe", "Mot de passe", true);
        passwordField = createModernPasswordField("Entrez votre mot de passe");
        passwordSection.getChildren().add(passwordField);
        
        VBox confirmSection = createFieldSection("Confirmer", "Confirmation", true);
        confirmPasswordField = createModernPasswordField("Confirmez votre mot de passe");
        confirmSection.getChildren().add(confirmPasswordField);
        
        row2.getChildren().addAll(passwordSection, confirmSection);
        HBox.setHgrow(passwordSection, Priority.ALWAYS);
        HBox.setHgrow(confirmSection, Priority.ALWAYS);
        
        // Indicateur de force du mot de passe
        VBox passwordStrength = createPasswordStrengthIndicator();
        
        // Acceptation des conditions
        acceptTermsCheckBox = createModernCheckBox();
        VBox termsSection = new VBox(8);
        termsSection.getChildren().add(acceptTermsCheckBox);
        
        // Label de statut
        statusLabel = new Label();
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-background-radius: 4;" +
            "-fx-border-radius: 4;"
        );
        
        form.getChildren().addAll(
            roleSection,
            row1,
            telephoneSection,
            organisationContainer,
            row2,
            passwordStrength,
            termsSection,
            statusLabel
        );
        
        return form;
    }
    
    private VBox createFieldSection(String label, String description, boolean required) {
        VBox section = new VBox(8);
        section.setAlignment(Pos.CENTER_LEFT);
        
        Label fieldLabel = new Label(label + (required ? " *" : ""));
        fieldLabel.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: #202124;"
        );
        
        section.getChildren().add(fieldLabel);
        return section;
    }
    
    private TextField createModernTextField(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        styleModernField(field);
        return field;
    }
    
    private PasswordField createModernPasswordField(String placeholder) {
        PasswordField field = new PasswordField();
        field.setPromptText(placeholder);
        styleModernField(field);
        return field;
    }
    
    private void styleModernField(Control field) {
        field.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 12 16 12 16;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #dadce0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-text-fill: #202124;" +
            "-fx-prompt-text-fill: #5f6368;"
        );
        
        // Effets de focus
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-family: 'Roboto', sans-serif;" +
                    "-fx-font-size: 16px;" +
                    "-fx-padding: 12 16 12 16;" +
                    "-fx-background-color: white;" +
                    "-fx-border-color: #1a73e8;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 4;" +
                    "-fx-background-radius: 4;" +
                    "-fx-text-fill: #202124;" +
                    "-fx-prompt-text-fill: #5f6368;"
                );
            } else {
                field.setStyle(
                    "-fx-font-family: 'Roboto', sans-serif;" +
                    "-fx-font-size: 16px;" +
                    "-fx-padding: 12 16 12 16;" +
                    "-fx-background-color: white;" +
                    "-fx-border-color: #dadce0;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 4;" +
                    "-fx-background-radius: 4;" +
                    "-fx-text-fill: #202124;" +
                    "-fx-prompt-text-fill: #5f6368;"
                );
            }
        });
        
        field.setMaxWidth(Double.MAX_VALUE);
    }
    
    private void styleModernComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 12 16 12 16;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #dadce0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }
    
    private HBox createModernCheckBox() {
        checkBox = new CheckBox();
          checkBox.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #1a73e8;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: #000000;" +
            "-fx-cursor: hand;"
        );

        
        // Créer le texte avec lien
        Label termsText = new Label(" J'accepte les ");
        termsText.setStyle("-fx-font-family: 'Roboto', sans-serif; -fx-font-size: 14px; -fx-text-fill: #5f6368;");
        
        Label termsLink = new Label("conditions d'utilisation");
        termsLink.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #1a73e8;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-underline: true;" +
            "-fx-cursor: hand;"
        );
        termsLink.setOnMouseClicked(e -> showTermsDialog());
        
        Label andText = new Label(" et la ");
        andText.setStyle("-fx-font-family: 'Roboto', sans-serif; -fx-font-size: 14px; -fx-text-fill: #5f6368;");
        
        Label privacyLink = new Label("politique de confidentialité");
        privacyLink.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #1a73e8;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-underline: true;" +
            "-fx-cursor: hand;"
        );
        privacyLink.setOnMouseClicked(e -> showPrivacyDialog());
        
        HBox termsBox = new HBox(0);
        termsBox.setAlignment(Pos.CENTER_LEFT);
        termsBox.getChildren().addAll(checkBox, termsText, termsLink, andText, privacyLink);
        
        return termsBox;
    }
    
    private VBox createPasswordStrengthIndicator() {
        VBox container = new VBox(4);
        
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(320);
        strengthBar.setStyle(
            "-fx-accent: #ea4335;" +
            "-fx-background-color: #f1f3f4;" +
            "-fx-padding: 2;"
        );
        
        Label strengthLabel = new Label("Force du mot de passe: Faible");
        strengthLabel.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #ea4335;"
        );
        
        // Validation en temps réel
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            double strength = calculatePasswordStrength(newVal);
            strengthBar.setProgress(strength);
            
            if (strength < 0.3) {
                strengthBar.setStyle("-fx-accent: #ea4335; -fx-background-color: #f1f3f4;");
                strengthLabel.setText("Force du mot de passe: Faible");
                strengthLabel.setStyle("-fx-font-family: 'Roboto', sans-serif; -fx-font-size: 12px; -fx-text-fill: #ea4335;");
            } else if (strength < 0.7) {
                strengthBar.setStyle("-fx-accent: #fbbc04; -fx-background-color: #f1f3f4;");
                strengthLabel.setText("Force du mot de passe: Moyenne");
                strengthLabel.setStyle("-fx-font-family: 'Roboto', sans-serif; -fx-font-size: 12px; -fx-text-fill: #fbbc04;");
            } else {
                strengthBar.setStyle("-fx-accent: #34a853; -fx-background-color: #f1f3f4;");
                strengthLabel.setText("Force du mot de passe: Forte");
                strengthLabel.setStyle("-fx-font-family: 'Roboto', sans-serif; -fx-font-size: 12px; -fx-text-fill: #34a853;");
            }
        });
        
        container.getChildren().addAll(strengthBar, strengthLabel);
        return container;
    }
    
    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        
        double strength = 0;
        if (password.length() >= 8) strength += 0.25;
        if (password.matches(".*[a-z].*")) strength += 0.25;
        if (password.matches(".*[A-Z].*")) strength += 0.25;
        if (password.matches(".*[0-9].*")) strength += 0.125;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength += 0.125;
        
        return Math.min(strength, 1.0);
    }
    
    private VBox createModernActions() {
        VBox actions = new VBox();
        actions.setAlignment(Pos.CENTER);
        actions.setSpacing(16);
        actions.setPadding(new Insets(8, 0, 0, 0));
        
        registerButton = new Button("Créer un compte");
        registerButton.setStyle(
            "-fx-font-family: 'Google Sans', 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: #1a73e8;" +
            "-fx-background-radius: 4;" +
            "-fx-border-radius: 4;" +
            "-fx-padding: 12 24 12 24;" +
            "-fx-min-width: 120px;" +
            "-fx-cursor: hand;"
        );
        
        // Effet hover
        registerButton.setOnMouseEntered(e -> {
            registerButton.setStyle(
                "-fx-font-family: 'Google Sans', 'Roboto', sans-serif;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 500;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: #1557b0;" +
                "-fx-background-radius: 4;" +
                "-fx-border-radius: 4;" +
                "-fx-padding: 12 24 12 24;" +
                "-fx-min-width: 120px;" +
                "-fx-cursor: hand;"
            );
        });
        
        registerButton.setOnMouseExited(e -> {
            registerButton.setStyle(
                "-fx-font-family: 'Google Sans', 'Roboto', sans-serif;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 500;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: #1a73e8;" +
                "-fx-background-radius: 4;" +
                "-fx-border-radius: 4;" +
                "-fx-padding: 12 24 12 24;" +
                "-fx-min-width: 120px;" +
                "-fx-cursor: hand;"
            );
        });
        
        registerButton.setOnAction(e -> handleRegistration());
        
        actions.getChildren().add(registerButton);
        return actions;
    }
    
    private VBox createFooterLinks() {
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER);
        footer.setSpacing(16);
        footer.setPadding(new Insets(32, 0, 0, 0));
        
        HBox loginSection = new HBox(8);
        loginSection.setAlignment(Pos.CENTER);
        
        Label alreadyLabel = new Label("Vous avez déjà un compte ?");
        alreadyLabel.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #5f6368;"
        );
        
        loginLinkButton = new Button("Se connecter");
        loginLinkButton.setStyle(
            "-fx-font-family: 'Google Sans', 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: #1a73e8;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-cursor: hand;"
        );
        loginLinkButton.setOnAction(e -> navigateToLogin());
        
        loginSection.getChildren().addAll(alreadyLabel, loginLinkButton);
        
        // Lien invité
        Button guestButton = new Button("Continuer en tant qu'invité");
        guestButton.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #5f6368;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: #dadce0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        );
        guestButton.setOnAction(e -> accessAsGuest());
        
        footer.getChildren().addAll(loginSection, guestButton);
        return footer;
    }
    
    private void updateFormBasedOnRole() {
        UserRole selectedRole = roleComboBox.getValue();
        boolean isOrganizer = (selectedRole == UserRole.ORGANISATEUR);
        
        // Animation de transition
        if (isOrganizer) {
            organisationContainer.setVisible(true);
            organisationContainer.setManaged(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), organisationContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), organisationContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                organisationContainer.setVisible(false);
                organisationContainer.setManaged(false);
            });
            fadeOut.play();
        }
    }
    
    private void handleRegistration() {
        if (!validateForm()) {
            return;
        }
        
        // Désactiver le bouton pendant le traitement
        registerButton.setDisable(true);
        registerButton.setText("Création en cours...");
        
        try {
            // Créer l'utilisateur avec les données du formulaire
            User newUser = new User(
                generateUserId(),
                nomField.getText().trim(),
                emailField.getText().trim(),
                passwordField.getText(),
                roleComboBox.getValue(),
                telephoneField.getText().trim().isEmpty() ? null : telephoneField.getText().trim(),
                organisationField.getText().trim().isEmpty() ? null : organisationField.getText().trim()
            );
            
            // Enregistrer l'utilisateur dans le système d'authentification
            boolean success = authService.registerUser(newUser);
            
            if (success) {
                showSuccess("Compte créé avec succès pour " + newUser.getNom() + " !");
                
                // Log de la création
                System.out.println("✅ Nouveau compte créé:");
                System.out.println("   Nom: " + newUser.getNom());
                System.out.println("   Email: " + newUser.getEmail());
                System.out.println("   Rôle: " + newUser.getRole());
                System.out.println("   Organisation: " + (newUser.getOrganisation() != null ? newUser.getOrganisation() : "N/A"));
                
                // Redirection automatique vers la page de connexion
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // Laisser le temps de lire le message
                        javafx.application.Platform.runLater(() -> navigateToLogin());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                
            } else {
                showError("Cette adresse email est déjà utilisée.\n\nVeuillez choisir une autre adresse email ou vous connecter avec votre compte existant.");
                resetRegisterButton();
            }
            
        } catch (IllegalArgumentException e) {
            showError("Données invalides: " + e.getMessage());
            resetRegisterButton();
        } catch (Exception e) {
            showError("Erreur lors de la création du compte: " + e.getMessage());
            resetRegisterButton();
            e.printStackTrace();
        }
    }
    
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        // Validation du nom
        if (nomField.getText().trim().isEmpty()) {
            errors.append("• Le nom est obligatoire\n");
        } else if (nomField.getText().trim().length() < 2) {
            errors.append("• Le nom doit contenir au moins 2 caractères\n");
        }
        
        // Validation de l'email
        if (emailField.getText().trim().isEmpty()) {
            errors.append("• L'email est obligatoire\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("• Format d'email invalide\n");
        }
        
        // Validation du mot de passe
        if (passwordField.getText().isEmpty()) {
            errors.append("• Le mot de passe est obligatoire\n");
        } else if (passwordField.getText().length() < 6) {
            errors.append("• Le mot de passe doit contenir au moins 6 caractères\n");
        }
        
        // Validation de la confirmation du mot de passe
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errors.append("• Les mots de passe ne correspondent pas\n");
        }
        
        // Validation de l'organisation pour les organisateurs
        if (roleComboBox.getValue() == UserRole.ORGANISATEUR && 
            organisationField.getText().trim().isEmpty()) {
            errors.append("• L'organisation est obligatoire pour les organisateurs\n");
        }
        
        // Validation du téléphone (optionnel mais format si présent)
        if (!telephoneField.getText().trim().isEmpty() && 
            !isValidPhoneNumber(telephoneField.getText().trim())) {
            errors.append("• Format de téléphone invalide\n");
        }
        
        // Validation de l'acceptation des conditions
        if (!checkBox.isSelected()) {
            errors.append("• Vous devez accepter les conditions d'utilisation\n");
        }
        
        if (errors.length() > 0) {
            showError("Veuillez corriger les erreurs suivantes :\n\n" + errors.toString().trim());
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
    
    private boolean isValidPhoneNumber(String phone) {
        // Accepter différents formats de numéros
        return phone.matches("^[+]?[0-9\\s\\-\\(\\)]{8,15}$");
    }
    
    private String generateUserId() {
        return roleComboBox.getValue().toString().toLowerCase() + "_" + System.currentTimeMillis();
    }
    
    private void resetRegisterButton() {
        registerButton.setDisable(false);
        registerButton.setText("Créer un compte");
    }
    
    private void navigateToLogin() {
        try {
            Stage currentStage = (Stage) formContainer.getScene().getWindow();
            currentStage.close();
            
            Stage loginStage = new Stage();
            LoginView loginView = new LoginView();
            loginView.start(loginStage);
        } catch (Exception ex) {
            showError("Erreur lors de l'ouverture de la connexion: " + ex.getMessage());
        }
    }
    
    private void accessAsGuest() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accès invité");
        confirm.setHeaderText("Continuer en tant qu'invité");
        confirm.setContentText("En tant qu'invité, vous aurez un accès limité aux fonctionnalités.\n\n" +
                              "Vous pourrez :\n" +
                              "• Consulter les événements disponibles\n" +
                              "• Voir les détails des événements\n\n" +
                              "Vous ne pourrez pas :\n" +
                              "• Vous inscrire à des événements\n" +
                              "• Sauvegarder vos préférences\n" +
                              "• Accéder à l'historique\n\n" +
                              "Voulez-vous continuer ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Stage currentStage = (Stage) formContainer.getScene().getWindow();
                    currentStage.close();
                    
                    Stage participantStage = new Stage();
                    ParticipantApp participantApp = new ParticipantApp();
                    participantApp.setCurrentUser(null); // Mode invité
                    participantApp.start(participantStage);
                    
                    System.out.println("✅ Accès invité accordé");
                    
                } catch (Exception e) {
                    showError("Erreur lors de l'accès invité: " + e.getMessage());
                }
            }
        });
    }
    
    private void showTermsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Conditions d'utilisation");
        alert.setHeaderText("Conditions d'utilisation d'EventPro");
        alert.setContentText(
            "CONDITIONS GÉNÉRALES D'UTILISATION\n\n" +
            "1. ACCEPTATION DES CONDITIONS\n" +
            "En utilisant EventPro, vous acceptez les présentes conditions.\n\n" +
            "2. DESCRIPTION DU SERVICE\n" +
            "EventPro est une plateforme de gestion d'événements permettant aux organisateurs " +
            "de créer des événements et aux participants de s'y inscrire.\n\n" +
            "3. COMPTES UTILISATEUR\n" +
            "• Vous devez fournir des informations exactes lors de l'inscription\n" +
            "• Vous êtes responsable de la sécurité de votre compte\n" +
            "• Un seul compte par personne est autorisé\n\n" +
            "4. UTILISATION ACCEPTABLE\n" +
            "Vous vous engagez à utiliser le service de manière légale et respectueuse.\n\n" +
            "5. DONNÉES PERSONNELLES\n" +
            "Vos données sont traitées conformément à notre politique de confidentialité.\n\n" +
            "6. MODIFICATION DES CONDITIONS\n" +
            "Nous nous réservons le droit de modifier ces conditions à tout moment."
        );
        alert.showAndWait();
    }
    
    private void showPrivacyDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Politique de confidentialité");
        alert.setHeaderText("Politique de confidentialité d'EventPro");
        alert.setContentText(
            "POLITIQUE DE CONFIDENTIALITÉ\n\n" +
            "1. COLLECTE DES DONNÉES\n" +
            "Nous collectons les informations que vous nous fournissez lors de l'inscription :\n" +
            "• Nom et prénom\n" +
            "• Adresse email\n" +
            "• Numéro de téléphone (optionnel)\n" +
            "• Organisation (pour les organisateurs)\n\n" +
            "2. UTILISATION DES DONNÉES\n" +
            "Vos données sont utilisées pour :\n" +
            "• Gérer votre compte utilisateur\n" +
            "• Vous envoyer des notifications d'événements\n" +
            "• Améliorer nos services\n\n" +
            "3. PARTAGE DES DONNÉES\n" +
            "Nous ne vendons ni ne partageons vos données personnelles avec des tiers, " +
            "sauf obligation légale.\n\n" +
            "4. SÉCURITÉ\n" +
            "Nous mettons en place des mesures de sécurité appropriées pour protéger vos données.\n\n" +
            "5. VOS DROITS\n" +
            "Vous avez le droit d'accéder, modifier ou supprimer vos données personnelles.\n\n" +
            "6. CONTACT\n" +
            "Pour toute question : privacy@eventpro.com"
        );
        alert.showAndWait();
    }
    
    private void applyStyling() {
        // Ajout d'une ombre douce à la carte principale
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.15));
        shadow.setRadius(1);
        shadow.setOffsetY(1);
        formContainer.setEffect(shadow);
    }
    
    private void addSmoothAnimations() {
        // Animation d'entrée de la carte
        formContainer.setScaleX(0.95);
        formContainer.setScaleY(0.95);
        formContainer.setOpacity(0);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), formContainer);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), formContainer);
        fadeTransition.setToValue(1.0);
        
        scaleTransition.play();
        fadeTransition.play();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #d93025;" +
            "-fx-background-color: #fce8e6;" +
            "-fx-border-color: #d93025;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 12 16 12 16;" +
            "-fx-background-radius: 4;" +
            "-fx-border-radius: 4;"
        );
        statusLabel.setVisible(true);
        
        // Animation d'apparition
        statusLabel.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), statusLabel);
        fade.setToValue(1.0);
        fade.play();
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle(
            "-fx-font-family: 'Roboto', sans-serif;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #137333;" +
            "-fx-background-color: #e6f4ea;" +
            "-fx-border-color: #34a853;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 12 16 12 16;" +
            "-fx-background-radius: 4;" +
            "-fx-border-radius: 4;"
        );
        statusLabel.setVisible(true);
        
        // Animation d'apparition
        statusLabel.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), statusLabel);
        fade.setToValue(1.0);
        fade.play();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}