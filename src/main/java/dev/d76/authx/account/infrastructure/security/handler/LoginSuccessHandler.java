package dev.d76.authx.account.infrastructure.security.handler;

import dev.d76.authx.account.application.model.dto.LoginResponse;
import dev.d76.authx.account.application.port.out.RefreshTokenPort;
import dev.d76.authx.account.application.port.out.TokenPort;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.model.Role;
import dev.d76.authx.account.infrastructure.security.federation.FederatedPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenPort tokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        FederatedPrincipal principal = (FederatedPrincipal) authentication.getPrincipal();
        Account.AccountSnapshot snap = Objects.requireNonNull(principal.accountSnapshot());

        var accessToken = tokenPort.issueAccessToken(new TokenPort.AccessTokenRequest(
                snap.accountId().toString(),
                snap.email(),
                snap.roles().stream().map(Role::toAuthority).toList()
        ));

        var body = new LoginResponse(
                snap.accountId().toString(),
                snap.email(),
                accessToken.value(),
                refreshTokenPort.issue(snap.accountId().toString())
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}