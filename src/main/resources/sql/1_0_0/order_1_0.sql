SET search_path TO order_schema;

-- Ajouter à la table orders (existant)
ALTER TABLE orders ADD COLUMN registered_by VARCHAR(200) NOT NULL;

-- Migration pour le système de numérotation robuste des commandes
-- Version: 1.0.0  
-- Description: Ajout du système de numérotation séquentielle avec reset quotidien à 6h

-- 1. Création de la table pour gérer les séquences quotidiennes
CREATE TABLE daily_order_sequence (
    id VARCHAR(60) PRIMARY KEY, -- Format: 'TENANT_CODE_YYYY-MM-DD'
    tenant_code VARCHAR(50) NOT NULL,
    business_date DATE NOT NULL,
    current_sequence INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    -- Contrainte d'unicité pour éviter les doublons
    CONSTRAINT uk_daily_sequence UNIQUE (tenant_code, business_date)
);

-- 2. Index pour optimiser les performances de recherche
CREATE INDEX idx_daily_sequence_tenant ON daily_order_sequence(tenant_code);
CREATE INDEX idx_daily_sequence_date ON daily_order_sequence(business_date);
CREATE INDEX idx_daily_sequence_tenant_date ON daily_order_sequence(tenant_code, business_date);

-- 3. Ajout de la colonne order_number à la table orders
ALTER TABLE orders ADD COLUMN order_number VARCHAR(10);

-- 4. Création d'un index unique pour order_number par tenant et date (créé après mise à jour des données - voir étape 8)

-- 5. Index pour optimiser la recherche par numéro de commande
CREATE INDEX idx_order_search ON orders(tenant_id, order_number, created_at DESC);

-- 6. Mise à jour des commandes existantes avec un numéro temporaire
-- Cette requête donne un numéro basé sur l'ID pour éviter les conflits
UPDATE orders 
SET order_number = LPAD(CAST((id % 99999) AS TEXT), 5, '0') 
WHERE order_number IS NULL;

-- 7. Rendre la colonne NOT NULL après mise à jour
ALTER TABLE orders ALTER COLUMN order_number SET NOT NULL;

-- 8. Approche simplifiée : Ajouter une colonne date normale et un trigger pour la maintenir
ALTER TABLE orders ADD COLUMN created_date DATE;

-- 9. Initialiser la colonne pour les données existantes
UPDATE orders SET created_date = created_at::date WHERE created_date IS NULL;

-- 10. Rendre la colonne NOT NULL
ALTER TABLE orders ALTER COLUMN created_date SET NOT NULL;

-- 11. Créer un trigger pour maintenir automatiquement created_date
CREATE OR REPLACE FUNCTION update_created_date()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_date = NEW.created_at::date;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_created_date
    BEFORE INSERT OR UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_created_date();

-- 12. Maintenant créer l'index unique sur la colonne normale
CREATE UNIQUE INDEX uk_order_number_tenant_date ON orders(tenant_id, order_number, created_date);

-- 13. Commentaires sur les colonnes pour documentation
COMMENT ON TABLE daily_order_sequence IS 'Table pour gérer les séquences de numéros de commande par jour et par tenant';
COMMENT ON COLUMN daily_order_sequence.business_date IS 'Date métier (reset à 6h du matin au lieu de minuit)';
COMMENT ON COLUMN daily_order_sequence.current_sequence IS 'Séquence actuelle pour ce tenant à cette date';
COMMENT ON COLUMN orders.order_number IS 'Numéro de commande affiché au client (5 chiffres, reset quotidien à 6h)';
COMMENT ON COLUMN orders.created_date IS 'Date de création maintenue par trigger (utilisée pour l''index unique)';