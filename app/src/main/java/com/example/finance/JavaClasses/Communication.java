package com.example.finance;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Communication {

    // SMTP email method
    public static void sendEmail(Context context, String subject, String body, EmailCallback callback) {
        // SMTP server settings
        String host = "smtp.gmail.com";
        String fromEmail = "jaystarven@gmail.com";
        String password = "sbyb dcfs przw aell";
        int port = 587;

        // Set up properties for the SMTP connection
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");  // Use STARTTLS encryption

        // Use ExecutorService to run the email sending process on a background thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            boolean success = false;
            try {
                // Create a session with the provided credentials
                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

                // Compose the email message
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress("jaystarven@gmail.com"));
                message.setSubject(subject);
                message.setText(body);

                // Send the email
                Transport.send(message);
                success = true;

                // Show success message on the main thread
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Email sent successfully!", Toast.LENGTH_SHORT).show()

                );

            } catch (MessagingException e) {
                e.printStackTrace();

                // Show error message on the main thread
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Failed to send email. Please try again.", Toast.LENGTH_SHORT).show()
                );
            }

            // Notify callback about the result
            boolean finalSuccess = success;
            new Handler(Looper.getMainLooper()).post(() -> callback.onEmailSent(finalSuccess));
        });
    }

    // Interface for email callback
    public interface EmailCallback {
        void onEmailSent(boolean success);
    }
}
