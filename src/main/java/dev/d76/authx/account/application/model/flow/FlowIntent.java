package dev.d76.authx.account.application.model.flow;

public enum FlowIntent {
    OPEN_ACCOUNT,   // no account exists for this usernameOrEmail — federated registration required
    LINK_IDENTITY   // account exists but provider not linked — linking required to proceed
}