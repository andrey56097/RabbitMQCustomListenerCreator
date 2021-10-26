package com.example.rabbit.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMqTestSetRunnerConsumerService {

    @RabbitListener(id = "test-set-runner-exchange",queues = {"test-set-runner-queue"},concurrency = "2")
    public void receiver(Long testSetId) {
        log.info("received Message from rabbit : " + testSetId);
        try {
            log.info("completed " + testSetId + " task");
        } catch (Exception e) {
            log.error("Error on running test set");
        }
    }
}
