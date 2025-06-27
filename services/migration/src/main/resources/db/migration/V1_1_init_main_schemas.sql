SET search_path TO main;

CREATE TABLE account_roles
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    account_id   VARCHAR(255)                NOT NULL,
    role_id      VARCHAR(255)                NOT NULL,
    enabled      BOOLEAN                     NOT NULL,
    CONSTRAINT pk_account_roles PRIMARY KEY (id)
);

CREATE TABLE accounts
(
    id               VARCHAR(255)                NOT NULL,
    created_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    username         VARCHAR(255)                NOT NULL,
    email            VARCHAR(255)                NOT NULL,
    hash_password    VARCHAR(255)                NOT NULL,
    secret           VARCHAR(255)                NOT NULL,
    enabled          BOOLEAN                     NOT NULL,
    locked           BOOLEAN                     NOT NULL,
    two_fa           BOOLEAN                     NOT NULL,
    join_in_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    changed_username BOOLEAN DEFAULT FALSE       NOT NULL,
    last_login       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);

CREATE TABLE brand_categories
(
    brand_id    VARCHAR(255) NOT NULL,
    category_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_brand_categories PRIMARY KEY (brand_id, category_id)
);

CREATE TABLE brands
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name         VARCHAR(255),
    description  VARCHAR(255),
    img_url      VARCHAR(255),
    enabled      BOOLEAN,
    CONSTRAINT pk_brands PRIMARY KEY (id)
);

CREATE TABLE categories
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name         VARCHAR(100)                NOT NULL,
    description  TEXT,
    parent_id    VARCHAR(255),
    visible      BOOLEAN                     NOT NULL,
    enabled      BOOLEAN                     NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE customer_contacts
(
    id            VARCHAR(255)                NOT NULL,
    created_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    customer_name VARCHAR(50)                 NOT NULL,
    phone         VARCHAR(12)                 NOT NULL,
    state         VARCHAR(50)                 NOT NULL,
    country       VARCHAR(20)                 NOT NULL,
    district      VARCHAR(50)                 NOT NULL,
    ward          VARCHAR(50)                 NOT NULL,
    address_line  VARCHAR(255)                NOT NULL,
    name          VARCHAR(50)                 NOT NULL,
    defaulted     BOOLEAN DEFAULT FALSE       NOT NULL,
    type          INTEGER                     NOT NULL,
    customer_id   VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_customer_contacts PRIMARY KEY (id)
);

CREATE TABLE customers
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    first_name   VARCHAR(255),
    last_name    VARCHAR(255),
    phone        VARCHAR(20),
    address      TEXT,
    avatar_url   TEXT,
    gender       INTEGER DEFAULT 1           NOT NULL,
    account_id   VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (id)
);

CREATE TABLE customers_contacts
(
    contacts_id  VARCHAR(255) NOT NULL,
    customers_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_customers_contacts PRIMARY KEY (contacts_id, customers_id)
);

CREATE TABLE login_sessions
(
    id                 VARCHAR(255)                NOT NULL,
    created_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    account_id         VARCHAR(255)                NOT NULL,
    device_fingerprint VARCHAR(255),
    device_name        VARCHAR(255),
    ip_address         VARCHAR(15)                 NOT NULL,
    location           VARCHAR(255),
    user_agent         VARCHAR(255),
    login_time         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_login_sessions PRIMARY KEY (id)
);

CREATE TABLE order_combo_deliveries
(
    id             VARCHAR(255)                NOT NULL,
    created_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    order_combo_id VARCHAR(255)                NOT NULL,
    delivery_code  VARCHAR(255)                NOT NULL,
    delivery_type  VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_order_combo_deliveries PRIMARY KEY (id)
);

CREATE TABLE order_combos
(
    id                   VARCHAR(255)                NOT NULL,
    created_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    seller_id            VARCHAR(255)                NOT NULL,
    grand_price          DECIMAL                     NOT NULL,
    order_destination_id VARCHAR(255)                NOT NULL,
    order_status_id      VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_order_combos PRIMARY KEY (id)
);

CREATE TABLE order_destinations
(
    id            VARCHAR(255)                NOT NULL,
    created_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    order_id      VARCHAR(255)                NOT NULL,
    customer_name VARCHAR(255)                NOT NULL,
    phone         VARCHAR(12)                 NOT NULL,
    state         VARCHAR(20),
    country       VARCHAR(20),
    district      VARCHAR(20),
    ward          VARCHAR(20),
    address_line  VARCHAR(500),
    CONSTRAINT pk_order_destinations PRIMARY KEY (id)
);

