package dev.d76.authx.platform.humanverification;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Validated
@EnableConfigurationProperties(HumanVerificationProperties.class)
public class HumanVerificationConfig {
}