package com.zidio.nexushr.auth.services.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${app.mail.from:no-reply@nexushr.com}")
    private String fromAddress;

    public EmailService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordReset(String to, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Reset your NexusHR password");

        message.setText(
                "Hi,\n\n" +
                        "Click the link below to reset your password:\n\n" +
                        resetLink +
                        "\n\nThis link expires in 15 minutes." +
                        "\nIf you didn't request this, you can ignore this email."
        );

        JavaMailSender sender = mailSender.getIfAvailable();

        if (sender == null) {
            log.info("Password reset link for {}: {}", to, resetLink);
            return;
        }

        try {
            sender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (Exception e) {
            log.error("Could not send reset email to {}: {}", to, e.getMessage());
            log.info("Password reset link for {}: {}", to, resetLink);
        }
    }
}
