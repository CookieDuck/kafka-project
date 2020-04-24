package com.github.CookieDuck.kafkaproject.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ShuffleRequest {
    private Integer times;
    private List<Card> cards;
}
