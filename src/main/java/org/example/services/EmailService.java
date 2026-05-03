package org.example.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    // Email configuration - UPDATE THESE WITH YOUR INFO
    private static final String FROM_EMAIL = "houimlilouay6@gmail.com";  // Ton email
    private static final String FROM_PASSWORD = "mrnzkwnlstypnndv";       // Ton mot de passe d'application
    private static final String TO_EMAIL = "houimlilouay6@gmail.com";     // Où envoyer les notifications   // Where to send notifications

    // Email server configuration for Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    public static void sendZoneAddedNotification(String zoneName, String gpsCoords, int pollutionLevel) {
        new Thread(() -> {
            try {
                String subject = "🌍 [WasteWise] Nouvelle zone polluée ajoutée - " + zoneName;
                String body = buildEmailBody(zoneName, gpsCoords, pollutionLevel);
                sendEmail(subject, body);
                System.out.println("✅ Email notification sent for zone: " + zoneName);
            } catch (Exception e) {
                System.err.println("❌ Failed to send email: " + e.getMessage());
            }
        }).start();
    }

    public static void sendZoneUpdatedNotification(String zoneName, int oldLevel, int newLevel, String gpsCoords) {
        new Thread(() -> {
            try {
                String subject = "✏️ [WasteWise] Zone polluée mise à jour - " + zoneName;
                String body = buildUpdateEmailBody(zoneName, oldLevel, newLevel, gpsCoords);
                sendEmail(subject, body);
                System.out.println("✅ Email notification sent for zone update: " + zoneName);
            } catch (Exception e) {
                System.err.println("❌ Failed to send update email: " + e.getMessage());
            }
        }).start();
    }

    public static void sendZoneDeletedNotification(String zoneName, int pollutionLevel) {
        new Thread(() -> {
            try {
                String subject = "🗑️ [WasteWise] Zone polluée supprimée - " + zoneName;
                String body = buildDeleteEmailBody(zoneName, pollutionLevel);
                sendEmail(subject, body);
                System.out.println("✅ Email notification sent for zone deletion: " + zoneName);
            } catch (Exception e) {
                System.err.println("❌ Failed to send deletion email: " + e.getMessage());
            }
        }).start();
    }

    public static void sendCriticalAlertNotification(String zoneName, int pollutionLevel, String gpsCoords) {
        new Thread(() -> {
            try {
                String subject = "🚨 [URGENT] Alerte critique - Zone polluée: " + zoneName;
                String body = buildCriticalAlertBody(zoneName, pollutionLevel, gpsCoords);
                sendEmail(subject, body);
                System.out.println("✅ Critical alert email sent for zone: " + zoneName);
            } catch (Exception e) {
                System.err.println("❌ Failed to send critical alert: " + e.getMessage());
            }
        }).start();
    }

    private static void sendEmail(String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO_EMAIL));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");

        Transport.send(message);
    }

    private static String buildEmailBody(String zoneName, String gpsCoords, int pollutionLevel) {
        String riskLevel = pollutionLevel >= 7 ? "🔴 CRITIQUE" :
                (pollutionLevel >= 4 ? "🟡 MOYEN" : "🟢 FAIBLE");

        String riskColor = pollutionLevel >= 7 ? "#dc3545" :
                (pollutionLevel >= 4 ? "#ffc107" : "#28a745");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2e7d32, #4caf50); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid %s; border-radius: 5px; }
                    .label { font-weight: bold; color: #555; }
                    .footer { text-align: center; padding-top: 20px; font-size: 12px; color: #999; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🌱 WasteWise TN</h2>
                        <p>Nouvelle zone polluée enregistrée</p>
                    </div>
                    <div class="content">
                        <h3>📋 Détails de la zone:</h3>
                        <div class="info-box" style="border-left-color: %s;">
                            <p><span class="label">📍 Nom:</span> <strong>%s</strong></p>
                            <p><span class="label">🗺️ Coordonnées GPS:</span> %s</p>
                            <p><span class="label">📊 Niveau:</span> <strong style="color: %s;">%d/10</strong></p>
                            <p><span class="label">⚠️ Risque:</span> <strong style="color: %s;">%s</strong></p>
                            <p><span class="label">📅 Date:</span> %s</p>
                        </div>
                        <div class="info-box">
                            <h4>💡 Actions recommandées:</h4>
                            <ul>
                                %s
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Cet email a été généré automatiquement par WasteWise TN.</p>
                        <p>© 2025 WasteWise TN - Gestion des zones polluées</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(riskColor, riskColor, zoneName, gpsCoords, riskColor, pollutionLevel, riskColor, riskLevel, timestamp, getRecommendations(pollutionLevel));
    }

    private static String buildUpdateEmailBody(String zoneName, int oldLevel, int newLevel, String gpsCoords) {
        String trend = newLevel > oldLevel ? "📈 DÉGRADATION" : (newLevel < oldLevel ? "📉 AMÉLIORATION" : "➡️ STABLE");
        String trendColor = newLevel > oldLevel ? "#dc3545" : (newLevel < oldLevel ? "#28a745" : "#ffc107");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #ff9800, #ffc107); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #ff9800; border-radius: 5px; }
                    .label { font-weight: bold; color: #555; }
                    .footer { text-align: center; padding-top: 20px; font-size: 12px; color: #999; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>✏️ WasteWise TN</h2>
                        <p>Mise à jour d'une zone polluée</p>
                    </div>
                    <div class="content">
                        <h3>📝 Détails de la modification:</h3>
                        <div class="info-box">
                            <p><span class="label">📍 Zone:</span> <strong>%s</strong></p>
                            <p><span class="label">🗺️ Coordonnées:</span> %s</p>
                            <p><span class="label">📊 Ancien niveau:</span> %d/10</p>
                            <p><span class="label">📊 Nouveau niveau:</span> <strong style="color: %s;">%d/10</strong></p>
                            <p><span class="label">📈 Tendance:</span> <strong style="color: %s;">%s</strong></p>
                            <p><span class="label">📅 Date:</span> %s</p>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Cet email a été généré automatiquement par WasteWise TN.</p>
                        <p>© 2025 WasteWise TN - Gestion des zones polluées</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(zoneName, gpsCoords, oldLevel, getColorForLevel(newLevel), newLevel, trendColor, trend, timestamp);
    }

    private static String buildDeleteEmailBody(String zoneName, int pollutionLevel) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #6c757d, #495057); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #6c757d; border-radius: 5px; }
                    .label { font-weight: bold; color: #555; }
                    .footer { text-align: center; padding-top: 20px; font-size: 12px; color: #999; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🗑️ WasteWise TN</h2>
                        <p>Suppression d'une zone polluée</p>
                    </div>
                    <div class="content">
                        <h3>📋 Zone supprimée:</h3>
                        <div class="info-box">
                            <p><span class="label">📍 Nom:</span> <strong>%s</strong></p>
                            <p><span class="label">📊 Dernier niveau:</span> %d/10</p>
                            <p><span class="label">📅 Date:</span> %s</p>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Cet email a été généré automatiquement par WasteWise TN.</p>
                        <p>© 2025 WasteWise TN - Gestion des zones polluées</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(zoneName, pollutionLevel, timestamp);
    }

    private static String buildCriticalAlertBody(String zoneName, int pollutionLevel, String gpsCoords) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #dc3545, #c82333); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .alert-box { background: #fff3e0; padding: 15px; margin: 15px 0; border-left: 4px solid #dc3545; border-radius: 5px; }
                    .label { font-weight: bold; color: #555; }
                    .urgent { color: #dc3545; font-weight: bold; font-size: 18px; }
                    .footer { text-align: center; padding-top: 20px; font-size: 12px; color: #999; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🚨 ALERTE CRITIQUE 🚨</h2>
                        <p>Zone à haut risque détectée</p>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <p class="urgent">⚠️ ACTION IMMÉDIATE REQUISE ⚠️</p>
                            <p><span class="label">📍 Zone:</span> <strong>%s</strong></p>
                            <p><span class="label">🗺️ Coordonnées:</span> %s</p>
                            <p><span class="label">📊 Niveau:</span> <strong style="color: #dc3545;">%d/10 - CRITIQUE</strong></p>
                            <p><span class="label">📅 Date:</span> %s</p>
                        </div>
                        <div class="alert-box">
                            <h4>🔴 Actions urgentes:</h4>
                            <ul>
                                <li>🚨 Évacuer la zone immédiatement</li>
                                <li>😷 Distribution de masques FFP2</li>
                                <li>📢 Alerter les autorités</li>
                                <li>🏥 Préparer les centres médicaux</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Ceci est une alerte automatique - Veuillez prendre des mesures immédiates.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(zoneName, gpsCoords, pollutionLevel, timestamp);
    }

    private static String getRecommendations(int level) {
        if (level >= 8) {
            return "<li>🚨 <strong>URGENCE MAXIMALE</strong> - Évacuation immédiate</li>" +
                    "<li>🛡️ Équipement de protection nécessaire</li>" +
                    "<li>📞 Contacter les autorités (197)</li>" +
                    "<li>🏥 Consultation médicale obligatoire</li>";
        } else if (level >= 6) {
            return "<li>⚠️ Risque élevé - Limiter l'exposition</li>" +
                    "<li>😷 Port du masque recommandé</li>" +
                    "<li>🏠 Rester à l'intérieur si possible</li>" +
                    "<li>👃 Surveiller les symptômes</li>";
        } else if (level >= 4) {
            return "<li>📊 Surveillance régulière recommandée</li>" +
                    "<li>🌳 Planter des arbres pour améliorer l'air</li>" +
                    "<li>♻️ Organiser des campagnes de nettoyage</li>";
        } else {
            return "<li>✅ Niveau acceptable - Continuer la surveillance</li>" +
                    "<li>📈 Maintenir les bonnes pratiques</li>" +
                    "<li>🎓 Sensibiliser la communauté</li>";
        }
    }

    private static String getColorForLevel(int level) {
        if (level >= 7) return "#dc3545";
        if (level >= 4) return "#ffc107";
        return "#28a745";
    }
}