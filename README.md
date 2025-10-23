# Service 1: Stock Data Service using Alpha Vantage

Spring Boot service exposing **daily** stock for Amazon via Alpha Vantage.

## Run the API

**Terminal 1**
```
cd service
export ALPHAVANTAGE_API_KEY='<your_api_key>'
mvn spring-boot:run
```

**Terminal 2**

```
curl -i "http://localhost:8080/market/daily?symbol=AMZN"
```


---
