-- Insert TenantContexts
INSERT INTO order_service_schema.tenant_context (tenant_code, tenant_name)
VALUES ('T1', 'Restaurant A'),
       ('T2', 'Lounge B');

-- Insert Articles
INSERT INTO order_service_schema.articles (name, unit_of_measure, tenant_context_id, created_at, updated_at)
VALUES ('Burger Bun', 'piece', 1, NOW(), NOW()),
       ('Tomato', 'kg', 2, NOW(), NOW());



INSERT INTO order_service_schema.categories (name, created_at)
VALUES ('Burger', NOW()),
       ( 'Pizza', NOW());

INSERT INTO order_service_schema.currencies (label, code)
VALUES ('Franc Congolais', "FC"),
       ( 'Dollars', "$");

INSERT INTO order_service_schema.tenant_context_categories (tenant_context_id, category_id)
VALUES
    (1, 1),
    (1, 2);
