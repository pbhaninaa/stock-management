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

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Communication {

    public static boolean hasEmailConfiguration(Context context) {
        String host = context.getString(R.string.smtp_host).trim();
        String fromEmail = context.getString(R.string.email_sender).trim();
        String password = context.getString(R.string.app_email_password).trim();
        String port = context.getString(R.string.smtp_port).trim();
        return !host.isEmpty() && !fromEmail.isEmpty() && !password.isEmpty() && !port.isEmpty();
    }

    public static void sendEmail(Context context, String subject,
                                 String body, EmailCallback callback) {
        String recipient = context.getString(R.string.report_email_recipient).trim();
        if (recipient.isEmpty()) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onEmailSent(false));
            return;
        }
        sendEmail(context, subject, body, java.util.Collections.singletonList(recipient), callback);
    }

    public static void sendEmail(Context context, String subject, String body,
                                 List<String> recipients, EmailCallback callback) {
        if (!hasEmailConfiguration(context) || recipients == null || recipients.isEmpty()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "Email reporting is not configured.", Toast.LENGTH_SHORT).show();
                callback.onEmailSent(false);
            });
            return;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            boolean success = sendEmailBlocking(context, subject, body, recipients);

            if (success) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Email sent successfully!",
                                Toast.LENGTH_SHORT).show()
                );
            } else {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Failed to send email. Please try again.",
                                Toast.LENGTH_SHORT).show()
                );
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.onEmailSent(success));
        });
    }

    public static boolean sendEmailBlocking(Context context, String subject, String body,
                                            List<String> recipients) {
        String host = context.getString(R.string.smtp_host).trim();
        String fromEmail = context.getString(R.string.email_sender).trim();
        String password = context.getString(R.string.app_email_password).trim();
        int port = Integer.parseInt(context.getString(R.string.smtp_port).trim());

        if (!hasEmailConfiguration(context) || recipients == null || recipients.isEmpty()) {
            return false;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        try {
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            for (String recipient : recipients) {
                if (recipient != null && !recipient.trim().isEmpty()) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.trim()));
                }
            }
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface EmailCallback {
        void onEmailSent(boolean success);
    }
}
