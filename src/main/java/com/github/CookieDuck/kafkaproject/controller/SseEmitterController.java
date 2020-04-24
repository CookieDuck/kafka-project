package com.github.CookieDuck.kafkaproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Controller
@RestController
public class SseEmitterController {
    private final SseEmitter emitter;

    @Autowired
    public SseEmitterController(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @GetMapping(value = "/shuffled")
    public SseEmitter shuffledOutputEmitter() {
        log.info("Requesting events for /shuffled");
        return emitter;
    }
}
