package Services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {

    private final String username;
    private final String password;

    public EmailService() {
        this.username = "tahajaballah07@gmail.com";
        this.password = "nphm ncyy cxlv kwtk";
    }

    public EmailService(String username, String appPassword) {
        this.username = username;
        this.password = appPassword;
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendHtmlEmail(String to, String subject, String html, String fallbackText) {
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
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email: " + e.getMessage(), e);
        }
    }

    public void sendPlantDeathMail(String plantName, int slotIndex) {
        try {
            Session session = createSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("tahajaballah07@gmail.com"));
            message.setSubject("🌱 FARMTECH - Plant Alert");

            // ===== BEAUTIFUL HTML DESIGN =====
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