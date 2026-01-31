package d76.app.auth.model;

import d76.app.auth.exception.AuthErrorCode;
import d76.app.core.exception.BusinessException;

public enum IdentityProvider {
    EMAIL,
    GOOGLE,
    GITHUB;

    public static IdentityProvider fromClient(String value) {
        if (value == null)
            throw new BusinessException(AuthErrorCode.INVALID_AUTH_PROVIDER, "Auth provider is missing");

        try {
            return IdentityProvider.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_AUTH_PROVIDER, "Invalid Authentication Provider:" + value);
        }
    }
}
