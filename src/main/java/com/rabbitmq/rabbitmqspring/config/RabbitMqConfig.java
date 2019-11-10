package com.rabbitmq.rabbitmqspring.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.rabbitmqspring.message.MessageDelegate;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@ComponentScan("com.rabbitmq.rabbitmqspring.*")
public class RabbitMqConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses("192.168.3.5:5672");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    /**
     * 针对消费者配置
     * 1. 设置交换机类型
     * 2. 将队列并顶到交换机
     *      FanoutExchange: 将消息分发到所有的绑定队列，无routingKey概念
     *      HeadersExchange: 通过添加属性key-value匹配
     *      DirectExchange: 按照routingKey分发到指定队列
     *      TopicExchange: 多关键字匹配
     * @return
     */
    @Bean
    public TopicExchange exchange001() {
        //param-2：是否持久化
        return new TopicExchange("topic001", true, false);
    }

    @Bean
    public Queue queue001(){
        //param-2：是否持久化
        return new Queue("queue001", true);
    }

    @Bean
    public Binding binding001() {
        return BindingBuilder.bind(queue001()).to(exchange001()).with("spring.*");
    }

    @Bean
    public TopicExchange exchange002() {
        return new TopicExchange("topic002", true, false);
    }

    @Bean
    public Queue queue002(){
        //param-2：是否持久化
        return new Queue("queue002", true);
    }

    @Bean
    public Binding binding002() {
        return BindingBuilder.bind(queue002()).to(exchange002()).with("rabbit.*");
    }

    @Bean
    public Queue queue003(){
        //param-2：是否持久化
        return new Queue("queue003", true);
    }

    @Bean
    public Binding binding003() {
        return BindingBuilder.bind(queue003()).to(exchange001()).with("mq.*");
    }

    @Bean
    public Queue queue_image(){
        //param-2：是否持久化
        return new Queue("image_queue", true);
    }

    @Bean
    public Queue queue_pdf(){
        //param-2：是否持久化
        return new Queue("pdf_queue", true);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        return rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        listenerContainer.setQueues(queue001(),queue002(),queue003(),queue_image(),queue_pdf());
        listenerContainer.setConcurrentConsumers(1);
        listenerContainer.setMaxConcurrentConsumers(5);
        listenerContainer.setDefaultRequeueRejected(false);
        listenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        listenerContainer.setConsumerTagStrategy(new ConsumerTagStrategy() {
            @Override
            public String createConsumerTag(String queue) {
                return queue+"_"+ UUID.randomUUID().toString();
            }
        });
        /*listenerContainer.setMessageListener(new ChannelAwareMessageListener() {
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                String mes = new String(message.getBody());
                System.out.println("------消费者"+mes);
            }
        });*/

        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
        listenerContainer.setMessageListener(adapter);

        return listenerContainer;
    }
}
