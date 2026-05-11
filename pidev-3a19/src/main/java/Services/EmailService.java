package Services;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.nio.charset.StandardCharsets;

/**
 * Envoi SMTP Gmail. Identifiants dans {@code config.properties} (voir {@link AppProperties}),
 * avec repli sur {@code GMAIL_ADDRESS} et {@code GMAIL_APP_PASSWORD}.
 */
public class EmailService {

    private final String username;
    private final String password;

    public EmailService() {
        this(resolveSmtpUsername(), resolveSmtpAppPassword());
    }

    public EmailService(String username, String appPassword) {
        this.username = username != null ? username.trim() : "";
        this.password = normalizeAppPassword(appPassword);
    }

    private static String firstEnv(String... keys) {
        for (String key : keys) {
            String v = System.getenv(key);
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }

    public static String resolveSmtpUsername() {
        String fromProp = AppProperties.property("smtp.gmail.address");
        if (fromProp != null) {
            return fromProp;
        }
        return firstEnv("GMAIL_ADDRESS", "FARMTECH_SMTP_USER", "MAIL_FROM");
    }

    public static String resolveSmtpAppPassword() {
        String fromProp = AppProperties.property("smtp.gmail.app.password");
        if (fromProp != null) {
            return normalizeAppPassword(fromProp);
        }
        return normalizeAppPassword(firstEnv("GMAIL_APP_PASSWORD", "FARMTECH_SMTP_APP_PASSWORD"));
    }

    public static String normalizeAppPassword(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\s+", "");
    }

    /** True si adresse et mot de passe d'application sont renseignés (fichier ou env). */
    public static boolean isSmtpConfigured() {
        return !resolveSmtpUsername().isBlank() && !resolveSmtpAppPassword().isBlank();
    }

    public boolean isConfigured() {
        return !username.isBlank() && !password.isBlank();
    }

    public String getUsername() {
        return username;
    }

    private Session createSession() {
        java.util.Properties mailProps = new java.util.Properties();
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.host", "smtp.gmail.com");
        mailProps.put("mail.smtp.port", "587");
        mailProps.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendHtmlEmail(String to, String subject, String html, String fallbackText) {
        if (!isConfigured()) {
            throw new IllegalStateException(
                "SMTP non configuré : renseignez smtp.gmail.address et smtp.gmail.app.password "
                    + "dans config.properties (src/main/resources ou à la racine du projet), "
                    + "ou les variables GMAIL_ADDRESS et GMAIL_APP_PASSWORD."
            );
        }
        try {
            Session session = createSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "FARMTECH", StandardCharsets.UTF_8.name()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            Multipart multipart = new MimeMultipart("alternative");
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(fallbackText, StandardCharsets.UTF_8.name());
            multipart.addBodyPart(textPart);
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(html, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new IllegalStateException(
                "Gmail a refusé la connexion : vérifiez smtp.gmail.app.password (mot de passe d'application, 16 caractères) "
                    + "pour le compte indiqué dans smtp.gmail.address.",
                e
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email: " + e.getMessage(), e);
        }
    }

    public void sendPlantDeathMail(String plantName, int slotIndex) {
        if (!isConfigured()) {
            System.err.println("Email plante : renseignez SMTP dans config.properties (smtp.gmail.*).");
            return;
        }
        try {
            Session session = createSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
            message.setSubject("🌱 FARMTECH - Plant Alert");

            String html =
                    "<div style='font-family: Arial, sans-serif; background-color:#f4f6f9; padding:20px;'>"
                            + "<div style='max-width:600px; margin:auto; background:white; border-radius:10px; overflow:hidden; box-shadow:0 4px 10px rgba(0,0,0,0.1);'>"

                            + "<div style='background-color:#2E7D32; padding:20px; text-align:center;'>"
                            + "<h1 style='color:white; margin:0;'>🌿 FARMTECH</h1>"
                            + "<p style='color:#d0f0d0; margin:5px 0 0;'>Plant Monitoring System</p>"
                            + "</div>"

                            + "<div style='padding:30px; text-align:center;'>"
                            + "<h2 style='color:#d32f2f;'>⚠ Plant Died Alert</h2>"

                            + "<p style='font-size:16px; color:#555;'>"
                            + "Your plant <strong>" + plantName + "</strong><br>"
                            + "in slot <strong>#" + slotIndex + "</strong><br>"
                            + "has <span style='color:red; font-weight:bold;'>died</span>."
                            + "</p>"

                            + "<div style='margin:25px 0;'>"
                            + "<a href='#' style='background-color:#2E7D32; color:white; padding:12px 25px;"
                            + "text-decoration:none; border-radius:5px; font-weight:bold;'>"
                            + "🌱 Revive Plant"
                            + "</a>"
                            + "</div>"

                            + "<p style='font-size:13px; color:#999;'>"
                            + "Please water your plant on time to keep it alive."
                            + "</p>"
                            + "</div>"

                            + "<div style='background:#f1f1f1; padding:15px; text-align:center; font-size:12px; color:#777;'>"
                            + "© 2026 FarmTech System | Smart Agriculture Monitoring"
                            + "</div>"

                            + "</div>"
                            + "</div>";

            message.setContent(html, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Beautiful HTML Email Sent!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
