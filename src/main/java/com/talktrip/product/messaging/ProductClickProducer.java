package com.talktrip.product.messaging;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductClickProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductClickProducer.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.product-click:product-click}")
    private String productClickTopic;

    public void publishProductClick(Long productId, Long memberId) {
        try {
            var event = ProductClickEventDTO.of(productId, memberId);
            kafkaTemplate.send(productClickTopic, String.valueOf(productId), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("product-click 발행 성공 productId={} memberId={}", productId, memberId);
                        } else {
                            log.warn("product-click 발행 실패 productId={} memberId={}", productId, memberId, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("product-click 발행 중 오류 productId={} memberId={}", productId, memberId, e);
        }
    }
}
