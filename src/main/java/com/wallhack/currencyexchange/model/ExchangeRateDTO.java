package com.wallhack.currencyexchange.model;

import java.math.BigDecimal;

public record ExchangeRateDTO(long id, int baseCurrency, int targetCurrency, BigDecimal rate) {}
