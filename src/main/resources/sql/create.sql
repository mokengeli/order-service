/* =============================================================
   SCHÉMA
   =============================================================*/
CREATE SCHEMA IF NOT EXISTS order_schema;
SET search_path TO order_schema;

/* =============================================================
   TENANTS
   =============================================================*/
CREATE TABLE tenants (
                         id           BIGSERIAL PRIMARY KEY,
                         code  VARCHAR(255) NOT NULL UNIQUE,
                         name  VARCHAR(255) NOT NULL
);

/* =============================================================
   REF TABLES (Tables physiques du resto)
   =============================================================*/
CREATE TABLE ref_tables (
                            id                BIGSERIAL PRIMARY KEY,
                            tenant_id BIGINT    NOT NULL REFERENCES tenants(id),
                            name              VARCHAR(255) NOT NULL,
                            created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                            updated_at        TIMESTAMP WITH TIME ZONE,
                            CONSTRAINT unique_name_per_tenant UNIQUE (name, tenant_id)
);

/* =============================================================
   DEVISES
   =============================================================*/
CREATE TABLE currencies (
                            id    BIGSERIAL PRIMARY KEY,
                            label VARCHAR(255) NOT NULL UNIQUE,
                            code  VARCHAR(10)  NOT NULL UNIQUE     -- ISO‑4217
);

/* =============================================================
   DISHES
   =============================================================*/
CREATE TABLE dishes (
                        id                BIGSERIAL PRIMARY KEY,
                        name              VARCHAR(255) NOT NULL,
                        price             NUMERIC(14,3) NOT NULL,
                        tenant_id BIGINT NOT NULL REFERENCES tenants(id),
                        currency_id       BIGINT NOT NULL REFERENCES currencies(id),
                        created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                        updated_at        TIMESTAMP WITH TIME ZONE,
                        CONSTRAINT unique_dish_per_tenant UNIQUE (name, tenant_id)
);

/* Pagination + vérif appartenance d’un plat */
CREATE INDEX IF NOT EXISTS idx_dishes_tenant_id
    ON dishes (tenant_id, id);

/* =============================================================
   DISH_PRODUCTS  (composition d’un plat)
   =============================================================*/
CREATE TABLE dish_products (
                               id         BIGSERIAL PRIMARY KEY,
                               product_id BIGINT       NOT NULL,                    -- FK vers inventaire
                               quantity   DOUBLE PRECISION NOT NULL,
                               dish_id    BIGINT NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
                               CONSTRAINT unique_product_id_per_dish_id UNIQUE (product_id, dish_id)
);

/* =============================================================
   MENUS
   =============================================================*/
CREATE TABLE menus (
                       id                BIGSERIAL PRIMARY KEY,
                       name              VARCHAR(255) NOT NULL,
                       price             NUMERIC(10,2) NOT NULL,
                       currency_id       BIGINT NOT NULL REFERENCES currencies(id),
                       tenant_id BIGINT NOT NULL REFERENCES tenants(id),
                       created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                       updated_at        TIMESTAMP WITH TIME ZONE
);

/* =============================================================
   MENU_DISHES  (plats contenus dans un menu)
   =============================================================*/
CREATE TABLE menu_dishes (
                             menu_id   BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
                             dish_id   BIGINT NOT NULL REFERENCES dishes(id),
                             category  VARCHAR(20) NOT NULL,
                             PRIMARY KEY (menu_id, dish_id)
);

/* Recherche rapide des plats par catégorie dans un menu */
CREATE INDEX IF NOT EXISTS idx_menu_dishes_category
    ON menu_dishes (menu_id, category, dish_id);

/* =============================================================
   MENU_CATEGORY_OPTIONS  (règles de choix)
   =============================================================*/
CREATE TABLE menu_category_options (
                                       menu_id     BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
                                       category    VARCHAR(20) NOT NULL,
                                       max_choices INT NOT NULL,
                                       PRIMARY KEY (menu_id, category)
);

/* =============================================================
   DISH_PRICE_HISTORY
   =============================================================*/
CREATE TABLE dish_price_history (
                                    id        BIGSERIAL PRIMARY KEY,
                                    dish_id   BIGINT NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
                                    price     NUMERIC(14,3) NOT NULL,
                                    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
                                    end_date   TIMESTAMP WITH TIME ZONE
);

/* Historique récent par plat */
CREATE INDEX IF NOT EXISTS idx_dish_price_hist
    ON dish_price_history (dish_id, start_date DESC);

/* =============================================================
   TENANT_PROMOTIONS
   =============================================================*/
