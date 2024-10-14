-- Insert TenantContexts
INSERT INTO order_service_schema.tenant_context (tenant_code, tenant_name)
VALUES ('T1', 'Restaurant A'),
       ('T2', 'Lounge B');

-- Insert Articles
INSERT INTO order_service_schema.articles (name, unit_of_measure, tenant_context_id, created_at, updated_at)
VALUES ('Burger Bun', 'piece', 1, NOW(), NOW()),
       ('Tomato', 'kg', 2, NOW(), NOW());

INSERT INTO order_service_schema.tenant_category (tenant_code, enabled_category)
VALUES
    ('T1', '["Burger", "Pizza", "Noodles"]'),
    ('T2', '["Pancakes", "Cheese"]');
