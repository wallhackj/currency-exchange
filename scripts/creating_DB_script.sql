PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS Currencies (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        code VARCHAR(3) UNIQUE NOT NULL,
        fullName VARCHAR NOT NULL,
        sign VARCHAR(5) NOT NULL
);

CREATE TABLE IF NOT EXISTS ExchangeRates (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        baseCurrencyID INTEGER NOT NULL,
        targetCurrencyID INTEGER NOT NULL,
        rate DECIMAL(6,2) NOT NULL,
        FOREIGN KEY (baseCurrencyID) REFERENCES Currencies(id),
        FOREIGN KEY (targetCurrencyID) REFERENCES Currencies(id),
        CONSTRAINT uc_currency_pair UNIQUE (baseCurrencyID, targetCurrencyID)
);

CREATE INDEX IF NOT EXISTS idx_currencies_code ON Currencies (code);
CREATE INDEX IF NOT EXISTS idx_exchange_rates_base_currency_id ON ExchangeRates (baseCurrencyID);
CREATE INDEX IF NOT EXISTS idx_exchange_rates_target_currency_id ON ExchangeRates (targetCurrencyID);

INSERT INTO Currencies (code, fullName, sign) VALUES ('AUD','Australian dollar', 'A$');
INSERT INTO Currencies (code, fullName, sign) VALUES ('EUR','Euro', '€');
INSERT INTO ExchangeRates (baseCurrencyID, targetCurrencyID, rate) VALUES (2,1,1.63);
