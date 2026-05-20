package dev.d76.authx.account.infrastructure.security.config;

import dev.d76.authx.account.infrastructure.security.federation.adapter.FederatedOAuth2Adapter;
import dev.d76.authx.account.infrastructure.security.federation.adapter.FederatedOidcAdapter;
import dev.d76.authx.account.infrastructure.security.filter.JwtFilter;
import dev.d76.authx.account.infrastructure.security.handler.LoginFailureHandler;
import dev.d76.authx.account.infrastructure.security.handler.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
@RequiredArgsConstructor
public class SecurityConfig {

    private final FederatedOidcAdapter oidcAdapter;
    private final FederatedOAuth2Adapter oAuth2Adapter;
    private final AuthenticationEntryPoint entryPoint;
    private final AccessDeniedHandler deniedHandler;
    private final LoginSuccessHandler successHandler;
    private final LoginFailureHandler failureHandler;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(info -> info
                                .oidcUserService(oidcAdapter)
                                .userService(oAuth2Adapter)
                        )
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Spring OAuth2 internals
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        // Public auth endpoints
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/verify-email").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/federated/register").permitAll()
                        .requestMatchers("/api/auth/federated/link").permitAll()
                        .requestMatchers("/api/auth/password/forgot").permitAll()
                        .requestMatchers("/api/auth/password/reset").permitAll()
                        // Challenge endpoint
                        .requestMatchers("/api/challenges/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Everything else requires a valid access token
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}