package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nu.pattern.OpenCV;
import org.example.Main;
import org.example.models.User;
import org.example.services.FaceLoginService;
import org.example.services.FaceService;
import org.example.services.SessionManager;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.File;

public class FaceLoginController {

    @FXML private ImageView cameraPreview;
    @FXML private Label cameraPlaceholderLabel;
    @FXML private TextArea embeddingArea;
    @FXML private Label messageLabel;

    private VideoCapture videoCapture;
    private Mat currentFrame;
    private Thread cameraThread;

    private boolean cameraActive = false;

    private final FaceService faceService = new FaceService();
    private final FaceLoginService faceLoginService = new FaceLoginService();

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

        showSuccess("Caméra ouverte. Place ton visage puis clique sur connexion.");
    }

    @FXML
    public void handleFaceLogin() {
        if (!cameraActive || currentFrame == null || currentFrame.empty()) {
            showError("Veuillez d'abord ouvrir la caméra.");
            return;
        }

        try {
            File dir = new File("captures");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String imagePath = "captures/face_login.jpg";

            Mat frameToSave = currentFrame.clone();
            Imgcodecs.imwrite(imagePath, frameToSave);

            showInfo("Analyse du visage en cours...");

            String loginEmbedding = faceService.extractEmbedding(imagePath);

            if (loginEmbedding == null || loginEmbedding.isBlank()) {
                showError("Aucun visage détecté.");
                return;
            }

            if (embeddingArea != null) {
                embeddingArea.setText(loginEmbedding);
            }

            User user = faceLoginService.findUserByFace(loginEmbedding);

            if (user == null) {
                showError("Visage non reconnu.");
                return;
            }

            if (!user.isActive()) {
                showError("Ce compte est désactivé.");
                return;
            }

            stopCamera();

            SessionManager.setCurrentUser(user);
            showSuccess("Connexion réussie : " + user.getEmail());

            Main.redirectByUserType(user);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la connexion par visage.");
        }
    }

    @FXML
    public void handleCloseCamera() {
        stopCamera();
        showSuccess("Caméra fermée.");
    }

    @FXML
    public void handleBack() {
        stopCamera();
        Main.showLoginPage();
    }

    private void stopCamera() {
        cameraActive = false;

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

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(message);
        }
    }

    private void showInfo(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: blue;");
            messageLabel.setText(message);
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText(message);
        }
    }
}