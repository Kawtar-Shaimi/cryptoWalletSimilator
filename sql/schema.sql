CREATE DATABASE cryptowallet;

CREATE TABLE IF NOT EXISTS wallets (
    pk BIGSERIAL PRIMARY KEY,
    id VARCHAR(64) UNIQUE NOT NULL,
    type VARCHAR(32) NOT NULL,
    address VARCHAR(128) NOT NULL,
    balance NUMERIC(38, 18) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    pk BIGSERIAL PRIMARY KEY,
    id VARCHAR(64) UNIQUE NOT NULL,
    from_address VARCHAR(128) NOT NULL,
    to_address VARCHAR(128) NOT NULL,
    amount NUMERIC(38, 18) NOT NULL,
    fee_priority VARCHAR(32) NOT NULL,
    fee_amount NUMERIC(38, 18) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    wallet_id VARCHAR(64) NOT NULL REFERENCES wallets(id)
);
