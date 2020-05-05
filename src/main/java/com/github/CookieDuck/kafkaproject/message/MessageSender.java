package com.github.CookieDuck.kafkaproject.message;

public interface MessageSender<T> {
    void send(T message);
}
