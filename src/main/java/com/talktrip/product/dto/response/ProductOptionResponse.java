package com.talktrip.product.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talktrip.product.entity.ProductOption;

import java.time.LocalDate;

public record ProductOptionResponse(
        Long productOptionId,
        String optionName,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,
        int stock,
        int price,
        int discountPrice
) {
    public static ProductOptionResponse from(ProductOption stock) {
        return new ProductOptionResponse(
                stock.getId(),
                stock.getOptionName(),
                stock.getStartDate(),
                stock.getStock(),
                stock.getPrice(),
                stock.getDiscountPrice()
        );
    }
}
