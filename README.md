### Project "Currency Exchanger"

A backend project without a web interface based on REST API for describing currency and exchange rates. It allows viewing and editing the list of currencies and exchange rates and calculating amounts in other currency.

Link for tasks for the project: https://zhukovsd.github.io/java-backend-learning-course/Projects/CurrencyExchange/#get-currencies

### Technologies / tools used:
- Java - collection , OOP , Optional
- Maven
- Tomcat 10 
- jakarta servlet-uri
- HTTP - requests GET  POST, HTTP codes
- SQL, JDBC
- REST API, JSON
- Postman
- Without frameworks.

### Motivation of the project
- Deep understanding of how some frameworks work , servlest and HTTP protocol
- Implementing Architectural Pattern MVC (Model View Controller) on a real project

API features
Note: Postman collection is in project.

## Currencies
## GET /currencies

#### Returns list of all currencies. Example of response:

<div>
  <pre id="jsonResponse">
[
    {
        "id": 1,
        "code": "AUD",
        "fullName": "Australian dollar",
        "sign": "A$"
    },
    {
        "id": 2,
        "code": "EUR",
        "fullName": "Euro",
        "sign": "€"
    },
  "..."
]
</pre>
</div>

## GET /currency/MDL

#### Returns particular currency. The currency code is specified in the query address Example of response:

<div>
  <pre id="jsonResponse">
[
  {
    "id": 8,
    "code": "MDL",
    "fullName": "Moldovan leu",
    "sign": "L"
}
]
</pre>
</div>

## POST /currencies

#### Adding a new currency to the database. Data is passed in the body of request in the x-www-form-urlencoded. The form fields are name, code, symbol. Example of response (inserted record):

<div>
  <pre id="jsonResponse">
[
  {
    "id": 2,
    "name": "Czech Koruna",
    "code": "CZK",
    "sign": "Kč"
  }
]
</pre>
</div>

## Exchange rates
## GET /exchangeRates
#### Returns list of all exchange rates. Example of response:

<div>
  <pre id="jsonResponse">
[
  {
        "id": 5,
        "baseCurrency": {
            "id": 2,
            "code": "EUR",
            "fullName": "Euro",
            "sign": "€"
        },
        "targetCurrency": {
            "id": 5,
            "code": "USD",
            "fullName": "US Dollar",
            "sign": "$"
        },
        "rate": 1.07
    },
    {
        "id": 6,
        "baseCurrency": {
            "id": 4,
            "code": "GBP",
            "fullName": "Pound Sterling",
            "sign": "£"
        },
        "targetCurrency": {
            "id": 5,
            "code": "USD",
            "fullName": "US Dollar",
            "sign": "$"
        },
        "rate": 1.2686
    },
  "..."
]
</pre>
</div>

## POST /exchangeRates

#### Adding a new exchange rate to the database. Data is passed in the body of request in the x-www-form-urlencoded. The form fields are baseCurrencyCode, targetCurrencyCode, rate. Example of response (inserted record):

<div>
  <pre id="jsonResponse">
[
  {
    "id": 2,
    "baseCurrency": {
      "id": 1,
      "name": "Euro",
      "code": "EUR",
      "sign": "€"
    },
    "targetCurrency": {
      "id": 2,
      "name": "US Dollar",
      "code": "USD",
      "sign": "$"
    },
    "rate": 0.95
  }
]
</pre>
</div>

## GET /exchangeRate/USDEUR

#### Returns a particular exchange rate. The currency pair is specified by consecutive currency codes in the query address. Example of response:

<div>
  <pre id="jsonResponse">
[
  {
    "id": 0,
    "baseCurrency": {
      "id": 0,
      "name": "United States dollar",
      "code": "USD",
      "sign": "$"
    },
    "targetCurrency": {
      "id": 1,
      "name": "Euro",
      "code": "EUR",
      "sign": "€"
    },
    "rate": 0.93
  }
]
</pre>
</div>

## PATCH /exchangeRate/MDLEUR

#### Updates the existing exchange rate in the database. The currency pair is specified by consecutive currency codes in the query address. The data is passed in the body of the request in the x-www-form-urlencoded. The only form field is rate. Example of response (inserted record):

<div>
  <pre id="jsonResponse">
[
  {
    "id": -1,
    "baseCurrency": {
        "id": 8,
        "code": "MDL",
        "fullName": "Moldovan leu",
        "sign": "L"
    },
    "targetCurrency": {
        "id": 2,
        "code": "EUR",
        "fullName": "Euro",
        "sign": "€"
    },
    "rate": 0.054
}
]
</pre>
</div>

## Currency exchange
#### GET /exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT

Calculate the conversion of a particular amount of money from one currency to another. The currency pair and amount is specified in the query address. Example of response:

<div>
  <pre id="jsonResponse">
{
    "exchangeRateDTO": {
        "id": 11,
        "baseCurrency": {
            "id": 2,
            "code": "EUR",
            "fullName": "Euro",
            "sign": "€"
        },
        "targetCurrency": {
            "id": 4,
            "code": "GBP",
            "fullName": "Pound Sterling",
            "sign": "£"
        },
        "rate": 0.86
    },
    "rate": 0.86,
    "amount": 11.0,
    "convertedAmount": 9.46
}
  </pre>
</div>

