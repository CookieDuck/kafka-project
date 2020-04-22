package com.github.CookieDuck.kafkaproject.controller;

import com.github.CookieDuck.kafkaproject.model.Message;
import com.github.CookieDuck.kafkaproject.service.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/messages")
public class ProducerController {
    private final ProducerService service;

    @Autowired
    public ProducerController(ProducerService service) {
        this.service = service;
    }

    @PostMapping
    public void produce(@RequestBody Message message) {
        log.info("Received {} as input", message);
        service.send(message);
    }
}
