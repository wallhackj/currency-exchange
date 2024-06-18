package com.wallhack.currencyexchange.model;

public record ExchangeDTO(ExchangeRateDTO exchangeRateDTO, float rate, float amount, float convertedAmount) {}
