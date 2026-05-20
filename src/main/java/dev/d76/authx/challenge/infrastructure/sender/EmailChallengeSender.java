package dev.d76.authx.challenge.infrastructure.sender;

import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.DeliveryChannel;
import dev.d76.authx.challenge.domain.port.out.ChallengeSender;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.authx.platform.notification.email.MailContentType;
import dev.d76.authx.platform.notification.email.MailMessage;
import dev.d76.authx.platform.notification.email.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailChallengeSender implements ChallengeSender {

    private final MailSender sender;

    @Override
    public boolean supports(DeliveryChannel required) {
        return DeliveryChannel.EMAIL == required;
    }

    @Override
    public void send(Email email, ChallengeSecret secret, ChallengePurpose purpose) {
        ChallengeEmailTemplate template = ChallengeEmailTemplate.of(purpose, secret);

        sender.send(new MailMessage(
                email.value(),
                template.subject(),
                template.body(),
                MailContentType.TEXT
        ));
    }
}