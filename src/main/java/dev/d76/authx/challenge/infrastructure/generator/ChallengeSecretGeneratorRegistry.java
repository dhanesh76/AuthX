package dev.d76.authx.challenge.infrastructure.generator;

import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.port.out.ChallengeSecretGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChallengeSecretGeneratorRegistry {
    private List<ChallengeSecretGenerator> generators;

    public ChallengeSecretGeneratorRegistry(List<ChallengeSecretGenerator> generators) {
        this.generators = generators;
    }

    public
    ChallengeSecretGenerator resolve(ChallengeType required){
        return generators.stream()
                .filter(generator -> generator.supports(required))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No secret generator available for the required challenge purpose")
                );
    }
}
