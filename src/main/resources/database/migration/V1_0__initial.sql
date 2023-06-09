CREATE TABLE IF NOT EXISTS products(
    id UUID NOT NULL,
 	"name" VARCHAR(100) NOT NULL,
    "product_type" VARCHAR(20) NOT NULL,
 	enabled BOOLEAN DEFAULT true,
    creation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deletion_time TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT Unique_Name UNIQUE (name));
