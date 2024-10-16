-- Insert TenantContexts
INSERT INTO order_service_schema.tenant_context (tenant_code, tenant_name)
VALUES ('T1', 'Restaurant A'),
       ('T2', 'Lounge B');


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
