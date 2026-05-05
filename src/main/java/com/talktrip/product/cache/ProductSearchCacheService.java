package com.talktrip.product.cache;

import com.talktrip.product.dto.response.ProductSummaryResponse;
import com.talktrip.product.entity.Product;
import com.talktrip.product.repository.ProductRepository;
import com.talktrip.product.service.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductSearchCacheService {

    private final ProductRepository productRepository;

    @Cacheable(
            cacheNames = "product",
            key = "'productSearch:v5:msa:'"
                    + " + (#keyword == null ? '' : #keyword.trim())"
                    + " + ':' + (#countryName == null ? '' : #countryName)"
                    + " + ':' + (#countryId == null ? '' : #countryId)"
                    + " + ':' + #pageable.pageNumber"
                    + " + ':' + #pageable.pageSize"
                    + " + ':' + #pageable.sort.toString()",
            unless = "#result == null || #result.content() == null || #result.content().isEmpty()"
    )
    @Transactional(readOnly = true)
    public ProductSearchPageCache getBaseProductSearchPageCache(
            String keyword,
            String countryName,
            String countryId,
            Pageable pageable
    ) {
        Specification<Product> countrySpec = (countryId != null && !countryId.isBlank())
                ? ProductSpecifications.countryIdEquals(countryId)
                : ProductSpecifications.country(countryName);
        Specification<Product> spec = ProductSpecifications.hasOptionInStock()
                .and(countrySpec)
                .and(ProductSpecifications.keywords(keyword));

        Page<Product> page = productRepository.findAll(spec, pageable);
        if (page.isEmpty()) {
            return new ProductSearchPageCache(java.util.List.of(), page.getTotalElements());
        }

        var content = page.getContent().stream()
                .map(p -> ProductSummaryResponse.from(p, 0f, false))
                .toList();

        return new ProductSearchPageCache(content, page.getTotalElements());
    }
}
