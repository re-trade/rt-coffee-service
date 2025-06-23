package org.retrade.main.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.main.model.document.ProductDocument;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.repository.ProductElasticsearchRepository;
import org.retrade.main.repository.ProductRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            ExistsRequest request = ExistsRequest.of(e -> e.index(PRODUCT_INDEX));
            return elasticsearchClient.indices().exists(request).value();
        } catch (Exception e) {
            log.error("Error checking if index exists", e);
            return false;
        }
    }

    private void createIndex() {
        try {
            CreateIndexRequest request = CreateIndexRequest.of(c -> c
                    .index(PRODUCT_INDEX)
                    .mappings(m -> m
                            .properties("id", p -> p.keyword(k -> k))
                            .properties("name", p -> p.text(t -> t.analyzer("standard")))
                            .properties("sellerId", p -> p.keyword(k -> k))
                            .properties("shortDescription", p -> p.text(t -> t.analyzer("standard")))
                            .properties("description", p -> p.text(t -> t.analyzer("standard")))
                            .properties("brand", p -> p.keyword(k -> k))
                            .properties("model", p -> p.keyword(k -> k))
                            .properties("currentPrice", p -> p.double_(d -> d))
                            .properties("discount", p -> p.double_(d -> d))
                            .properties("verified", p -> p.boolean_(b -> b))
                            .properties("createdAt", p -> p.date(d -> d))
                            .properties("updatedAt", p -> p.date(d -> d))
                            .properties("categories", p -> p.nested(n -> n
                                    .properties("id", cp -> cp.keyword(k -> k))
<<<<<<< HEAD
                                    .properties("name", cp -> cp.keyword(k -> k))
                                    .properties("type", cp -> cp.keyword(k -> k))
=======
                                    .properties("name", cp -> cp.text(t -> t
                                            .analyzer("standard")
                                            .fields("keyword", f -> f.keyword(k -> k.ignoreAbove(256)))
                                    ))
>>>>>>> 644b29bb29325c5f33ef47e964a21213396462ca
                            ))
                    )
                    .settings(s -> s
                            .numberOfShards("1")
                            .numberOfReplicas("0")
                    )
            );
            elasticsearchClient.indices().create(request);
            log.info("Successfully created product index");
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
                            ProductDocument newDoc = convertToProductDocument(product);
                            productElasticsearchRepository.save(newDoc);
                            totalUpdated++;
                        } else if (shouldUpdateDocument(product, existingDoc)) {
                            ProductDocument updatedDoc = convertToProductDocument(product);
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
                .map(this::convertToProductDocument)
                .collect(Collectors.toList());
    }

    private ProductDocument convertToProductDocument(ProductEntity product) {
        return ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .sellerId(product.getSeller().getId())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .brand(product.getBrand())
                .discount(product.getDiscount())
                .model(product.getModel())
                .currentPrice(product.getCurrentPrice())
                .categories(product.getCategories().stream()
                        .map(item -> ProductDocument.CategoryInfo.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .type(item.getType())
                                .build())
                        .collect(Collectors.toSet()))
                .verified(product.getVerified())
                .createdAt(product.getCreatedDate() != null ? product.getCreatedDate() : null)
                .updatedAt(product.getUpdatedDate() != null ? product.getUpdatedDate() : null)
                .build();
    }
}