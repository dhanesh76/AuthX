package dev.d76.authx.account.application.model.flow;

public enum ReAuthIntent {
    CONFIRM_SENSITIVE_ACTION,   // re-authentication before password change
    AUTHORIZE_DELETION          // re-authentication before account deletion (not yet implemented)
}