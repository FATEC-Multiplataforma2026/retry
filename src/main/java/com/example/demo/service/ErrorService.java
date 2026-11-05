package com.example.demo.service;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Service;

@Service
public class ErrorService {

    public void  onError() {
        throw new AmqpRejectAndDontRequeueException("Error");
    }

}
