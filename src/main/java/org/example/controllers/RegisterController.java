package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nu.pattern.OpenCV;
import org.example.Main;
import org.example.services.FaceService;
import org.example.services.UserService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.File;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private RadioButton citoyenRadio;
    @FXML private RadioButton valorizerRadio;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox agreeTermsCheckBox;
    @FXML private TextArea faceEmbeddingArea;
    @FXML private Label messageLabel;

    @FXML private ImageView cameraPreview;
    @FXML private Label cameraPlaceholderLabel;

    private boolean cameraOpened = false;
    private boolean cameraActive = false;
    private VideoCapture videoCapture;
    private Mat currentFrame;
    private Thread cameraThread;

    private final UserService userService = UserService.getInstance();
    private final FaceService faceService = new FaceService();

    @FXML
    public void initialize() {
        try {
            OpenCV.loadLocally();
        } catch (Exception e) {
            showError("Erreur chargement OpenCV.");
        }

        if (messageLabel != null) {
            messageLabel.setText("");
        }

        if (citoyenRadio != null) {
            citoyenRadio.setSelected(true);
        }

        if (cameraPlaceholderLabel != null) {
            cameraPlaceholderLabel.setText("Aperçu caméra");
            cameraPlaceholderLabel.setVisible(true);
        }
    }

    @FXML
    public void handleOpenCamera() {
        if (cameraActive) {
            showError("La caméra est déjà ouverte.");
            return;
        }

        videoCapture = new VideoCapture(0);

        if (!videoCapture.isOpened()) {
            showError("Impossible d'ouvrir la caméra.");
            return;
        }

        cameraActive = true;
        cameraOpened = true;
        currentFrame = new Mat();

        if (cameraPlaceholderLabel != null) {
            cameraPlaceholderLabel.setVisible(false);
        }

        cameraThread = new Thread(() -> {
            while (cameraActive) {
                if (videoCapture.read(currentFrame)) {
                    Image image = matToImage(currentFrame);

                    Platform.runLater(() -> {
                        if (cameraPreview != null) {
                            cameraPreview.setImage(image);
                        }
                    });
                }

                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        cameraThread.setDaemon(true);
        cameraThread.start();

        showSuccess("Caméra ouverte. Tu peux capturer le visage.");
    }

    @FXML
    public void handleCaptureFace() {
        if (!cameraOpened || !cameraActive || currentFrame == null || currentFrame.empty()) {
            showError("Veuillez d'abord ouvrir la caméra.");
            return;
        }

        try {
            File dir = new File("captures");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String imagePath = "captures/face_register.jpg";
            Imgcodecs.imwrite(imagePath, currentFrame);

            String embedding = faceService.extractEmbedding(imagePath);

            if (embedding == null || embedding.isBlank()) {
                showError("Aucun visage détecté. Réessayez avec un visage bien visible.");
                return;
            }

            if (faceEmbeddingArea != null) {
                faceEmbeddingArea.setText(embedding);
            }

            showSuccess("Visage capturé avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la capture du visage.");
        }
    }

    @FXML
    public void handleCloseCamera() {
        stopCamera();
        showSuccess("Caméra fermée.");
    }

    @FXML
    public void handleRegister() {
        String nom = getText(nomField);
        String prenom = getText(prenomField);
        String email = getText(emailField).toLowerCase();
        String telephone = getText(telephoneField);
        String password = passwordField != null ? passwordField.getText() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";
        String faceEmbedding = getText(faceEmbeddingArea);

        String type = null;

        if (citoyenRadio != null && citoyenRadio.isSelected()) {
            type = "CITIZEN";
        } else if (valorizerRadio != null && valorizerRadio.isSelected()) {
            type = "VALORIZER";
        }

        if (nom.isEmpty()) {
            showError("Le champ nom est obligatoire.");
            return;
        }

        if (prenom.isEmpty()) {
            showError("Le champ prénom est obligatoire.");
            return;
        }

        if (email.isEmpty()) {
            showError("Le champ email est obligatoire.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Veuillez saisir une adresse email valide.");
            return;
        }

        if (telephone.isEmpty()) {
            showError("Le champ téléphone est obligatoire.");
            return;
        }

        if (!telephone.matches("\\d{8}")) {
            showError("Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        if (type == null) {
            showError("Veuillez sélectionner un type d'utilisateur.");
            return;
        }

        if (password.isEmpty()) {
            showError("Le champ mot de passe est obligatoire.");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (confirmPassword.isEmpty()) {
            showError("Veuillez confirmer le mot de passe.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (agreeTermsCheckBox == null || !agreeTermsCheckBox.isSelected()) {
            showError("Vous devez accepter les conditions d'utilisation.");
            return;
        }

        String result = userService.registerUser(
                nom,
                prenom,
                email,
                telephone,
                type,
                password,
                confirmPassword,
                true,
                faceEmbedding.isBlank() ? null : faceEmbedding
        );

        if ("SUCCESS".equals(result)) {
            stopCamera();
            showSuccess("Compte créé avec succès. Connecte-toi maintenant.");
            clearFields();
        } else {
            showError(result);
        }
    }

    @FXML
    public void handleBackToLogin() {
        stopCamera();
        Main.showLoginPage();
    }

    private void stopCamera() {
        cameraActive = false;
        cameraOpened = false;

        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.release();
        }

        if (cameraPreview != null) {
            cameraPreview.setImage(null);
        }

        if (cameraPlaceholderLabel != null) {
            cameraPlaceholderLabel.setText("Aperçu caméra");
            cameraPlaceholderLabel.setVisible(true);
        }
    }

    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private String getText(TextInputControl field) {
        return field != null && field.getText() != null ? field.getText().trim() : "";
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(message);
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText(message);
        }
    }

    private void clearFields() {
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (emailField != null) emailField.clear();
        if (telephoneField != null) telephoneField.clear();
        if (passwordField != null) passwordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (faceEmbeddingArea != null) faceEmbeddingArea.clear();
        if (agreeTermsCheckBox != null) agreeTermsCheckBox.setSelected(false);
        if (citoyenRadio != null) citoyenRadio.setSelected(true);
        if (valorizerRadio != null) valorizerRadio.setSelected(false);
    }
}