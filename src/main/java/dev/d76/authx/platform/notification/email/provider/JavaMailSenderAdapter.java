package dev.d76.authx.platform.notification.email.provider;

import dev.d76.authx.platform.notification.email.MailContentType;
import dev.d76.authx.platform.notification.email.MailMessage;
import dev.d76.authx.platform.notification.email.MailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JavaMailSenderAdapter implements MailSender {

    private final JavaMailSender javaMailSender;

    @Async
    @Override
    public void send(MailMessage message) {
        if (MailContentType.HTML.equals(message.contentType())) {
            sendHtml(message);
        } else {
            sendText(message);
        }
    }

    private void sendText(MailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.body());
        javaMailSender.send(mail);
    }

    private void sendHtml(MailMessage message) {
        try {
            MimeMessage mime = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true);
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.body(), true);
            javaMailSender.send(mime);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML usernameOrEmail", e);
        }
    }
}