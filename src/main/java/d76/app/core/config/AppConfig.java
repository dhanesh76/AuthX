package d76.app.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }

}
