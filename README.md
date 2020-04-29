# kafka-project
Self-directed study for learning about producing and consuming messages in Kafka
using Spring Boot.

## Environment setup (option 1)
#### Download and run Kafka locally:
1. Download the latest [Kafka](https://kafka.apache.org/downloads)
1. Follow [Apache's installation instructions](https://kafka.apache.org/quickstart)
1. cd into the directory into which you installed Kafka
1. Start zookeeper: `zookeeper-server-start.sh config/zookeeper.properties`
1. Start a broker: `kafka-server-start.sh config/server.properties`
1. Create the topics: (deck, top, bottom, shuffled, output)
  1. `kafka-topics.sh --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 13 --topic deck`
  1. `kafka-topics.sh --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 13 --topic top`
  1. `kafka-topics.sh --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 13 --topic bottom`
  1. `kafka-topics.sh --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 13 --topic shuffled`
  1. `kafka-topics.sh --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 13 --topic output`

## Installation
1. clone project
1. cd into project directory
1. run`./gradlew

## Running Application
1. From the project directory, run the bootRun task: `./gradlew bootRun`
1. From a web browser, navigate to localhost:8080
1. localhost:8080 should be serving a webpage with some UI elements.
You can pick how many times to shuffle, pick a starter deck from a handful of choices,
and then click the shuffle button.  The results of the shuffling should be displayed.
