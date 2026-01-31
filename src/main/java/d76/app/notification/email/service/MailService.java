package d76.app.notification.email.service;

import d76.app.notification.email.model.MailContentType;
import d76.app.notification.email.model.MailMessage;
import d76.app.notification.email.sender.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailSender mailSender;

    @Async
    public void sendTextMail(String to, String subject, String content) {
        mailSender.send(new MailMessage(
                to,
                subject,
                content,
                MailContentType.TEXT)
        );
    }
    //implement html mail sender for future requirements
}
