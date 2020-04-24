package com.github.CookieDuck.kafkaproject.controller;

import com.github.CookieDuck.kafkaproject.model.ShuffleRequest;
import com.github.CookieDuck.kafkaproject.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards")
public class CardsController {
    private final CardService service;

    @Autowired
    public CardsController(CardService service) {
        this.service = service;
    }

    @PostMapping("/shuffle")
    public void shuffle(@RequestBody ShuffleRequest request) {
        this.service.shuffle(request);
    }
}
