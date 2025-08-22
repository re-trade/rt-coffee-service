package org.retrade.main.repository.elasticsearch;

import org.retrade.main.model.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    Stream<ProductDocument> findBySellerId(String sellerId);

    Stream<ProductDocument> findByBrandId(String brandId);

    Stream<ProductDocument> findByCategories_Id(String id);
}
