-- Insert TenantContexts
INSERT INTO order_service_schema.tenant_context (tenant_code, tenant_name)
VALUES ('TENANT_1', 'Restaurant A'),
       ('TENANT_2', 'Lounge B');

-- Insert Articles
INSERT INTO order_service_schema.articles (name, unit_of_measure, tenant_context_id, created_at, updated_at)
VALUES ('Burger Bun', 'piece', 1, NOW(), NOW()),
       ('Tomato', 'kg', 2, NOW(), NOW());
