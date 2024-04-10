# Getting Started

## Prerequisites

* This requires Maven and Java 17 or higher.
* MongoDB 6.0 or higher as a Replica set - either MongoDB Atlas or start a single node replica set locally.
* Do not use MongoDB atlas cluster sizes < M30 for any sort of performance testing as these are throttled

## Building

There are two parts to this - a web service that implements functionality for maintaining bank balances along with transaction and balance histories and a test harness to provide a test load for that web service. This is supporting material for the blog post published at XXXXX looking at design options and performance tuning for Spring boot based services and MongoDB.

Edit `src/main/java/com/mongodb/johnlpage/mongoBalanceControlles/resources/application.properties` to add your MongoDB conneciton details and also the number of accounts you want in your sample data and the number of transactions per account a call to bootstrap should add on average.
``` 
spring.data.mongodb.uri=mongodb://localhost:27017/bankbalance?replicaSet=repset
spring.data.mongodb.database=bankbalance

mongobalance.johnlpage.nAccounts=100
mongobalance.johnlpage.bootstrapTxnPerAccount=200
```

Build with 

```
 mvn package 
```

Run with 

```
java -jar target/mongoBalanceService-0.0.1-SNAPSHOT.jar
```

To build the testharness use

```
cd testharness && mvn package && cd ..
```

## Background

The service implements multiple versions of the code as different endpoints to allow a single build to compare different models
You can post a transaction document or if you do not the service will generate a random one for test purposes.

The endpoints implemented are

Process a transaction
-----------------

`POST /transaction`

`POST /v2/transaction`

`POT /v3/transaction`

Process many transactions to bulk load data
----------------------------------------

`POST /bootstrap`

`POST /v2/bootstrap`

`POST /v3/bootstrap`

Fetch the balance for an account and the last 10 transactions
---------------------------------------------------------------
`GET /balance/{accountid}`

Fetch a specific transaction
------------------------------

`GET /transaction/{transactionid}`

`GET /transaction/{accountId}/{transactionId}`

The latter case  be used in a sharded cluster to allow us to shard data by accountId as that would be 
the appropriate shard key, not transactionid (unless transacitonid included account information)

Fetch n transactions from an account in reverse time order from the given time and transaction number
--------------------

This is used for paging - the transactionnumer is used to differentiate when two have the saem timestamp

`GET /transactions/{accountId}?fromDate=yyyyMMddHHmmss&fromId=NNNNNNNNNN&n=10`


### Endpoint Examples

Autogenerate a test transaction

```
curl -d '{}'  -H "Content-Type: application/json"   -X POST http://localhost:8080/transaction
```

Load a Specific transaction (TransactionId must be unique and as it's a long supplied as a string to avoid JSON rounding)

```
export TXN='{"transactionId":"170963189769800046","customerId":4835458,"accountId":1611819,"reference":"","amount":122.60,"transactionDate":"2016-08-13T21:28:04.639+00:00","transactionType":"DEPOSIT","flags":[],"iban":"XX707976139561090747"}'

curl -d "$TXN"  -H "Content-Type: application/json"   -X POST http://localhost:8080/transaction
```


Bootstrap load a bunch of test transactions (see appication.properties )

```
curl -d '{}'  -H "Content-Type: application/json"   -X POST http://localhost:8080/bootstrap
```



Get a Transaction

```
curl http://localhost:8080/transaction/170963189769800044
```

Get a Transaction including AcountId to optimse in sharded cluster


```
curl http://localhost:8080/transaction/1611819/170963189769800044
```

Get Balance for an account

```
curl http://localhost:8080/balance/1611819
```

Get a Set of Transactions for an account from a given date,
Pageable by supplying date and id of last transactions

```
# Get last 10
curl "http://localhost:8080/transactions/1000009"
#Get last 3
curl "http://localhost:8080/transactions/1000009?n=3"
#Get 3 from date supplies as YYYYmmDDHHMMSS
curl "http://localhost:8080/transactions/1000009?n=3&fromDate=20231201041259
#Get 3 from date supplies as YYYYmmDDHHMMSS after Transaction supplied (Used when paging)
curl "http://localhost:8080/transactions/1000009?n=3&fromDate=20231201041259&fromId=170963472448900030"
```

## Using the testharness

The testharness code use 

```
cd testharness
java -jar ServiceText.jar [GET_]<url> [ numThreads(4) ] [ numCallsPerThread (1000) ]
```

This will call the specified URL with the specified number of threads the number of calls per thread, it will POST unless the ulr is prefixe by GET_

It will then output max, mean and 95th centile response times as well as total time.

