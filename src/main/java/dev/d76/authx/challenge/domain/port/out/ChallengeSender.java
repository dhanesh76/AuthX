package dev.d76.authx.challenge.domain.port.out;

import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.DeliveryChannel;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.kernel.vo.Email;

public interface ChallengeSender {
    boolean supports(DeliveryChannel channel);
    void send(Email email, ChallengeSecret secret, ChallengePurpose purpose);
}
