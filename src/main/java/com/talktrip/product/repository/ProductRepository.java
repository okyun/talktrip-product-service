package com.talktrip.product.repository;

import com.talktrip.product.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Hibernate는 동시에 두 개의 List(@OneToMany "bag")를 fetch join/EntityGraph로 당기면
    // MultipleBagFetchException을 던질 수 있어, 상세는 옵션만 먼저 가져오고 이미지는 별도 쿼리로 조회합니다.
    @EntityGraph(attributePaths = {"productOptions"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findDetailById(@Param("id") Long id);

    @Query("select pi.imageUrl from ProductImage pi where pi.product.id = :id order by pi.sortOrder asc")
    List<String> findImageUrlsByProductId(@Param("id") Long id);
}
