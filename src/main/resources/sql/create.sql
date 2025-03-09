-- Create schema
CREATE SCHEMA IF NOT EXISTS order_service_schema;

-- TenantContext Table
CREATE TABLE order_service_schema.tenant_context (
                                                     id SERIAL PRIMARY KEY,
                                                     tenant_code VARCHAR(255) NOT NULL UNIQUE,
                                                     tenant_name VARCHAR(255) NOT NULL
);
--
-- Create the ref_table linked to tenant
CREATE TABLE order_service_schema.ref_tables (
                           id SERIAL PRIMARY KEY,
                           tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                           name VARCHAR(255) NOT NULL UNIQUE,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP
);

CREATE TABLE order_service_schema.currencies (
                                               id SERIAL PRIMARY KEY,
                                               label VARCHAR(255) NOT NULL UNIQUE,
                                               code VARCHAR(10) NOT NULL UNIQUE
);



-- Dishes Table
CREATE TABLE order_service_schema.dishes (
                                             id SERIAL PRIMARY KEY,
                                             name VARCHAR(255) NOT NULL,
                                             price DECIMAL(10, 2) NOT NULL,
                                             image_url TEXT,
                                             tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                             currency_id INT NOT NULL REFERENCES order_service_schema.currencies(id),
                                             created_at TIMESTAMP NOT NULL,
                                             updated_at TIMESTAMP,
                                             CONSTRAINT unique_dish_per_tenant UNIQUE (name, tenant_context_id)
);

-- Dish products Table (for composite dishes)
CREATE TABLE order_service_schema.dish_products (
                                                    id SERIAL PRIMARY KEY,
                                                    product_id INT NOT NULL,
                                                    quantity DOUBLE PRECISION NOT NULL,
                                                    dish_id INT NOT NULL REFERENCES order_service_schema.dishes(id) ON DELETE CASCADE,
                                                    CONSTRAINT unique_product_id_per_dish_id UNIQUE (product_id, dish_id)

);

-- Menus Table
CREATE TABLE order_service_schema.menus (
                                            id SERIAL PRIMARY KEY,
                                            name VARCHAR(255) NOT NULL,
                                            price DECIMAL(10, 2) NOT NULL,
                                            currency_id INT NOT NULL REFERENCES order_service_schema.currencies(id),
                                            image_url TEXT,
                                            tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                            created_at TIMESTAMP NOT NULL,
                                            updated_at TIMESTAMP
);

-- Menu Dishes Table (for dishes inside a menu)
CREATE TABLE order_service_schema.menu_dishes (
                                                  menu_id INT NOT NULL REFERENCES order_service_schema.menus(id) ON DELETE CASCADE,
                                                  dish_id INT NOT NULL REFERENCES order_service_schema.dishes(id),
                                                  category VARCHAR(20) NOT NULL,
                                                  PRIMARY KEY (menu_id, dish_id)
);

-- New Table: Menu Category Options
-- This table specifies the maximum number of choices allowed for each category within a menu.
CREATE TABLE order_service_schema.menu_category_options (
                                                            menu_id INT NOT NULL REFERENCES order_service_schema.menus(id) ON DELETE CASCADE,
                                                            category VARCHAR(20) NOT NULL,
                                                            max_choices INT NOT NULL,
                                                            PRIMARY KEY (menu_id, category)
);


-- Dish Price History Table (for price versioning)
CREATE TABLE order_service_schema.dish_price_history (
                                                         id SERIAL PRIMARY KEY,
                                                         dish_id INT NOT NULL REFERENCES order_service_schema.dishes(id) ON DELETE CASCADE,
                                                         price DECIMAL(10, 2) NOT NULL,
                                                         start_date TIMESTAMP NOT NULL,
                                                         end_date TIMESTAMP
);

-- Tenant Promotion Table (for promotions specific to a tenant's dishes or menus)
CREATE TABLE order_service_schema.tenant_promotions (
                                                        id SERIAL PRIMARY KEY,
                                                        tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                                        dish_id INT REFERENCES order_service_schema.dishes(id) ON DELETE SET NULL,
                                                        menu_id INT REFERENCES order_service_schema.menus(id) ON DELETE SET NULL,
                                                        discount_percentage DECIMAL(5, 2) NOT NULL,
                                                        start_date TIMESTAMP NOT NULL,
                                                        end_date TIMESTAMP NOT NULL,
                                                        is_active BOOLEAN DEFAULT TRUE
);


-- Categories Table
CREATE TABLE order_service_schema.categories (
                                                 id SERIAL PRIMARY KEY,
                                                 name VARCHAR(255) NOT NULL UNIQUE,
                                                 image_url TEXT,
                                                 created_at TIMESTAMP NOT NULL,
                                                 updated_at TIMESTAMP
);

-- Dish Categories Table (for composite dishes and categories)
CREATE TABLE order_service_schema.dish_categories (
                                                      dish_id INT NOT NULL REFERENCES order_service_schema.dishes(id),
                                                      category_id INT NOT NULL REFERENCES order_service_schema.categories(id),
                                                      PRIMARY KEY (dish_id, category_id)
);

-- Tenant Categories Table (for composite dishes and categories)
CREATE TABLE order_service_schema.tenant_context_categories (
                                                        tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                                        category_id INT NOT NULL REFERENCES order_service_schema.categories(id),
                                                        PRIMARY KEY (tenant_context_id, category_id)
);


CREATE TABLE order_service_schema.orders (
                                             id SERIAL PRIMARY KEY,
                                             ref_table_id INT NOT NULL REFERENCES order_service_schema.ref_tables(id),
                                             total_price DECIMAL(12, 2) NOT NULL,
                                             tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                             currency_id INT NOT NULL REFERENCES order_service_schema.currencies(id),
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP
);

CREATE TABLE order_service_schema.order_items (
                                                  id SERIAL PRIMARY KEY,
                                                  state VARCHAR(50) NOT NULL,
                                                  note TEXT,
                                                  unit_price DECIMAL(12, 2) NOT NULL,
                                                  order_id INT NOT NULL REFERENCES order_service_schema.orders(id),
                                                  dish_id INT REFERENCES order_service_schema.dishes(id),
                                                  currency_id INT NOT NULL REFERENCES order_service_schema.currencies(id),
                                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_service_schema.orders_audit (
                                                   id SERIAL PRIMARY KEY,
                                                   audit_action VARCHAR(50) NOT NULL,  -- For example, "CREATED", "UPDATED", "DELETED"
                                                   old_state VARCHAR(50),
                                                   new_state VARCHAR(50),
                                                   change_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   order_id INT NOT NULL REFERENCES order_service_schema.orders(id),
                                                   tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                                   changed_by VARCHAR(255) NOT NULL  -- Employee number or any identifier for the user who made the change
);







