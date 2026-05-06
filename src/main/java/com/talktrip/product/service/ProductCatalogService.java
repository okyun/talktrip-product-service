package com.talktrip.product.service;

import com.talktrip.product.cache.ProductSearchCacheService;
import com.talktrip.product.dto.response.ProductDetailResponse;
import com.talktrip.product.dto.response.ProductSummaryResponse;
import com.talktrip.product.entity.Product;
import com.talktrip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductCatalogService {

    private static final Set<String> ALLOWED_SORT = Set.of("updatedAt", "productName");

    private final ProductRepository productRepository;
    private final ProductSearchCacheService productSearchCacheService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fastapi.base-url:http://localhost:8000}")
    private String fastApiBaseUrl;

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchProducts(
            String keyword,
            String countryName,
            String countryId,
            Long memberId,
            Pageable pageable
    ) {
        Pageable p = sanitizePageable(pageable);
        return productSearchCacheService
                .getBaseProductSearchPageCache(keyword, countryName, countryId, p)
                .toPage(p);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId, Long memberId, Pageable ignored) {
        Product product = productRepository.findDetailById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        if (product.getTotalStock() <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
        List<String> imageUrls = productRepository.findImageUrlsByProductId(productId);
        boolean liked = false;
        return ProductDetailResponse.fromProduct(product, liked, imageUrls);
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> aiSearchProducts(String query, Long memberId) {
        try {
            String fastApiUrl = fastApiBaseUrl + "/query";
            Map<String, String> requestBody = Map.of("query", query);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(fastApiUrl, requestBody, Map.class);
            if (response == null || !response.containsKey("product_ids")) {
                return List.of();
            }
            Object productIdsObj = response.get("product_ids");
            List<String> productIdStrings;
            if (productIdsObj instanceof List<?>) {
                productIdStrings = ((List<?>) productIdsObj).stream().map(Object::toString).toList();
            } else {
                return List.of();
            }
            if (productIdStrings.isEmpty()) {
                return List.of();
            }
            List<Long> productIds = productIdStrings.stream().map(Long::parseLong).toList();
            List<Product> products = productRepository.findAllById(productIds);
            Map<Long, Integer> idOrder = new HashMap<>();
            for (int i = 0; i < productIds.size(); i++) {
                idOrder.put(productIds.get(i), i);
            }
            return products.stream()
                    .filter(p -> !p.isDeleted())
                    .sorted(Comparator.comparing(p -> idOrder.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                    .map(p -> ProductSummaryResponse.from(p, 0f, false))
                    .toList();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ai search failed", e);
        }
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return pageable;
        }
        for (Sort.Order o : pageable.getSort()) {
            if (!ALLOWED_SORT.contains(o.getProperty())) {
                return PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "updatedAt")
                );
            }
        }
        return pageable;
    }
}
