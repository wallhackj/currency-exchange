INSERT INTO Currencies (code, fullName, sign)
VALUES ('USD', 'United States Dollar', '$'),
       ('EUR', 'Euro', '€'),
       ('GBP', 'Pound Sterling', '£');

INSERT INTO ExchangeRates (baseCurrencyID, targetCurrencyID, rate)
VALUES (1, 2, 0.93),
       (1, 3, 0.79);