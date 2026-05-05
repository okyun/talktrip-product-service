package com.talktrip.product.dto.response;

/**
 * 모놀리스 {@code ReviewResponse} 와 동일한 JSON 필드(상세 응답 직렬화 호환).
 * 상품 MSA 에는 리뷰 테이블이 없어 목록은 비어 있습니다.
 */
public record ReviewResponse(
        Long reviewId,
        String nickName,
        String productName,
        String thumbnailImageUrl,
        String comment,
        float reviewStar,
        String updatedAt
) {
}
