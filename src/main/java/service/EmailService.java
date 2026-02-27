package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeBodyPart;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {

    private final String smtpHost;
    private final int smtpPort;

    private final String username;     // email expéditeur
    private final String appPassword;  // mot de passe application

    public EmailService(String username, String appPassword) {
        // Gmail par défaut
        this.smtpHost = "smtp.gmail.com";
        this.smtpPort = 587;

        this.username = username;
        this.appPassword = appPassword;
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // évite certains blocages

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });
    }

    public void sendTextEmail(String to, String subject, String body) {
        try {
            Session session = createSession();
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username, "TechFarm", StandardCharsets.UTF_8.name()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            message.setText(body, StandardCharsets.UTF_8.name());

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email: " + e.getMessage(), e);
        }
    }

    // ✅ HTML (design pro)
    public void sendHtmlEmail(String to, String subject, String html, String fallbackText) {
        try {
            Session session = createSession();
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username, "TechFarm", StandardCharsets.UTF_8.name()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, StandardCharsets.UTF_8.name());

            // multipart/alternative : le mail client choisit HTML ou Texte
            Multipart multipart = new MimeMultipart("alternative");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(fallbackText, StandardCharsets.UTF_8.name());
            multipart.addBodyPart(textPart);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(html, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email HTML: " + e.getMessage(), e);
        }
    }
}