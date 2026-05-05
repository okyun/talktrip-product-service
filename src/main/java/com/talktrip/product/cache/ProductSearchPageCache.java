package com.talktrip.product.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.talktrip.product.dto.response.ProductSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record ProductSearchPageCache(
        List<ProductSummaryResponse> content,
        long totalElements
) {
    public Page<ProductSummaryResponse> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
