package services;

import entities.AppelOffre;
import entities.ReponseOffre;
import entities.UserContact;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class EmailNotificationService {

    public void envoyerValidationReponse(UserContact citoyen, AppelOffre appelOffre, ReponseOffre reponse) throws MessagingException {
        MailConfig config = MailConfig.fromEnvironment();
        if (!config.isConfigured()) {
            throw new MessagingException("Configuration SMTP manquante. Renseignez mail.properties ou WW_MAIL_HOST, WW_MAIL_USER, WW_MAIL_PASSWORD et WW_MAIL_FROM.");
        }
        if (citoyen == null || !citoyen.hasEmail()) {
            throw new MessagingException("Email citoyen introuvable.");
        }

        Session session = Session.getInstance(config.properties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.user(), config.password());
            }
        });

        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(config.from(), config.fromName()));
        } catch (UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress(config.from()));
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(citoyen.getEmail(), false));
        message.setSubject("Validation de votre reponse d'offre", "UTF-8");
        message.setContent(buildHtml(citoyen, appelOffre, reponse), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private String buildHtml(UserContact citoyen, AppelOffre appelOffre, ReponseOffre reponse) {
        String name = safe(citoyen.getFullName(), "Citoyen");
        String titre = appelOffre == null ? "l'appel d'offre" : safe(appelOffre.getTitre(), "l'appel d'offre");
        return """
                <div style="font-family:Segoe UI,Arial,sans-serif;color:#2b2b2b;font-size:15px;line-height:1.55">
                  <h2 style="font-weight:500;color:#111827;margin-bottom:22px">Validation de votre reponse d'offre</h2>
                  <p>Bonjour %s,</p>
                  <p>Votre reponse a l'appel d'offre <strong>%s</strong> a ete validee.</p>
                  <p>
                    <strong>Quantite proposee:</strong> %s kg<br>
                    <strong>Statut:</strong> valide
                  </p>
                  <p style="margin-top:26px">Cordialement,<br>WasteWise</p>
                </div>
                """.formatted(
                escapeHtml(name),
                escapeHtml(titre),
                String.format(Locale.ROOT, "%.2f", reponse.getQuantiteProposee()).replace('.', ',')
        );
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String escapeHtml(String value) {
        return safe(value, "")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record MailConfig(
            String host,
            String port,
            String user,
            String password,
            String from,
            String fromName,
            boolean startTls,
            boolean auth
    ) {
        private static MailConfig fromEnvironment() {
            ConfigValues values = ConfigValues.load();
            String dsn = values.config("MAILER_DSN", "");
            if (!dsn.isBlank() && !dsn.equalsIgnoreCase("null://null")) {
                MailConfig fromDsn = fromMailerDsn(dsn, values);
                if (fromDsn != null) {
                    return fromDsn;
                }
            }

            String user = values.config("WW_MAIL_USER", "");
            return new MailConfig(
                    values.config("WW_MAIL_HOST", ""),
                    values.config("WW_MAIL_PORT", "587"),
                    user,
                    values.config("WW_MAIL_PASSWORD", ""),
                    values.config("WW_MAIL_FROM", values.config("MAILER_FROM", user)),
                    values.config("WW_MAIL_FROM_NAME", "WasteWise"),
                    Boolean.parseBoolean(values.config("WW_MAIL_STARTTLS", "true")),
                    Boolean.parseBoolean(values.config("WW_MAIL_AUTH", "true"))
            );
        }

        private static MailConfig fromMailerDsn(String dsn, ConfigValues values) {
            try {
                URI uri = URI.create(dsn);
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
                if (!scheme.startsWith("smtp")) {
                    return null;
                }

                String userInfo = uri.getRawUserInfo();
                String user = "";
                String password = "";
                if (userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    user = decode(parts[0]);
                    if (parts.length > 1) {
                        password = decode(parts[1]);
                    }
                }

                String host = uri.getHost() == null ? "" : uri.getHost();
                String port = uri.getPort() > 0 ? String.valueOf(uri.getPort()) : "587";
                boolean startTls = !dsn.toLowerCase(Locale.ROOT).contains("encryption=ssl") && !scheme.equals("smtps");
                String from = values.config("WW_MAIL_FROM", values.config("MAILER_FROM", user));
                return new MailConfig(host, port, user, password, from, values.config("WW_MAIL_FROM_NAME", "WasteWise"), startTls, true);
            } catch (Exception e) {
                return null;
            }
        }

        private boolean isConfigured() {
            return !host.isBlank() && !port.isBlank() && !user.isBlank() && !password.isBlank() && !from.isBlank();
        }

        private Properties properties() {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", String.valueOf(auth));
            props.put("mail.smtp.starttls.enable", String.valueOf(startTls));
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");
            return props;
        }

        private static String decode(String value) {
            return URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
        }
    }

    private static final class ConfigValues {
        private final Properties localProperties;

        private ConfigValues(Properties localProperties) {
            this.localProperties = localProperties;
        }

        private static ConfigValues load() {
            Properties properties = new Properties();
            loadFromClasspath(properties);
            loadFromProjectFile(properties);
            return new ConfigValues(properties);
        }

        private String config(String key, String defaultValue) {
            String sys = System.getProperty(key);
            if (sys != null && !sys.isBlank()) {
                return sys.trim();
            }
            String env = System.getenv(key);
            if (env != null && !env.isBlank()) {
                return env.trim();
            }
            String local = localProperties.getProperty(key);
            if (local != null && !local.isBlank()) {
                return local.trim();
            }
            return defaultValue;
        }

        private static void loadFromClasspath(Properties properties) {
            try (InputStream input = EmailNotificationService.class.getClassLoader().getResourceAsStream("mail.properties")) {
                if (input != null) {
                    properties.load(input);
                }
            } catch (IOException ignored) {
            }
        }

        private static void loadFromProjectFile(Properties properties) {
            Path path = Path.of("mail.properties");
            if (!Files.isRegularFile(path)) {
                return;
            }
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            } catch (IOException ignored) {
            }
        }
    }
}
