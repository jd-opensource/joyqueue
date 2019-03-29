# JournalQ Terminology

Here is a glossary of term related to JournalQ.

## JournalQ

JournalQ is a message broker which handles and accumulates incoming messages from applications,and then delivers messages to application who is interested in.

## Pub-Sub

A message pattern where senders(publishers) of messages are not programmed to send their messages to specific receivers(subscribers). Decoupling of publishers and subscribers will be benefit for greater scalability and a more dynamic network topology

## Topic

The identity of a message destination on broker.

## Partition

For the sake of scalability, the broker stores all messages of a topic into serveral partitions.

## App

The identity of a message sender(publisher) or receiver(subscriber).

## Producer

A process that publishes message to JournalQ.

## Consumer

A process that subscribes one or more topics and consumes messages published to these topics.


## Naming service

A metadata service which will be used to service discovery by consumers and producers.
