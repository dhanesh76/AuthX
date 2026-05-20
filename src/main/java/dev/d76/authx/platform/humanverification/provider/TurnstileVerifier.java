package dev.d76.authx.platform.humanverification.provider;

import dev.d76.authx.platform.humanverification.HumanVerificationProperties;
import dev.d76.authx.platform.humanverification.HumanVerificationRequest;
import dev.d76.authx.platform.humanverification.HumanVerifier;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TurnstileVerifier implements HumanVerifier {

    private final HumanVerificationProperties properties;
    private final RestClient restClient;

    @Override
    public boolean verify(HumanVerificationRequest request) {

        TurnstileRequest requestBody = new TurnstileRequest(
                properties.secretKey(),
                request.token(),
                request.remoteIp()
        );

        TurnstileResponse response;

        try {
            response = restClient
                    .post()
                    .uri(properties.verifyUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(TurnstileResponse.class);
        } catch (Exception e) {
            log.error("Turnstile verification call failed — failing closed", e);
            return false;
        }

        if (response == null || !response.success()) {
            log.warn("Turnstile rejected — error-codes={}",
                    response == null ? "null response" : response.errorCodes());
            return false;
        }

        // action is only present when a real Cloudflare widget generated the token.
        // Cloudflare test keys do not run a widget, so action is null — skip the check.
        if (response.action() != null && !request.expectedAction().equals(response.action())) {
            log.warn("Action mismatch — expected={} actual={}",
                    request.expectedAction(), response.action());
            return false;
        }

        if (!properties.expectedHost().equals(response.hostname())) {
            log.warn("Hostname mismatch — expected={} actual={}",
                    properties.expectedHost(), response.hostname());
            return false;
        }

        return true;
    }

    private record TurnstileRequest(
            String secret,
            String response,
            @JsonProperty("remoteip") String remoteIp
    ) {}

    private record TurnstileResponse(
            boolean success,
            @JsonProperty("error-codes") List<String> errorCodes,
            String hostname,
            String action,
            @JsonProperty("challenge_ts") String challengeTs
    ) {}
}