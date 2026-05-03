package org.example.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    public static void sendNotification(String toEmail, String nomCitoyen, String nomEvent) {
        // 1. Mailtrap Credentials (Badalhom mel account mte3ek!)
        final String username = "VOTRE_MAILTRAP_USERNAME";
        final String password = "VOTRE_MAILTRAP_PASSWORD";

        // 2. Configuration SMTP mta3 Mailtrap
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); // Secure connection
        prop.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        prop.put("mail.smtp.port", "2525");

        // 3. Création de la Session
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // 4. Construction du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("support@wastewise.tn"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Confirmation de Participation - WasteWiseTN");

            // 5. Contenu du mail
            String htmlContent = "<h1>Bonjour " + nomCitoyen + " !</h1>"
                    + "<p>Nous sommes ravis de confirmer votre participation à l'événement : <b>" + nomEvent + "</b>.</p>"
                    + "<p>Merci de contribuer à rendre notre environnement plus propre avec <b>WasteWiseTN</b> ! 🌍</p>"
                    + "<br><p>Cordialement,<br>L'équipe WasteWise.</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            // 6. Envoi
            Transport.send(message);
            System.out.println("📧 Notification envoyée avec succès à : " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi du mail : " + e.getMessage());
            e.printStackTrace();
        }
    }
}