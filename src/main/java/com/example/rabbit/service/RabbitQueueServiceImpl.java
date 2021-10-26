package com.example.rabbit.service;

import com.example.rabbit.handler.ConsumerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitQueueServiceImpl implements RabbitQueueService {
    @Autowired
    private RabbitAdmin rabbitAdmin;
    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;
    @Autowired
    private ConnectionFactory connectionFactory;

    @Override
    public void addNewQueue(String queueName, String exchangeName, String routingKey) {
        rabbitAdmin.declareExchange(new DirectExchange(exchangeName, true, false));

        Queue queue = new Queue(queueName, true, false, false);
        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, exchangeName, null);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);

        this.addQueueToListener(exchangeName, queueName);
    }

    @Override
    public void addQueueToListener(String listenerId, String queueName) {
        log.info("adding queue : " + queueName + " to listener with id : " + listenerId);
        if (!checkQueueExistOnListener(listenerId, queueName)) {
            this.getMessageListenerContainerById(listenerId).addQueueNames(queueName);
            log.info("queue REGISTERED");
        } else {
            log.info("given queue name : {} not exist on given listener id : {}" + listenerId, queueName, listenerId);
        }
    }

    @Override
    public void removeQueueFromListener(String listenerId, String queueName) {
        log.info("removing queue : " + queueName + " from listener : " + listenerId);
        if (checkQueueExistOnListener(listenerId, queueName)) {
            this.getMessageListenerContainerById(listenerId).removeQueueNames(queueName);
            log.info("deleting queue from rabbit management");
            this.rabbitAdmin.deleteQueue(queueName);
        } else {
            log.info("given queue name : " + queueName + " not exist on given listener id : " + listenerId);
        }
    }

    @Override
    public Boolean checkQueueExistOnListener(String listenerId, String queueName) {
        try {
            log.info("checking queueName : " + queueName + " exist on listener id : " + listenerId);
            log.info("getting queueNames");
            String[] queueNames = this.getMessageListenerContainerById(listenerId).getQueueNames();
            log.info("queueNames : " + String.join(",", queueName));
            if (queueNames != null) {
                log.info("checking " + queueName + " exist on active queues");
                for (String name : queueNames) {
                    log.info("name : " + name + " with checking name : " + queueName);
                    if (name.equals(queueName)) {
                        log.info("queue name exist on listener, returning true");
                        return Boolean.TRUE;
                    }
                }
                return Boolean.FALSE;
            } else {
                log.info("there is no queue exist on listener");
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error on checking queue exist on listener");
            return Boolean.FALSE;
        }
    }

    private AbstractMessageListenerContainer getMessageListenerContainerById(String listenerId) {
        log.info("getting message listener container by id : " + listenerId);

        if (this.rabbitListenerEndpointRegistry.getListenerContainer(listenerId) == null) {

            SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
            factory.setPrefetchCount(1);
            factory.setConsecutiveActiveTrigger(1);
            factory.setConsecutiveIdleTrigger(1);
            factory.setConnectionFactory(connectionFactory);

            SimpleRabbitListenerEndpoint rabbitListenerEndpoint = new SimpleRabbitListenerEndpoint();
            rabbitListenerEndpoint.setId(listenerId);
            rabbitListenerEndpoint.setQueueNames(listenerId);
            rabbitListenerEndpoint.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(listenerId), new Jackson2JsonMessageConverter()));

            this.rabbitListenerEndpointRegistry.registerListenerContainer(rabbitListenerEndpoint, factory);
        }

        AbstractMessageListenerContainer listenerContainer = ((AbstractMessageListenerContainer) this.rabbitListenerEndpointRegistry.getListenerContainer(listenerId));
        listenerContainer.start();

        return listenerContainer;
    }
}
