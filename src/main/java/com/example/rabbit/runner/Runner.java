package com.example.rabbit.runner;

import com.example.rabbit.service.RabbitQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class Runner implements CommandLineRunner {

    @Autowired
    RabbitQueueService rabbitQueueService;

    @Override
    public void run(String... args) throws Exception {
        log.info("running....");

        rabbitQueueService.addNewQueue("test1","test1", "test1");
        rabbitQueueService.addNewQueue("test2","test2", "test2");
        rabbitQueueService.addNewQueue("test3","test3", "test3");
    }
}
