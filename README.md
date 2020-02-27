# DNA Challenge

## API

The API has a single endpoint: /v1/resolver/words/:word and returns the document ids where the given word appears.
It supposed to work from the output file of the indexer but in real world I would store that data in a NoSql database(Cassandra) and serve the resolution request from there for the following reasons:
- if we load the data before the server starts, we won't not able to respond to requests during the data load - availability issue
- if we load the data on the first request and cache it, the first request would be very slow or even times out - responsive issue

The solution does not contain database interaction(hard coded values inside repositories), however it contains an example for resource(and configuration) handling in a functional way(Postgres connection)

To run the server:

```
sbt "runMain com.dowjones.Main"
```

Request example:

```
curl http://0.0.0.0:8080/v1/resolver/words/the
```

Expected output:

```
["0","2","12"]
```

## Jobs
To build package run:

```
sbt pack
```

To run the dictionary creation job:

```
target/pack/bin/dictionary --output=dic
```

To run the indexer job:

```
target/pack/bin/indexer --output=out
```

### Testing

To run tests:

```
sbt test
```