CREATE TABLE order_histories
(
    id             VARCHAR(255)                NOT NULL,
    created_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    order_combo_id VARCHAR(255)                NOT NULL,
    seller_id      VARCHAR(255)                NOT NULL,
    status         BOOLEAN                     NOT NULL,
    notes          TEXT,
    created_by     VARCHAR(255),
    CONSTRAINT pk_order_histories PRIMARY KEY (id)
);

CREATE TABLE order_items
(
    id                VARCHAR(255)                NOT NULL,
    created_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    order_id          VARCHAR(255)                NOT NULL,
    product_id        VARCHAR(255)                NOT NULL,
    quantity          INTEGER DEFAULT 1           NOT NULL,
    order_combo_id    VARCHAR(255)                NOT NULL,
    short_description TEXT                        NOT NULL,
    product_name      VARCHAR(128)                NOT NULL,
    background_url    VARCHAR(256)                NOT NULL,
    base_price        DECIMAL                     NOT NULL,
    unit              VARCHAR(10)                 NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id)
);

CREATE TABLE order_statuses
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    code         VARCHAR(20)                 NOT NULL,
    name         VARCHAR(50)                 NOT NULL,
    enabled      BOOLEAN                     NOT NULL,
    CONSTRAINT pk_order_statuses PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id             VARCHAR(255)                NOT NULL,
    created_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    tax_total      DECIMAL                     NOT NULL,
    discount_total DECIMAL                     NOT NULL,
    sub_total      DECIMAL                     NOT NULL,
    shipping_total DOUBLE PRECISION            NOT NULL,
    grand_total    DECIMAL                     NOT NULL,
    customer_id    VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE payment_histories
(
    id                VARCHAR(255)                NOT NULL,
    created_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    order_id          VARCHAR(255)                NOT NULL,
    payment_method_id VARCHAR(255)                NOT NULL,
    payment_total     DECIMAL                     NOT NULL,
    payment_content   VARCHAR(1024),
    payment_code      VARCHAR(20),
    payment_status    VARCHAR DEFAULT 'CREATED'   NOT NULL,
    payment_time      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_payment_histories PRIMARY KEY (id)
);

CREATE TABLE payment_methods
(
    id            VARCHAR(255)                NOT NULL,
    created_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name          VARCHAR(50)                 NOT NULL,
    code          VARCHAR(50)                 NOT NULL,
    description   VARCHAR(500)                NOT NULL,
    img_url       VARCHAR(255)                NOT NULL,
    type          VARCHAR(255)                NOT NULL,
    handler_class VARCHAR(255)                NOT NULL,
    callback_uri  VARCHAR(255)                NOT NULL,
    enabled       BOOLEAN DEFAULT TRUE,
    CONSTRAINT pk_payment_methods PRIMARY KEY (id)
);

CREATE TABLE product_categories
(
    category_id VARCHAR(255) NOT NULL,
    product_id  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_product_categories PRIMARY KEY (category_id, product_id)
);

CREATE TABLE product_price_histories
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    product_id   VARCHAR(255)                NOT NULL,
    old_price    DECIMAL                     NOT NULL,
    new_price    DECIMAL                     NOT NULL,
    update_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_product_price_histories PRIMARY KEY (id)
);

CREATE TABLE product_reviews
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    product_id   VARCHAR(255)                NOT NULL,
    customer_id  VARCHAR(255)                NOT NULL,
    order_id     VARCHAR(255)                NOT NULL,
    vote         DOUBLE PRECISION            NOT NULL,
    content      VARCHAR(255)                NOT NULL,
    status       BOOLEAN                     NOT NULL,
    CONSTRAINT pk_product_reviews PRIMARY KEY (id)
);

