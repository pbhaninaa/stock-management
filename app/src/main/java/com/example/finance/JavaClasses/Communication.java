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

    public static void sendEmail(Context context, String subject,
                                 String body, EmailCallback callback) {
        String host = context.getString(R.string.smtp_host).trim();
        String fromEmail = context.getString(R.string.email_sender).trim();
        String password = context.getString(R.string.app_email_password).trim();
        String recipient = context.getString(R.string.report_email_recipient).trim();
        int port = Integer.parseInt(context.getString(R.string.smtp_port).trim());

        if (host.isEmpty() || fromEmail.isEmpty() || password.isEmpty() || recipient.isEmpty()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "Email reporting is not configured.", Toast.LENGTH_SHORT).show();
                callback.onEmailSent(false);
            });
            return;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            boolean success = false;
            try {
                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(recipient));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                success = true;

                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Email sent successfully!",
                                Toast.LENGTH_SHORT).show()

                );

            } catch (MessagingException e) {
                e.printStackTrace();

                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Failed to send email. Please try again.",
                                Toast.LENGTH_SHORT).show()
                );
            }

            boolean finalSuccess = success;
            new Handler(Looper.getMainLooper()).post(() -> callback.onEmailSent(finalSuccess));
        });
    }

    public interface EmailCallback {
        void onEmailSent(boolean success);
    }
}
