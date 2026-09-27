ALTER TABLE product ADD COLUMN purchase_year INTEGER;
ALTER TABLE product ADD COLUMN attributes JSONB NOT NULL DEFAULT '{}'::jsonb;

ALTER TABLE product ADD CONSTRAINT ck_product_purchase_year
    CHECK (purchase_year IS NULL OR purchase_year BETWEEN 1900 AND 2100);
ALTER TABLE product ADD CONSTRAINT ck_product_attributes_object
    CHECK (jsonb_typeof(attributes) = 'object');
