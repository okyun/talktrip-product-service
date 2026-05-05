package com.talktrip.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE product SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "country_id")
    private String countryId;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(length = 1000, nullable = false)
    private String description;

    @Column(name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @Column(name = "thumbnail_image_hash")
    private String thumbnailImageHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> productOptions = new ArrayList<>();

    public ProductOption getMinPriceOption() {
        return productOptions.stream()
                .filter(option -> option.getStartDate() == null || !option.getStartDate().isBefore(LocalDate.now()))
                .min(Comparator.comparingInt(ProductOption::getDiscountPrice))
                .orElse(null);
    }

    public int getTotalStock() {
        return productOptions.stream().mapToInt(ProductOption::getStock).sum();
    }
}
