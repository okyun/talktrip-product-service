package com.talktrip.product.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ProductClickEventDTO(
        @JsonProperty("productId") Long productId,
        @JsonProperty("memberId") Long memberId,
        @JsonProperty("clickedAt") Instant clickedAt
) {
    public static ProductClickEventDTO of(Long productId, Long memberId) {
        return new ProductClickEventDTO(productId, memberId, Instant.now());
    }
}
