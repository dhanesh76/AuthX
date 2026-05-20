-- AuthX Identity Schema
-- V1 — Initial schema creation

CREATE TABLE accounts (
                          id               UUID                     NOT NULL,
                          email            VARCHAR(255)             NOT NULL,
                          encoded_password VARCHAR(255),
                          status           VARCHAR(255)             NOT NULL,
                          username         VARCHAR(255)             NOT NULL,
                          created_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,

                          CONSTRAINT accounts_pkey PRIMARY KEY (id),
                          CONSTRAINT accounts_email_unique UNIQUE (email),
                          CONSTRAINT accounts_username_unique UNIQUE (username),
                          CONSTRAINT accounts_status_check CHECK (
                              status IN ('VERIFICATION_PENDING', 'ACTIVE', 'BLOCKED')
                              )
);

CREATE TABLE account_identity_providers (
                                            account_id UUID         NOT NULL,
                                            provider   VARCHAR(255),

                                            CONSTRAINT fk_account_identity_providers_account
                                                FOREIGN KEY (account_id) REFERENCES accounts (id),
                                            CONSTRAINT account_identity_providers_provider_check CHECK (
                                                provider IN ('EMAIL', 'GOOGLE', 'GITHUB')
                                                )
);

CREATE TABLE account_roles (
                               account_id UUID         NOT NULL,
                               role       VARCHAR(255),

                               CONSTRAINT fk_account_roles_account
                                   FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE TABLE refresh_tokens (
                                token_hash VARCHAR(64)                 NOT NULL,
                                account_id UUID                        NOT NULL,
                                expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                                issued_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                                revoked_at TIMESTAMP(6) WITH TIME ZONE,

                                CONSTRAINT refresh_tokens_pkey PRIMARY KEY (token_hash)
);

CREATE INDEX idx_refresh_token_account_id ON refresh_tokens (account_id);