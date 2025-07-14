package org.retrade.main.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.main.model.document.ProductDocument;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.repository.elasticsearch.ProductElasticsearchRepository;
import org.retrade.main.repository.jpa.ProductRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchMigrationService {

    private final ElasticsearchClient elasticsearchClient;
    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductElasticsearchRepository productElasticsearchRepository;

    private static final String PRODUCT_INDEX = "products";
    private static final int BATCH_SIZE = 100;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void migrateDataOnStartup() {
        try {
            log.info("Starting Elasticsearch migration process...");
            if (isIndexExists()) {
                log.info("Product index exists. Checking for data synchronization...");
                syncExistingData();
            } else {
                log.info("Product index does not exist. Creating index and migrating all data...");
                createIndex();
                migrateAllData();
            }

            log.info("Elasticsearch migration completed successfully");
        } catch (Exception e) {
            log.error("Failed to migrate data to Elasticsearch", e);
        }
    }

    private boolean isIndexExists() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            return indexOps.exists();
        } catch (Exception e) {
            log.error("Error checking if index exists", e);
            return false;
        }
    }

    private void createIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            if (!indexOps.exists()) {
                indexOps.create();
                indexOps.putMapping(indexOps.createMapping(ProductDocument.class));
                log.info("Successfully created product index using ProductDocument mappings");
            }
        } catch (Exception e) {
            log.error("Failed to create product index", e);
            throw new RuntimeException("Failed to create product index", e);
        }
    }
    private void migrateAllData() {
        try {
            long totalProducts = productRepository.count();
            log.info("Starting migration of {} products to Elasticsearch", totalProducts);
            int pageNumber = 0;
            int totalMigrated = 0;
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<ProductEntity> productPage = productRepository.findAll(pageable);
                if (productPage.getContent().isEmpty()) {
                    break;
                }
                List<ProductDocument> productDocuments = convertToProductDocuments(productPage.getContent());
                productElasticsearchRepository.saveAll(productDocuments);
                totalMigrated += productPage.getContent().size();
                log.info("Migrated {}/{} products", totalMigrated, totalProducts);
                if (!productPage.hasNext()) {
                    break;
                }
                pageNumber++;
            }
            log.info("Successfully migrated {} products to Elasticsearch", totalMigrated);
        } catch (Exception e) {
            log.error("Failed to migrate all data", e);
            throw new RuntimeException("Failed to migrate all data", e);
        }
    }
    private void syncExistingData() {
        try {
            long dbCount = productRepository.count();
            long esCount = productElasticsearchRepository.count();
            log.info("Database has {} products, Elasticsearch has {} products", dbCount, esCount);
            if (dbCount > esCount) {
                log.info("Database has more products than Elasticsearch. Starting sync...");
                syncMissingProducts();
            } else if (dbCount == esCount) {
                log.info("Product counts match. Checking for updates...");
                syncUpdatedProducts();
            } else {
                log.warn("Elasticsearch has more products than database. This might indicate data inconsistency.");
            }

        } catch (Exception e) {
            log.error("Failed to sync existing data", e);
            throw new RuntimeException("Failed to sync existing data", e);
        }
    }
    private void syncMissingProducts() {
        try {
            Iterable<ProductDocument> allEsProducts = productElasticsearchRepository.findAll();
            List<String> esProductIds = StreamSupport.stream(allEsProducts.spliterator(), false)
                    .map(ProductDocument::getId)
                    .toList();
            int pageNumber = 0;
            int totalSynced = 0;
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<ProductEntity> productPage = productRepository.findAll(pageable);
                if (productPage.getContent().isEmpty()) {
                    break;
                }
                List<ProductEntity> missingProducts = productPage.getContent().stream()
                        .filter(product -> !esProductIds.contains(product.getId()))
                        .collect(Collectors.toList());
                if (!missingProducts.isEmpty()) {
                    List<ProductDocument> productDocuments = convertToProductDocuments(missingProducts);
                    productElasticsearchRepository.saveAll(productDocuments);
                    totalSynced += missingProducts.size();
                    log.info("Synced {} missing products", totalSynced);
                }
                if (!productPage.hasNext()) {
                    break;
                }
                pageNumber++;
            }
            log.info("Successfully synced {} missing products", totalSynced);
        } catch (Exception e) {
            log.error("Failed to sync missing products", e);
            throw new RuntimeException("Failed to sync missing products", e);
        }
    }

    private void syncUpdatedProducts() {
        try {
            int pageNumber = 0;
            int totalUpdated = 0;

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<ProductEntity> productPage = productRepository.findAll(pageable);

                if (productPage.getContent().isEmpty()) {
                    break;
                }

                for (ProductEntity product : productPage.getContent()) {
                    try {
                        ProductDocument existingDoc = productElasticsearchRepository.findById(product.getId()).orElse(null);
                        if (existingDoc == null) {
                            ProductDocument newDoc = ProductDocument.wrapEntityToDocument(product);
                            productElasticsearchRepository.save(newDoc);
                            totalUpdated++;
                        } else if (shouldUpdateDocument(product, existingDoc)) {
                            ProductDocument updatedDoc = ProductDocument.wrapEntityToDocument(product);
                            productElasticsearchRepository.save(updatedDoc);
                            totalUpdated++;
                        }
                    } catch (Exception e) {
                        log.warn("Failed to sync product with ID: {}", product.getId(), e);
                    }
                }

                if (!productPage.hasNext()) {
                    break;
                }
                pageNumber++;
            }
            log.info("Successfully updated {} products in Elasticsearch", totalUpdated);
        } catch (Exception e) {
            log.error("Failed to sync updated products", e);
            throw new RuntimeException("Failed to sync updated products", e);
        }
    }

    private boolean shouldUpdateDocument(ProductEntity product, ProductDocument document) {
        if (product.getUpdatedDate() != null && document.getUpdatedAt() != null) {
            LocalDateTime productUpdatedDateTime =
                    product.getUpdatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime documentUpdatedDateTime =
                    document.getUpdatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return productUpdatedDateTime.isAfter(documentUpdatedDateTime);
        }
        return !product.getName().equals(document.getName()) ||
                !product.getCurrentPrice().equals(document.getCurrentPrice()) ||
                !product.getVerified().equals(document.getVerified());
    }


    private List<ProductDocument> convertToProductDocuments(List<ProductEntity> products) {
        return products.stream()
                .map(ProductDocument::wrapEntityToDocument)
                .collect(Collectors.toList());
    }
}