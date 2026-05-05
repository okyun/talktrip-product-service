package com.talktrip.product.controller;

import com.talktrip.product.dto.response.ProductDetailResponse;
import com.talktrip.product.dto.response.ProductSummaryResponse;
import com.talktrip.product.messaging.ProductClickProducer;
import com.talktrip.product.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.talktrip.product.util.SortUtil.buildSort;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductCatalogService productCatalogService;
    private final ProductClickProducer productClickProducer;

    @GetMapping
    public ResponseEntity<Page<ProductSummaryResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "전체") String countryName,
            @RequestParam(required = false) String countryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") List<String> sort,
            @RequestParam(required = false) Long memberId
    ) {
        log.info("GET /api/products page={} size={} keyword={} countryName={} countryId={} memberId={}",
                page, size, keyword, countryName, countryId, memberId);
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        return ResponseEntity.ok(productCatalogService.searchProducts(keyword, countryName, countryId, memberId, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @PathVariable Long productId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "updatedAt,desc") List<String> sort
    ) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        try {
            productClickProducer.publishProductClick(productId, memberId);
        } catch (Exception e) {
            log.warn("상품 클릭 이벤트 발행 실패(상세 조회는 계속): productId={}, memberId={}", productId, memberId, e);
        }
        return ResponseEntity.ok(productCatalogService.getProductDetail(productId, memberId, pageable));
    }

    @GetMapping("/aisearch")
    public ResponseEntity<List<ProductSummaryResponse>> aiSearchProducts(
            @RequestParam String question,
            @RequestParam(required = false) Long memberId
    ) {
        return ResponseEntity.ok(productCatalogService.aiSearchProducts(question, memberId));
    }
}
