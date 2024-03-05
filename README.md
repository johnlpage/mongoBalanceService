# Getting Started

### Endpoint Examples

Autogenerate a test transaction

```
curl -d '{}'  -H "Content-Type: application/json"   -X POST http://localhost:8080/transaction
```

Load a Specific transaction (TransactionId must be unique and as it's a long supplied as a string to avoid JSON rounding)

```
export TXN='{"transactionId":"170963189769800046","customerId":4835458,"accountId":1611819,"reference":"","ammount":122.60,"transactionDate":"2016-08-13T21:28:04.639+00:00","transactionType":"DEPOSIT","flags":[],"iban":"XX707976139561090747"}'

curl -d "$TXN"  -H "Content-Type: application/json"   -X POST http://localhost:8080/transaction
```


Bootstrap load a bunch of test traansactions (see appication.properties2)

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

;