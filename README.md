# Service 1: Stock Data Service using Alpha Vantage

Spring Boot service exposing **daily** and **intraday** stock for Amazon via Alpha Vantage.

- Daily uses `TIME_SERIES_DAILY`
- Intraday uses `TIME_SERIES_INTRADAY`:
  - **latest**: ~100 most recent bars
  - **month**: full intraday for a specific `YYYY-MM`


## Run the API

**Terminal 1**
```
cd stockDataService
export ALPHAVANTAGE_API_KEY='<your_api_key>'
mvn spring-boot:run
```

**Terminal 2**

Run daily:
```
curl -i http://localhost:8080/amzn/daily
```

Run intraday given a specific month:
```
export MONTH="2024-06"
curl -i -X POST "http://localhost:8080/amzn/intraday/month?month=${MONTH}"
```

Run intraday latest:
```
curl -i -X POST "http://localhost:8080/amzn/intraday/latest"
```


---
