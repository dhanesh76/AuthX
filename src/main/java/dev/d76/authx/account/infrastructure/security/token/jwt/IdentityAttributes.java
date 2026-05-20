package dev.d76.authx.account.infrastructure.security.token.jwt;

public final class IdentityAttributes {
    private IdentityAttributes() {}

    // present on every token — verified first to prevent token substitution
    public static final String TOKEN_TYPE = "typ";

    // ACCESS token claims
    public static final String EMAIL = "usernameOrEmail";
    public static final String ROLES = "roles";

    // FLOW token claims
    public static final String IDENTITY_PROVIDER = "idp";
    public static final String FLOW_TOKEN        = "flowToken";
    public static final String FLOW_INTENT       = "fin";

    // RE_AUTH token claims
    public static final String RE_AUTH_INTENT = "rin";
}