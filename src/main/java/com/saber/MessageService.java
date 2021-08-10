package com.saber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    @Inject
    RabbitMQClient rabbitMQClient;

    private Channel channel;

    @ConfigProperty(name = "quarkus.rabbitmqclient.exchange")
    private String quarkusExchange;
    @ConfigProperty(name = "quarkus.rabbitmqclient.queue")
    private String quarkusQueue;

    @Inject
    private ObjectMapper objectMapper;


    public void onApplicationStart(@Observes StartupEvent event) {
        // on application start prepare the queus and message listener
        setupQueues();
        setupReceiving();
    }

    private void setupQueues() {
        try {
            // create a connection
            Connection connection = rabbitMQClient.connect();
            // create a channel
            channel = connection.createChannel();

            // declare exchanges and queues
            channel.exchangeDeclare(quarkusExchange, BuiltinExchangeType.TOPIC, true);
            channel.queueDeclare(quarkusQueue, true, false, false, null);
            channel.queueBind(quarkusQueue, quarkusExchange, "#");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setupReceiving() {
        try {
            // register a consumer for messages
            channel.basicConsume(quarkusQueue, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    // just print the received message.
                    log.info("Received message from {}: ==> {} ",quarkusQueue , new String(body, StandardCharsets.UTF_8));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void send(String message) {
        try {
            // send a message to the exchange
            channel.basicPublish(quarkusExchange, "#", null, message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void sendMovie(Movie movie){
        try {

            channel.basicPublish(quarkusExchange, "#", null,objectMapper.writeValueAsString(movie).getBytes(StandardCharsets.UTF_8));
        }catch (IOException ex){
            log.error("Error ====> {}",ex.getMessage());
            throw new UncheckedIOException(ex);
        }
    }
}
