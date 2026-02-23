package Services;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class EmailService {

    private final String sender = "tahajaballah07@gmail.com";
    private final String password = "nphm ncyy cxlv kwtk";

    public void sendPlantDeathMail(String plantName, int slotIndex) {

        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(sender, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse("tahajaballah07@gmail.com")
            );

            message.setSubject("🌱 Plant Died Alert");

            message.setText("Your plant '" + plantName +
                    "' in slot " + slotIndex +
                    " has died. Please water it!");

            Transport.send(message);
            System.out.println("✅ Email successfully sent to: " + sender);

            System.out.println("Death email sent ✅");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}