CREATE TABLE tenants_promotions (
                                   id                BIGSERIAL PRIMARY KEY,
                                   tenant_id BIGINT NOT NULL REFERENCES tenants(id),
                                   dish_id           BIGINT REFERENCES dishes(id) ON DELETE SET NULL,
                                   menu_id           BIGINT REFERENCES menus(id) ON DELETE SET NULL,
                                   discount_percentage NUMERIC(5,2) NOT NULL,
                                   start_date        TIMESTAMP WITH TIME ZONE NOT NULL,
                                   end_date          TIMESTAMP WITH TIME ZONE NOT NULL,
                                   is_active         BOOLEAN DEFAULT TRUE
);

/* =============================================================
   CATEGORIES
   =============================================================*/
CREATE TABLE categories (
                            id         BIGSERIAL PRIMARY KEY,
                            name       VARCHAR(255) NOT NULL UNIQUE,
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                            updated_at TIMESTAMP WITH TIME ZONE
);

/* =============================================================
   DISH_CATEGORIES  (plat ↔️ catégorie)
   =============================================================*/
CREATE TABLE dish_categories (
                                 dish_id     BIGINT NOT NULL REFERENCES dishes(id),
                                 category_id BIGINT NOT NULL REFERENCES categories(id),
                                 PRIMARY KEY (dish_id, category_id)
);

/* Plats d'une catégorie */
CREATE INDEX IF NOT EXISTS idx_dish_cat_cat
    ON dish_categories (category_id, dish_id);

/* =============================================================
   tenants_CATEGORIES  (catégories visibles par tenants)
   =============================================================*/
CREATE TABLE tenants_categories (
                                           tenant_id BIGINT NOT NULL REFERENCES tenants(id),
                                           category_id       BIGINT NOT NULL REFERENCES categories(id),
                                           PRIMARY KEY (tenant_id, category_id)
);

/* Unicité + pagination par tenants */
CREATE UNIQUE INDEX IF NOT EXISTS idx_tenant_cat
    ON tenants_categories (tenant_id, category_id);

/* =============================================================
   ORDERS
   =============================================================*/
CREATE TABLE orders (
                        id                BIGSERIAL PRIMARY KEY,
                        ref_table_id      BIGINT NOT NULL REFERENCES ref_tables(id),
                        total_price       NUMERIC(14,3) NOT NULL,
                        tenant_id BIGINT NOT NULL REFERENCES tenants(id),
                        currency_id       BIGINT NOT NULL REFERENCES currencies(id),
                        payment_status    VARCHAR(50)  NOT NULL DEFAULT 'UNPAID',
                        paid_amount       NUMERIC(14,3) NOT NULL DEFAULT 0.0,
                        created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                        updated_at        TIMESTAMP WITH TIME ZONE
);

/* Index existants + nouveaux pour reporting */
CREATE INDEX IF NOT EXISTS idx_orders_tenant_id
    ON orders (tenant_id);

CREATE INDEX IF NOT EXISTS idx_orders_ref_table_id
    ON orders (ref_table_id);

CREATE INDEX IF NOT EXISTS idx_orders_payment_status
    ON orders (payment_status);

CREATE INDEX IF NOT EXISTS idx_orders_tenant_created
    ON orders (tenant_id, created_at);

/* =============================================================
   ORDER_ITEMS
   =============================================================*/
CREATE TABLE order_items (
                             id          BIGSERIAL PRIMARY KEY,
                             state       VARCHAR(50) NOT NULL,
                             note        TEXT,
                             unit_price  NUMERIC(12,2) NOT NULL,
                             order_id    BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             dish_id     BIGINT REFERENCES dishes(id),
                             currency_id BIGINT NOT NULL REFERENCES currencies(id),
                             created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                             updated_at        TIMESTAMP WITH TIME ZONE
);

/* Agrégats dashboard & filtrage par état */
CREATE INDEX IF NOT EXISTS idx_item_state_created
    ON order_items (state, created_at);

CREATE INDEX IF NOT EXISTS idx_item_dish_state
    ON order_items (dish_id, state);

/* =============================================================
   PAYMENT_TRANSACTIONS
   =============================================================*/
CREATE TABLE payment_transactions (
                                      id              BIGSERIAL PRIMARY KEY,
                                      order_id        BIGINT NOT NULL REFERENCES orders(id),
                                      amount          NUMERIC(14,3) NOT NULL,
                                      payment_method  VARCHAR(50) NOT NULL,
                                      employee_number VARCHAR(50) NOT NULL,
                                      notes           TEXT,
                                      is_refund       BOOLEAN NOT NULL DEFAULT false,
                                      discount_amount NUMERIC(14,3) NOT NULL DEFAULT 0.0,
                                      created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                                      updated_at        TIMESTAMP WITH TIME ZONE
);

/* Historique de paiement d’une commande */
CREATE INDEX IF NOT EXISTS idx_payment_transactions_order_id
    ON payment_transactions (order_id, created_at DESC);

