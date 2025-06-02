package org.retrade.main.repository;

import org.retrade.main.model.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {
}
