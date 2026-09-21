CREATE TABLE social_account (
    social_account_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    member_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_social_account PRIMARY KEY (social_account_id),
    CONSTRAINT fk_social_account_member
        FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT uq_social_account_provider_user
        UNIQUE (provider, provider_user_id),
    CONSTRAINT uq_social_account_member_provider
        UNIQUE (member_id, provider)
);
