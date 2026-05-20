package dev.d76.authx.account.application.port.out;

import dev.d76.authx.account.application.model.flow.FlowIntent;
import dev.d76.authx.account.application.model.flow.ReAuthIntent;
import dev.d76.authx.account.application.model.token.IssuedToken;
import dev.d76.authx.kernel.vo.Email;

import java.util.List;

public interface TokenPort {

    IssuedToken issueAccessToken(AccessTokenRequest request);
    IssuedToken issueFlowToken(FlowTokenRequest request);
    IssuedToken issueReAuthToken(ReAuthTokenRequest request);

    AccessClaims verifyAccessToken(String raw);
    FlowClaims verifyFlowToken(String raw, FlowIntent expectedIntent);
    ReAuthClaims verifyReAuthToken(String raw, Email expectedSubject, ReAuthIntent expectedIntent);

    // ── Request types ─────────────────────────────────────────────────────────

    record AccessTokenRequest(
            String accountId,       // JWT subject — UUID string
            String email,
            List<String> roles
    ) {}

    record FlowTokenRequest(
            Email email,            // JWT subject
            FlowIntent intent,
            String identityProvider
    ) {}

    record ReAuthTokenRequest(
            Email email,            // JWT subject
            ReAuthIntent intent
    ) {}

    // ── Claim types ───────────────────────────────────────────────────────────

    record AccessClaims(
            String accountId,
            String email,
            List<String> roles
    ) {}

    record FlowClaims(
            String email,
            FlowIntent intent,
            String identityProvider
    ) {}

    record ReAuthClaims(
            String email,
            ReAuthIntent intent
    ) {}
}