package com.wallhack.currencyexchange.model;

import java.math.BigDecimal;

public record ExchangeRateDTO(long id, long baseCurrency, long targetCurrency, BigDecimal rate) {}
