CREATE TABLE IF NOT EXISTS Currencies (
                                          id SERIAL PRIMARY KEY ,
                                          code VARCHAR(3) UNIQUE NOT NULL,
                                          fullName VARCHAR NOT NULL,
                                          sign VARCHAR(3) NOT NULL
);

CREATE TABLE IF NOT EXISTS ExchangeRates (
                                             id SERIAL PRIMARY KEY,
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