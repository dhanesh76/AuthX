package d76.app.security.config;

import d76.app.security.access.RestAccessDeniedHandler;
import d76.app.security.access.RestAuthenticationEntryPoint;
import d76.app.security.auth.LoginFailureHandler;
import d76.app.security.auth.LoginSuccessHandler;
import d76.app.security.auth.LogoutSuccessHandler;
import d76.app.security.jwt.JwtFilter;
import d76.app.security.oauth.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@NullMarked
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final LoginSuccessHandler authenticationSuccessHandler;
    private final LoginFailureHandler authenticationFailureHandler;
    private final CustomOidcUserService oidcUserService;
    private final JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity security) {
        security
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(f -> f
                        .loginProcessingUrl("/api/auth/login")
                        .loginPage("/api/auth/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                )
                .logout(l -> l
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(info -> info
                                .oidcUserService(oidcUserService)
                        )
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                )
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/", "/home").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/oauth/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/login/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/verify-otp",
                                "/forgot-password",
                                "/reset-password",
                                "/dashboard",
                                "/security",
                                "/css/**",
                                "/js/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return security.build();
    }
}
