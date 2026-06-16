UPDATE users
SET password = '$2a$10$C.7CjDcnNxNhXJvqTGy4e.UyWXfMapnbsxu38ckYzOE9YS87aep16'
WHERE email = 'admin@mercadhar.com';

INSERT INTO categories (name, description) VALUES
('Harinas y granos', 'Harinas, caraotas, lentejas y más'),
('Bebidas', 'Refrescos, jugos y maltas venezolanas'),
('Snacks', 'Chips, galletas y pasapalos'),
('Lácteos', 'Quesos, mantequilla y más'),
('Salsas y condimentos', 'Salsas, aliños y especias venezolanas'),
('Dulces y postres', 'Dulces típicos venezolanos');


INSERT INTO products (name, description, price, image_url, available, category_id) VALUES

-- Harinas y granos
('Harina PAN Blanca', 'Harina de maíz precocida blanca 1kg', 2.50, NULL, true,
    (SELECT id FROM categories WHERE name = 'Harinas y granos')),
('Harina PAN Amarilla', 'Harina de maíz precocida amarilla 1kg', 2.50, NULL, true,
    (SELECT id FROM categories WHERE name = 'Harinas y granos')),
('Caraotas Negras', 'Caraotas negras secas 500g', 1.80, NULL, true,
    (SELECT id FROM categories WHERE name = 'Harinas y granos')),
('Lentejas', 'Lentejas secas 500g', 1.60, NULL, true,
    (SELECT id FROM categories WHERE name = 'Harinas y granos')),

-- Bebidas
('Malta Regional', 'Malta venezolana sin alcohol 330ml', 1.20, NULL, true,
    (SELECT id FROM categories WHERE name = 'Bebidas')),
('Papelón con Limón', 'Bebida natural de papelón 500ml', 1.50, NULL, true,
    (SELECT id FROM categories WHERE name = 'Bebidas')),
('Jugo de Parchita', 'Jugo de maracuyá natural 1L', 2.00, NULL, true,
    (SELECT id FROM categories WHERE name = 'Bebidas')),

-- Snacks
('Tostones de Plátano', 'Tostones precocidos listos para freír 400g', 2.80, NULL, true,
    (SELECT id FROM categories WHERE name = 'Snacks')),
('Chicharrón de Cerdo', 'Chicharrón venezolano crujiente 150g', 3.50, NULL, true,
    (SELECT id FROM categories WHERE name = 'Snacks')),
('Galletas Africanas', 'Galletas dulces venezolanas 200g', 1.90, NULL, true,
    (SELECT id FROM categories WHERE name = 'Snacks')),

-- Lácteos
('Queso Blanco Duro', 'Queso blanco venezolano para rallar 500g', 5.50, NULL, true,
    (SELECT id FROM categories WHERE name = 'Lácteos')),
('Queso de Mano', 'Queso fresco venezolano 400g', 4.80, NULL, true,
    (SELECT id FROM categories WHERE name = 'Lácteos')),
('Mantequilla de Maní', 'Crema de maní natural sin azúcar 400g', 3.20, NULL, true,
    (SELECT id FROM categories WHERE name = 'Lácteos')),

-- Salsas y condimentos
('Salsa Guasacaca', 'Salsa venezolana de aguacate 250g', 3.90, NULL, true,
    (SELECT id FROM categories WHERE name = 'Salsas y condimentos')),
('Aliño Completo', 'Mezcla de especias venezolanas 100g', 2.10, NULL, true,
    (SELECT id FROM categories WHERE name = 'Salsas y condimentos')),

-- Dulces y postres
('Bienmesabe', 'Dulce de coco venezolano 300g', 4.20, NULL, true,
    (SELECT id FROM categories WHERE name = 'Dulces y postres')),
('Conserva de Coco', 'Dulce tradicional de coco 200g', 2.90, NULL, true,
    (SELECT id FROM categories WHERE name = 'Dulces y postres')),
('Golfeado', 'Pan dulce venezolano con papelón 2 unidades', 3.00, NULL, true,
    (SELECT id FROM categories WHERE name = 'Dulces y postres'));

-- Time slots de ejemplo
INSERT INTO time_slots (date, start_time, end_time, max_orders, current_orders, available) VALUES
('2026-06-17', '10:00', '11:00', 5, 0, true),
('2026-06-17', '11:00', '12:00', 5, 0, true),
('2026-06-17', '12:00', '13:00', 5, 0, true),
('2026-06-17', '17:00', '18:00', 5, 0, true),
('2026-06-17', '18:00', '19:00', 5, 0, true),
('2026-06-18', '10:00', '11:00', 5, 0, true),
('2026-06-18', '11:00', '12:00', 5, 0, true),
('2026-06-18', '17:00', '18:00', 5, 0, true);