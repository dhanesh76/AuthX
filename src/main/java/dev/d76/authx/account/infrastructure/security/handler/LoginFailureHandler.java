package dev.d76.authx.account.infrastructure.security.handler;

import dev.d76.authx.account.application.model.flow.FlowIntent;
import dev.d76.authx.account.application.port.out.TokenPort;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.infrastructure.security.federation.exception.FederatedLoginException;
import dev.d76.authx.account.infrastructure.security.token.jwt.IdentityAttributes;
import dev.d76.authx.kernel.vo.Email;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.d76.spring.exception.autoconfigure.web.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;
    private final TokenPort tokenPort;

    @Override
    public void onAuthenticationFailure(@NonNull HttpServletRequest request,
                                        HttpServletResponse response,
                                        @NonNull AuthenticationException exception) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (exception instanceof FederatedLoginException ex) {
            handleFederatedFlow(request, response, ex);
        } else {
            writeError(request, response, AccountErrorCode.INVALID_CREDENTIALS);
        }
    }

    private void handleFederatedFlow(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FederatedLoginException ex) throws IOException {

        switch (ex.decision()) {

            case REQUIRE_REGISTRATION -> {
                var flowToken = tokenPort.issueFlowToken(new TokenPort.FlowTokenRequest(
                        new Email(ex.email()),
                        FlowIntent.OPEN_ACCOUNT,
                        ex.provider().name()
                ));

                var apiError = ApiErrorResponse
                        .builderFrom(AccountErrorCode.ACCOUNT_NOT_FOUND, request)
                        .extension(IdentityAttributes.FLOW_TOKEN, flowToken.value())
                        .build();

                response.setStatus(AccountErrorCode.ACCOUNT_NOT_FOUND.getHttpStatus());
                objectMapper.writeValue(response.getWriter(), apiError);
            }

            case REQUIRE_LINKING -> {
                var flowToken = tokenPort.issueFlowToken(new TokenPort.FlowTokenRequest(
                        new Email(ex.email()),
                        FlowIntent.LINK_IDENTITY,
                        ex.provider().name()
                ));

                var apiError = ApiErrorResponse
                        .builderFrom(AccountErrorCode.PROVIDER_NOT_LINKED, request)
                        .extension(IdentityAttributes.FLOW_TOKEN, flowToken.value())
                        .build();

                response.setStatus(AccountErrorCode.PROVIDER_NOT_LINKED.getHttpStatus());
                objectMapper.writeValue(response.getWriter(), apiError);
            }

            case BLOCKED -> writeError(request, response, AccountErrorCode.ACCOUNT_BLOCKED);

            case ALLOW_LOGIN -> throw new IllegalStateException(
                    "FederatedLoginException must never carry ALLOW_LOGIN"
            );
        }
    }

    private void writeError(HttpServletRequest request,
                            HttpServletResponse response,
                            AccountErrorCode errorCode) throws IOException {

        var errorResponse = ApiErrorResponse.builderFrom(errorCode, request).build();

        response.setStatus(errorCode.getHttpStatus());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}