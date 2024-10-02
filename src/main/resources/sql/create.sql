-- Create schema
CREATE SCHEMA IF NOT EXISTS order_service_schema;

-- TenantContext Table
CREATE TABLE order_service_schema.tenant_context (
                                                     id SERIAL PRIMARY KEY,
                                                     tenant_code VARCHAR(255) NOT NULL UNIQUE,
                                                     tenant_name VARCHAR(255) NOT NULL
);

-- Articles Table
CREATE TABLE order_service_schema.articles (
                                               id SERIAL PRIMARY KEY,
                                               name VARCHAR(255) NOT NULL,
                                               unit_of_measure VARCHAR(50) NOT NULL,
                                               tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                               created_at TIMESTAMP NOT NULL,
                                               updated_at TIMESTAMP
);

-- Dishes Table
CREATE TABLE order_service_schema.dishes (
                                             id SERIAL PRIMARY KEY,
                                             name VARCHAR(255) NOT NULL,
                                             current_price DECIMAL(10, 2) NOT NULL,
                                             tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                             created_at TIMESTAMP NOT NULL,
                                             updated_at TIMESTAMP
);

-- Dish Articles Table (for composite dishes)
CREATE TABLE order_service_schema.dish_articles (
                                                    dish_id INT NOT NULL REFERENCES order_service_schema.dishes(id) ON DELETE CASCADE,
                                                    article_id INT NOT NULL REFERENCES order_service_schema.articles(id),
                                                    quantity DOUBLE PRECISION NOT NULL,
                                                    removable BOOLEAN DEFAULT TRUE,
                                                    PRIMARY KEY (dish_id, article_id)
);

-- Menus Table
CREATE TABLE order_service_schema.menus (
                                            id SERIAL PRIMARY KEY,
                                            name VARCHAR(255) NOT NULL,
                                            price DECIMAL(10, 2) NOT NULL,
                                            tenant_context_id INT NOT NULL REFERENCES order_service_schema.tenant_context(id),
                                            created_at TIMESTAMP NOT NULL,
                                            updated_at TIMESTAMP
);

-- Menu Dishes Table (for dishes inside a menu)
CREATE TABLE order_service_schema.menu_dishes (
                                                  menu_id INT NOT NULL REFERENCES order_service_schema.menus(id) ON DELETE CASCADE,
                                                  dish_id INT NOT NULL REFERENCES order_service_schema.dishes(id),
                                                  PRIMARY KEY (menu_id, dish_id)
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
