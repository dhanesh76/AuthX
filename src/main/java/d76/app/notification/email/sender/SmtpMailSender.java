package d76.app.notification.email.sender;

import d76.app.notification.email.model.MailContentType;
import d76.app.notification.email.model.MailMessage;
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
public class SmtpMailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    @Override
    @Async
    public void send(MailMessage message) {

        var contentType = message.contentType();
        if (contentType.equals(MailContentType.TEXT))
            sendText(message);

        else if (contentType.equals(MailContentType.HTML)) {
            try {
                sendHtml(message);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendText(MailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.content());

        javaMailSender.send(mail);
    }

    private void sendHtml(MailMessage message) throws MessagingException {
        MimeMessage mime = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true);

        helper.setTo(message.to());
        helper.setSubject(message.subject());
        helper.setText(message.content(), true);

        javaMailSender.send(mime);
    }
}
