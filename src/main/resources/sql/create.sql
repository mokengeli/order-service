CREATE SCHEMA IF NOT EXISTS order_service_schema;

-- Table to store orders
CREATE TABLE order_service_schema.orders (
                                             id SERIAL PRIMARY KEY,
                                             employee_number VARCHAR(50) NOT NULL,  -- Employee number for the waiter/cooker
                                             table_number VARCHAR(50),  -- Table number (optional)
                                             state VARCHAR(50) NOT NULL,  -- Order state (e.g., PENDING, IN_PREPARATION, REJECTED, etc.)
                                             total_price DECIMAL(10, 2) NOT NULL,  -- Total price of the order
                                             comment TEXT,  -- Optional comment on the order
                                             tenant_code VARCHAR(255) NOT NULL,  -- Tenant identifier for multi-tenancy
                                             created_at TIMESTAMP NOT NULL,
                                             updated_at TIMESTAMP
);

-- Table to store articles (centralized to normalize articles across dishes)
CREATE TABLE order_service_schema.articles (
                                               id SERIAL PRIMARY KEY,
                                               name VARCHAR(255) NOT NULL,  -- Name of the article (e.g., Bread, Steak)
                                               unit_of_measure VARCHAR(50) NOT NULL,  -- Unit of measure (e.g., "kg", "L")
                                               created_at TIMESTAMP NOT NULL,
                                               updated_at TIMESTAMP
);

-- Table to store dishes
CREATE TABLE order_service_schema.dishes (
                                             id SERIAL PRIMARY KEY,
                                             name VARCHAR(255) NOT NULL,  -- Name of the dish (e.g., Thor Hamburger)
                                             created_at TIMESTAMP NOT NULL,
                                             updated_at TIMESTAMP
);

-- Table to store articles used in dishes (normalized, linking to articles table)
CREATE TABLE order_service_schema.dish_articles (
                                                    dish_id INT NOT NULL REFERENCES order_service_schema.dishes(id) ON DELETE CASCADE,  -- Foreign key to the dish
                                                    article_id INT NOT NULL REFERENCES order_service_schema.articles(id),  -- Foreign key to the article
                                                    quantity DOUBLE PRECISION NOT NULL,  -- Quantity of the article used in the dish
                                                    removable BOOLEAN DEFAULT TRUE,  -- Flag to indicate if the article is removable from the dish
                                                    PRIMARY KEY (dish_id, article_id)
);

-- Table to store menus
CREATE TABLE order_service_schema.menus (
                                            id SERIAL PRIMARY KEY,
                                            name VARCHAR(255) NOT NULL,  -- Name of the menu (e.g., Menu Hammer)
                                            price DECIMAL(10, 2) NOT NULL,  -- Price of the menu
                                            created_at TIMESTAMP NOT NULL,
                                            updated_at TIMESTAMP
);

-- Table to store dishes associated with a menu (many-to-many relationship)
CREATE TABLE order_service_schema.menu_dishes (
                                                  menu_id INT NOT NULL REFERENCES order_service_schema.menus(id) ON DELETE CASCADE,  -- Foreign key to the menu
                                                  dish_id INT NOT NULL REFERENCES order_service_schema.dishes(id) ON DELETE CASCADE,  -- Foreign key to the dish
                                                  PRIMARY KEY (menu_id, dish_id)
);

-- Table to store orders that reference both dishes and menus
CREATE TABLE order_service_schema.order_items (
                                                  id SERIAL PRIMARY KEY,
                                                  order_id INT NOT NULL REFERENCES order_service_schema.orders(id) ON DELETE CASCADE,  -- Foreign key to the order
                                                  dish_id INT REFERENCES order_service_schema.dishes(id),  -- Foreign key to the dish
                                                  menu
