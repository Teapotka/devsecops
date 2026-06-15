CREATE TABLE IF NOT EXISTS user_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    user_account_id UUID NOT NULL,
    product_name VARCHAR(180) NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_orders_user_account
        FOREIGN KEY (user_account_id)
        REFERENCES user_accounts(id)
        ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS idx_orders_user_account_id ON orders(user_account_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_user_accounts_email ON user_accounts(email);