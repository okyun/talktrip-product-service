package com.talktrip.product.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talktrip.product.entity.Product;
import com.talktrip.product.entity.ProductImage;
import com.talktrip.product.entity.ProductOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailResponse(
        Long productId,
        String productName,
        String shortDescription,
        int price,
        int discountPrice,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime regDate,
        String thumbnailImageUrl,
        String countryName,
        List<String> hashtags,
        List<String> images,
        List<ProductOptionResponse> stocks,
        float averageReviewStar,
        List<ReviewResponse> reviews,
        boolean isLiked,
        String sellerName,
        String email,
        String phoneNum
) {
    public static ProductDetailResponse fromProduct(Product product, boolean isLiked) {
        List<ProductOption> futureOptions = product.getProductOptions().stream()
                .filter(option -> option.getStartDate() == null || !option.getStartDate().isBefore(LocalDate.now()))
                .toList();

        ProductOption minPriceStock = product.getMinPriceOption();
        int price = minPriceStock != null ? minPriceStock.getPrice() : 0;
        int discountPrice = minPriceStock != null ? minPriceStock.getDiscountPrice() : 0;

        String countryLabel = product.getCountryId() != null ? product.getCountryId() : "";

        return new ProductDetailResponse(
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                price,
                discountPrice,
                product.getUpdatedAt(),
                product.getThumbnailImageUrl(),
                countryLabel,
                List.of(),
                product.getImages().stream().map(ProductImage::getImageUrl).toList(),
                futureOptions.stream().map(ProductOptionResponse::from).toList(),
                0f,
                List.of(),
                isLiked,
                "seller:" + product.getSellerId(),
                "",
                ""
        );
    }
}
