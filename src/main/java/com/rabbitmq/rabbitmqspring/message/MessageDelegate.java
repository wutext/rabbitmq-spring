package com.rabbitmq.rabbitmqspring.message;

public class MessageDelegate {

    public void handleMessage(String message) {
        System.out.println("莫热门方法："+message.toString());
    }
}
