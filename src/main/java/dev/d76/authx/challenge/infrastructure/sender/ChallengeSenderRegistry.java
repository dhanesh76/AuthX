package dev.d76.authx.challenge.infrastructure.sender;

import dev.d76.authx.challenge.domain.model.DeliveryChannel;
import dev.d76.authx.challenge.domain.port.out.ChallengeSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChallengeSenderRegistry {
    private final List<ChallengeSender> senders;

    public ChallengeSenderRegistry(List<ChallengeSender> senders) {
        this.senders = senders;
    }

    public ChallengeSender resolve(DeliveryChannel channel) {
        return senders.stream()
                .filter(challengeSender -> challengeSender.supports(channel))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No sender registered for delivery channel: " + channel.name()
                ));
    }
}
