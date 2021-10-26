package com.example.rabbit.handler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerHandler {
    String listenerId;

    public ConsumerHandler(String listenerId) {
        this.listenerId = listenerId;
    }

    public void handleMessage(String text) {
        log.info("Received from queue '{}' ------------------------ {}" , listenerId, text);
    }
}