CREATE TABLE products
(
    id                   VARCHAR(255)                NOT NULL,
    created_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name                 VARCHAR(255),
    seller_id            VARCHAR(255)                NOT NULL,
    short_description    TEXT                        NOT NULL,
    description          TEXT                        NOT NULL,
    thumbnail            VARCHAR(256),
    img_urls             TEXT[],
    avg_vote             numeric(5, 2) DEFAULT 0     NOT NULL,
    brand_id             VARCHAR(255)                NOT NULL,
    quantity             INTEGER       DEFAULT 0     NOT NULL,
    warranty_expiry_date date,
    condition            SMALLINT      DEFAULT 0     NOT NULL,
    model                VARCHAR(128)                NOT NULL,
    current_price        DECIMAL                     NOT NULL,
    tags                 TEXT[],
    verified             BOOLEAN                     NOT NULL,
    status               SMALLINT      DEFAULT 0     NOT NULL,
    product_parent_id    VARCHAR(255),
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE report_sellers
(
    id                VARCHAR(255)                NOT NULL,
    created_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    seller_id         VARCHAR(255)                NOT NULL,
    type_report       TEXT                        NOT NULL,
    content           TEXT                        NOT NULL,
    resolution_status TEXT                        NOT NULL,
    resolution_detail TEXT                        NOT NULL,
    resolution_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    order_id           VARCHAR(255)                NOT NULL,
    product_id        VARCHAR(255)                NOT NULL,
    admin_id          VARCHAR(255)                NOT NULL,
    customer_id       VARCHAR(255)                NOT NULL,
    image             VARCHAR(255),
    CONSTRAINT pk_report_sellers PRIMARY KEY (id)
);

CREATE TABLE roles
(
    id           VARCHAR(255)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name         VARCHAR(255)                NOT NULL,
    code         VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE sellers
(
    id                       VARCHAR(255)                NOT NULL,
    created_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    shop_name                VARCHAR(50)                 NOT NULL,
    description              TEXT,
    avg_vote                 DOUBLE PRECISION,
    address_line             VARCHAR(100)                NOT NULL,
    district                 VARCHAR(50)                 NOT NULL,
    ward                     VARCHAR(50)                 NOT NULL,
    state                    VARCHAR(50)                 NOT NULL,
    avatar_url               VARCHAR(256),
    background               VARCHAR(256),
    email                    VARCHAR(50)                 NOT NULL,
    phone_number             VARCHAR(12)                 NOT NULL,
    back_side_identity_card  VARCHAR(256),
    front_side_identity_card VARCHAR(256),
    identity_number          VARCHAR(20)                 NOT NULL,
    verified                 BOOLEAN                     NOT NULL,
    identity_verified        SMALLINT       DEFAULT 0    NOT NULL,
    balance                  DECIMAL(19, 2) DEFAULT 0    NOT NULL,
    account_id               VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_sellers PRIMARY KEY (id)
);

CREATE TABLE third_party_authentications
(
    id             VARCHAR(255)                NOT NULL,
    created_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    provider       VARCHAR(255),
    provider_id    VARCHAR(255),
    provider_email VARCHAR(255),
    CONSTRAINT pk_third_party_authentications PRIMARY KEY (id)
);

ALTER TABLE accounts
    ADD CONSTRAINT uc_accounts_email UNIQUE (email);

ALTER TABLE accounts
    ADD CONSTRAINT uc_accounts_secret UNIQUE (secret);

ALTER TABLE accounts
    ADD CONSTRAINT uc_accounts_username UNIQUE (username);

ALTER TABLE customers
    ADD CONSTRAINT uc_customers_account UNIQUE (account_id);

ALTER TABLE customers_contacts
    ADD CONSTRAINT uc_customers_contacts_contacts UNIQUE (contacts_id);

ALTER TABLE order_destinations
    ADD CONSTRAINT uc_order_destinations_order UNIQUE (order_id);

ALTER TABLE payment_methods
    ADD CONSTRAINT uc_payment_methods_callback_uri UNIQUE (callback_uri);

ALTER TABLE payment_methods
    ADD CONSTRAINT uc_payment_methods_code UNIQUE (code);

ALTER TABLE roles
    ADD CONSTRAINT uc_roles_code UNIQUE (code);

ALTER TABLE sellers
    ADD CONSTRAINT uc_sellers_account UNIQUE (account_id);

ALTER TABLE account_roles
    ADD CONSTRAINT FK_ACCOUNT_ROLES_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES accounts (id);

ALTER TABLE account_roles
    ADD CONSTRAINT FK_ACCOUNT_ROLES_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE categories
    ADD CONSTRAINT FK_CATEGORIES_ON_PARENT FOREIGN KEY (parent_id) REFERENCES categories (id);

ALTER TABLE customers
    ADD CONSTRAINT FK_CUSTOMERS_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES accounts (id);

ALTER TABLE customer_contacts
    ADD CONSTRAINT FK_CUSTOMER_CONTACTS_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES customers (id);

ALTER TABLE login_sessions
    ADD CONSTRAINT FK_LOGIN_SESSIONS_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES accounts (id);

ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES customers (id);

ALTER TABLE order_combos
    ADD CONSTRAINT FK_ORDER_COMBOS_ON_ORDER_DESTINATION FOREIGN KEY (order_destination_id) REFERENCES order_destinations (id);

ALTER TABLE order_combos
    ADD CONSTRAINT FK_ORDER_COMBOS_ON_ORDER_STATUS FOREIGN KEY (order_status_id) REFERENCES order_statuses (id);

ALTER TABLE order_combos
    ADD CONSTRAINT FK_ORDER_COMBOS_ON_SELLER FOREIGN KEY (seller_id) REFERENCES sellers (id);

ALTER TABLE order_combo_deliveries
    ADD CONSTRAINT FK_ORDER_COMBO_DELIVERIES_ON_ORDER_COMBO FOREIGN KEY (order_combo_id) REFERENCES order_combos (id);

ALTER TABLE order_destinations
    ADD CONSTRAINT FK_ORDER_DESTINATIONS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE order_histories
    ADD CONSTRAINT FK_ORDER_HISTORIES_ON_ORDER_COMBO FOREIGN KEY (order_combo_id) REFERENCES order_combos (id);

ALTER TABLE order_histories
    ADD CONSTRAINT FK_ORDER_HISTORIES_ON_SELLER FOREIGN KEY (seller_id) REFERENCES sellers (id);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER_COMBO FOREIGN KEY (order_combo_id) REFERENCES order_combos (id);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE payment_histories
    ADD CONSTRAINT FK_PAYMENT_HISTORIES_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE payment_histories
    ADD CONSTRAINT FK_PAYMENT_HISTORIES_ON_PAYMENT_METHOD FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id);

ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brands (id);

ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_PRODUCT_PARENT FOREIGN KEY (product_parent_id) REFERENCES products (id);

ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_SELLER FOREIGN KEY (seller_id) REFERENCES sellers (id);

ALTER TABLE product_price_histories
    ADD CONSTRAINT FK_PRODUCT_PRICE_HISTORIES_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE product_reviews
    ADD CONSTRAINT FK_PRODUCT_REVIEWS_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES customers (id);

ALTER TABLE product_reviews
    ADD CONSTRAINT FK_PRODUCT_REVIEWS_ON_ORDER FOREIGN KEY (order_id) REFERENCES order_combos (id);

ALTER TABLE product_reviews
    ADD CONSTRAINT FK_PRODUCT_REVIEWS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE report_sellers
    ADD CONSTRAINT FK_REPORT_SELLERS_ON_ADMIN FOREIGN KEY (admin_id) REFERENCES accounts (id);

ALTER TABLE report_sellers
    ADD CONSTRAINT FK_REPORT_SELLERS_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES customers (id);

ALTER TABLE report_sellers
    ADD CONSTRAINT FK_REPORT_SELLERS_ON_ODER FOREIGN KEY (order_id) REFERENCES order_combos (id);

ALTER TABLE report_sellers
    ADD CONSTRAINT FK_REPORT_SELLERS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE report_sellers
    ADD CONSTRAINT FK_REPORT_SELLERS_ON_SELLER FOREIGN KEY (seller_id) REFERENCES sellers (id);

ALTER TABLE sellers
    ADD CONSTRAINT FK_SELLERS_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES accounts (id);

ALTER TABLE brand_categories
    ADD CONSTRAINT fk_bracat_on_brand_entity FOREIGN KEY (brand_id) REFERENCES brands (id);

ALTER TABLE brand_categories
    ADD CONSTRAINT fk_bracat_on_category_entity FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE customers_contacts
    ADD CONSTRAINT fk_cuscon_on_customer_contact_entity FOREIGN KEY (contacts_id) REFERENCES customer_contacts (id);

ALTER TABLE customers_contacts
    ADD CONSTRAINT fk_cuscon_on_customer_entity FOREIGN KEY (customers_id) REFERENCES customers (id);

ALTER TABLE product_categories
    ADD CONSTRAINT fk_procat_on_category_entity FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE product_categories
    ADD CONSTRAINT fk_procat_on_product_entity FOREIGN KEY (product_id) REFERENCES products (id);