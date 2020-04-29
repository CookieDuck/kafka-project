# kafka-project
Self-directed study for learning about producing and consuming messages in Kafka
using Spring Boot.  The Spring Boot portion shuffles cards by producing and consuming
messages to and from various kafka topics.  The consumers all run in their own threads.

Generally: the application mimics a person riffle shuffling the cards n times.  
Specifically: When a shuffle request (a list of cards and a number of times to shuffle)
is made, a message is produced to the `deck` topic.  The consumer thread for the `deck`
topic takes the first half of and the second half of the list of cards and produces to
the `top` and `bottom` topics, respectively.  The `top` and `bottom` consumers each
take a small packet (1-3 cards) from their lists, and produce to the `shuffled` topic.
The `shuffled` consumer receives the packets of cards, and waits until all the cards have
arrived.  The `shuffled` consumer then checks to see if there are more shuffles to perform.
If so, it decrements the remaining shuffles to do, and produces this list to the `deck`,
and the process of shuffling occurs again.  Otherwise, the deck has been shuffled the
requested amount of times.  In this case, the `shuffled` consumer produces to the `output`
topic.

The UI served by the application (visit localhost:8080 in a web browser) allows the user
to choose a deck and a number of times to shuffle.  The UI subscribes to Server Sent Events
(SSEs) from the application.  The application sends an SSE to the UI when a message comes
in from the `output` topic.  Clicking the `Shuffle!` button in the UI will trigger the
steps outlined above in the application, culminating in an SSE containing the shuffled deck.
Upon receiving this SSE, the UI will show the results.

## Environment setup
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
1. run `./gradlew`

## Running Application
1. From the project directory, run the bootRun task: `./gradlew bootRun`
1. From a web browser, navigate to localhost:8080
1. localhost:8080 should be serving a webpage with some UI elements.
You can pick how many times to shuffle, pick a starter deck from a handful of choices,
and then click the shuffle button.  The results of the shuffling should be displayed.
