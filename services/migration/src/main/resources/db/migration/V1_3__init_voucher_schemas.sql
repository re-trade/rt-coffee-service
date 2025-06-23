SET search_path TO voucher;

CREATE TABLE voucher_category_restrictions
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    voucher_id   VARCHAR(255)                NOT NULL,
    category     VARCHAR(100)                NOT NULL,
    CONSTRAINT pk_voucher_category_restrictions PRIMARY KEY (id)
);

CREATE TABLE voucher_restrictions
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    voucher_id   VARCHAR(255)                NOT NULL,
    product_id   VARCHAR(255),
    CONSTRAINT pk_voucher_restrictions PRIMARY KEY (id)
);

CREATE TABLE voucher_seller_restrictions
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    voucher_id   VARCHAR(255)                NOT NULL,
    seller_id    VARCHAR(50)                 NOT NULL,
    CONSTRAINT pk_voucher_seller_restrictions PRIMARY KEY (id)
);

CREATE TABLE voucher_usages
(
    id               VARCHAR(255)                NOT NULL,
    created_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    voucher_id       VARCHAR(255)                NOT NULL,
    vault_id         VARCHAR(255)                NOT NULL,
    order_id         VARCHAR(255)                NOT NULL,
    user_id          VARCHAR(255)                NOT NULL,
    usage_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    discount_applied DECIMAL                     NOT NULL,
    type             VARCHAR(255)                NOT NULL,
    failure_reason   VARCHAR(255),
    CONSTRAINT pk_voucher_usages PRIMARY KEY (id)
);

CREATE TABLE voucher_vaults
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    account_id   VARCHAR(255)                NOT NULL,
    voucher_id   VARCHAR(255)                NOT NULL,
    status       VARCHAR(255)                NOT NULL,
    claimed_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used_date    TIMESTAMP WITHOUT TIME ZONE,
    expired_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_voucher_vaults PRIMARY KEY (id)
);

CREATE TABLE vouchers
(
    id                VARCHAR(255)                NOT NULL,
    created_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    code              VARCHAR(255)                NOT NULL,
    name              VARCHAR(255)                NOT NULL,
    description       TEXT                        NOT NULL,
    type              VARCHAR(255)                NOT NULL,
    discount          DOUBLE PRECISION            NOT NULL,
    max_discount      DECIMAL                     NOT NULL,
    start_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    exprired_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    activated         BOOLEAN                     NOT NULL,
    max_uses          INTEGER,
    max_uses_per_user INTEGER,
    min_spend         DECIMAL,
    seller_id         VARCHAR(255),
    CONSTRAINT pk_vouchers PRIMARY KEY (id)
);

ALTER TABLE vouchers
    ADD CONSTRAINT uc_vouchers_code UNIQUE (code);

ALTER TABLE voucher_category_restrictions
    ADD CONSTRAINT FK_VOUCHER_CATEGORY_RESTRICTIONS_ON_VOUCHER FOREIGN KEY (voucher_id) REFERENCES vouchers (id);

ALTER TABLE voucher_restrictions
    ADD CONSTRAINT FK_VOUCHER_RESTRICTIONS_ON_VOUCHER FOREIGN KEY (voucher_id) REFERENCES vouchers (id);

ALTER TABLE voucher_seller_restrictions
    ADD CONSTRAINT FK_VOUCHER_SELLER_RESTRICTIONS_ON_VOUCHER FOREIGN KEY (voucher_id) REFERENCES vouchers (id);

ALTER TABLE voucher_usages
    ADD CONSTRAINT FK_VOUCHER_USAGES_ON_VAULT FOREIGN KEY (vault_id) REFERENCES voucher_vaults (id);

ALTER TABLE voucher_usages
    ADD CONSTRAINT FK_VOUCHER_USAGES_ON_VOUCHER FOREIGN KEY (voucher_id) REFERENCES vouchers (id);

ALTER TABLE voucher_vaults
    ADD CONSTRAINT FK_VOUCHER_VAULTS_ON_VOUCHER FOREIGN KEY (voucher_id) REFERENCES vouchers (id);