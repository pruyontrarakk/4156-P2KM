
# StatusCo
A Financial Sentiment Analyzer ingests real-time market and news sentiment score data across tickers; enables stock predictions for the future.

## Building and Running a Local Instance

In order to build and use our service you must install the following (for Mac):
- Maven 3.9.5 - download following the instructions per this link: https://maven.apache.org/download.cgi
- JDK 21 - download following the instructions per this link: https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html
- IntelliJ IDEA - download following the instructions per this link: https://www.jetbrains.com/idea/download/?section=mac
- Python 3.10.9 - download following the instructions per this link: https://www.python.org/downloads/release/python-3109/
- TrendMaster - download following the instructions per this link: https://github.com/hemangjoshi37a/TrendMaster/blob/main/docs/installation.md

## Microservice Architecture
The API architecture is built around three core microservices - **StockDataService**, **NewsDataService**, and **ForecastDataService**. These services are unified through the **CompositeController**, which acts as the central RESTful interface that aggregates their outputs and delivers user-friendly responses to clients.

### 1. StockDataService (and Alpha Vantage)
Spring Boot service exposing daily stock for Amazon via Alpha Vantage.
To operate this particular API service, 
- Run in terminal 1:
```
cd service
export ALPHAVANTAGE_API_KEY='<your_api_key>'
mvn spring-boot:run
```
- Run in terminal 2:
```
curl -i "http://localhost:8080/market/daily?symbol=AMZN"
```
### 2. NewsDataService [TODO]

### 3. ForecastDataService
Spring Boot service that utilizes [Hemang Joshi](https://github.com/hemangjoshi37a)'s open-source library [TrendMaster](https://github.com/hemangjoshi37a/TrendMaster). 

It includes a helper class called PythonService, which executes a Python script running TrendMaster’s forecasting model. 
The service then parses the script’s JSON-formatted output and constructs a Map of dates and predicted prices for the ForecastDataService to use.

## Running Tests
To run our unit tests (located under the directory `src/test`), run the following command in the most outer `service` directory
```
mvn clean test
```

## Endpoints [TODO]

## Style Checking Report [TODO]
The tool "checkstyle" is used to check the style of our code and generate style checking reports. 
The following code can be run in terminal to check the checkstyle report.

1. `mvn checkstyle:check`
2. `mvn checkstyle:checkstyle`

Results: [dont have yet]

## Branch Coverage Report [TODO]
JaCoCo was used to perform branch analysis in order to see the branch coverage.
The following code can be run in terminal to check the checkstyle report.
1. `mvn clean test`
2. `mvn jacoco:report`
3. `open ./target/site/jacoco/index.html`

Results: [dont have yet]

## Static Code Analysis [TODO]
PMD was used to perform static analysis on the codebase.
The following code can be run in terminal to check the PMD report.

1. `brew install pmd`
2. `mvn clean pmd:pmd`
3. `open target/site/pmd.html`

Results: [dont have yet]

## Summary of Tools Used
- [Alpha Vantage](https://www.alphavantage.co/): Used for retrieving relevant stock data.
- [TrendMaster](https://github.com/hemangjoshi37a/TrendMaster): Used to generate stock price forecasts.

## AI Disclosure
AI was used for debugging and testing.

---
