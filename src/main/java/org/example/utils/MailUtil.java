package org.example.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailUtil {

    private static final String FROM_EMAIL = "ksammoudy@gmail.com";
    private static final String APP_PASSWORD = "exsqwesnniwjituf";

    public static boolean sendEmail(String to, String subject, String content) {
        System.out.println("📨 Tentative d'envoi à : " + to);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès.");
            return true;

        } catch (MessagingException e) {
            System.out.println("❌ Erreur envoi email : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}