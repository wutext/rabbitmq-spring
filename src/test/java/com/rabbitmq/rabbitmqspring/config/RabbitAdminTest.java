package com.rabbitmq.rabbitmqspring.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitAdminTest {


    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Test
    public void testRabbitAdmin() {
        rabbitAdmin.declareExchange(new DirectExchange("test.direct", false, false));

        rabbitAdmin.declareExchange(new TopicExchange("test.topic", false, false));

        rabbitAdmin.declareExchange(new FanoutExchange("test.fanout", false, false));

        rabbitAdmin.declareQueue(new Queue("test.direct.queue", false));
        rabbitAdmin.declareQueue(new Queue("test.topic.queue", false));
        rabbitAdmin.declareQueue(new Queue("test.fanout.queue", false));

        rabbitAdmin.declareBinding(new Binding("test.direct.queue",
                Binding.DestinationType.QUEUE,
                "test.direct", "direct", new HashMap<>()));

        rabbitAdmin.declareBinding(
                BindingBuilder
                .bind(new Queue("test.topic.queue", false))  // 创建队列
                .to(new TopicExchange("test.topic", false, false)) // 创建交换机
                .with("user.#") //指定路由key
                );

        rabbitAdmin.declareBinding(
                BindingBuilder
                        .bind(new Queue("test.fanout.queue", false))
                        .to(new FanoutExchange("test.fanout", false, false)));

        rabbitAdmin.purgeQueue("test.topic.queue", false);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testTemplate() {

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("text/plain");
        messageProperties.getHeaders().put("001","第一条信息");
        messageProperties.getHeaders().put("002","第二条信息");

        Message message = new Message("Hello Message".getBytes(), messageProperties);

        rabbitTemplate.convertAndSend("topic001", "spring.abc", message);

        rabbitTemplate.convertAndSend("topic001", "spring.amqp", "HelloObject i come here");
        rabbitTemplate.convertAndSend("topic002", "rabbit.abc", "HelloObject 3333 come here");
    }
}
