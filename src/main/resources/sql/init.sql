-- Insert TenantContexts
INSERT INTO order_service_schema.tenant_context (tenant_code, tenant_name)
VALUES ('T1', 'Restaurant A'),
       ('T2', 'Lounge B');

-- Insert TenantContexts
INSERT INTO order_service_schema.ref_tables (name, tenant_context_id, created_at)
VALUES ('TABLE_1', '1', NOW()),
       ('TABLE_2', '1', NOW()),
       ('TABLE_3', '1', NOW()),
       ('TABLE_4', '1', NOW());

INSERT INTO order_service_schema.categories (name, created_at)
VALUES ('Burger', NOW()),
       ( 'Pizza', NOW());

INSERT INTO order_service_schema.currencies (label, code)
VALUES ('Franc Congolais', 'FC'),
       ( 'Dollars', '$');

INSERT INTO order_service_schema.tenant_context_categories (tenant_context_id, category_id)
VALUES
    (1, 1),
    (1, 2);


