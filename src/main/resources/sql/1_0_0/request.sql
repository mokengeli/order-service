SET search_path TO order_schema;
-- Ajouter à la table orders
ALTER TABLE orders ADD COLUMN registered_by VARCHAR(200) NOT NULL;