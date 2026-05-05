package com.talktrip.product.service;

import com.talktrip.product.entity.Product;
import com.talktrip.product.entity.ProductOption;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> hasOptionInStock() {
        return (root, query, cb) -> {
            Subquery<Long> sq = query.subquery(Long.class);
            Root<ProductOption> opt = sq.from(ProductOption.class);
            sq.select(cb.literal(1L));
            sq.where(cb.equal(opt.get("product"), root), cb.gt(opt.get("stock"), 0));
            return cb.exists(sq);
        };
    }

    public static Specification<Product> keywords(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            var parts = keyword.trim().split("\\s+");
            var combined = cb.conjunction();
            for (String raw : parts) {
                if (raw.isBlank()) {
                    continue;
                }
                String pattern = "%" + raw.toLowerCase(Locale.ROOT) + "%";
                combined = cb.and(combined, cb.or(
                        cb.like(cb.lower(root.get("productName")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            return combined;
        };
    }

    /**
     * productDB 에는 국가명 테이블이 없어 country_id 문자열 기준으로만 필터합니다.
     */
    public static Specification<Product> country(String countryName) {
        return (root, query, cb) -> {
            if (countryName == null || countryName.isBlank() || "전체".equals(countryName)) {
                return cb.conjunction();
            }
            String pattern = "%" + countryName.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("countryId")), pattern);
        };
    }

    /** ISO 등 저장된 {@code country_id} 와 대소문자 무시 일치 (예: FR, JP). */
    public static Specification<Product> countryIdEquals(String countryId) {
        return (root, query, cb) -> {
            if (countryId == null || countryId.isBlank()) {
                return cb.conjunction();
            }
            String id = countryId.trim().toLowerCase(Locale.ROOT);
            return cb.equal(cb.lower(root.get("countryId")), id);
        };
    }
}
