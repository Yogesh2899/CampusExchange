-- Optional demo listings. First register seller@thapar.edu through the app.
-- No demo password or administrator account is created by this script.
USE campusexchange;
INSERT IGNORE INTO categories(name) VALUES ('Books'),('Electronics'),('Furniture');
SET @demo_seller=(SELECT id FROM users WHERE email='seller@thapar.edu' LIMIT 1);
INSERT INTO items(seller_id,category_id,title,description,price,item_condition,status,listed_at)
SELECT @demo_seller,c.id,'Database textbook','Used textbook in good condition.',450.00,'GOOD','AVAILABLE',CURRENT_TIMESTAMP(6)
FROM categories c WHERE c.name='Books' AND @demo_seller IS NOT NULL
AND NOT EXISTS(SELECT 1 FROM items WHERE seller_id=@demo_seller AND title='Database textbook');
INSERT INTO items(seller_id,category_id,title,description,price,item_condition,status,listed_at)
SELECT @demo_seller,c.id,'Scientific calculator','Working scientific calculator for campus pickup.',600.00,'LIKE_NEW','AVAILABLE',CURRENT_TIMESTAMP(6)
FROM categories c WHERE c.name='Electronics' AND @demo_seller IS NOT NULL
AND NOT EXISTS(SELECT 1 FROM items WHERE seller_id=@demo_seller AND title='Scientific calculator');
INSERT INTO items(seller_id,category_id,title,description,price,item_condition,status,listed_at)
SELECT @demo_seller,c.id,'Study table','Compact study table with light wear.',1200.00,'FAIR','AVAILABLE',CURRENT_TIMESTAMP(6)
FROM categories c WHERE c.name='Furniture' AND @demo_seller IS NOT NULL
AND NOT EXISTS(SELECT 1 FROM items WHERE seller_id=@demo_seller AND title='Study table');

