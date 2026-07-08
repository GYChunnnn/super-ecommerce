package com.javastudy.ecommerce.module.product.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.javastudy.ecommerce.module.product.mapper.ProductMapper;
import com.javastudy.ecommerce.module.product.model.dto.ProductSearchRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductSearchResult;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import com.javastudy.ecommerce.module.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ES 搜索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private static final String INDEX_NAME = "products";

    private final ElasticsearchClient esClient;
    private final ProductMapper productMapper;

    @Override
    public Map<String, Object> search(ProductSearchRequest req) {
        ensureIndex();

        try {
            var searchBuilder = new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                    .index(INDEX_NAME);

            // ========== 查询条件 ==========
            var boolBuilder = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();
            boolBuilder.filter(f -> f.term(t -> t.field("status").value(1))); // 只查上架

            if (req.getCategoryId() != null) {
                boolBuilder.filter(f -> f.term(t -> t.field("categoryId").value(req.getCategoryId())));
            }

            if (StringUtils.hasText(req.getKeyword())) {
                boolBuilder.must(m -> m.multiMatch(mm -> mm
                        .fields("name^3", "description") // name 权重 3 倍
                        .query(req.getKeyword())));
            }

            if (req.getPriceMin() != null) {
                boolBuilder.filter(f -> f.range(r -> r.field("price")
                        .gte(JsonData.of(req.getPriceMin()))));
            }
            if (req.getPriceMax() != null) {
                boolBuilder.filter(f -> f.range(r -> r.field("price")
                        .lte(JsonData.of(req.getPriceMax()))));
            }
            searchBuilder.query(q -> q.bool(boolBuilder.build()));

            // ========== 排序 ==========
            if (StringUtils.hasText(req.getSortBy())) {
                SortOrder order = "DESC".equalsIgnoreCase(req.getSortDir())
                        ? SortOrder.Desc : SortOrder.Asc;
                String field = switch (req.getSortBy()) {
                    case "sales" -> "sales";
                    case "price" -> "price";
                    default -> "_score";
                };
                searchBuilder.sort(s -> s.field(f -> f.field(field).order(order)));
            } else {
                searchBuilder.sort(s -> s.field(f -> f.field("_score").order(SortOrder.Desc)));
                searchBuilder.sort(s -> s.field(f -> f.field("sales").order(SortOrder.Desc)));
            }

            // ========== 分页 ==========
            int from = (req.getPage() - 1) * req.getSize();
            searchBuilder.from(from).size(req.getSize());

            // ========== 高亮 ==========
            Map<String, HighlightField> fields = new HashMap<>();
            fields.put("name", HighlightField.of(h -> h
                    .preTags("<em style='color:red'>")
                    .postTags("</em>")
                    .fragmentSize(100)
                    .numberOfFragments(1)));
            fields.put("description", HighlightField.of(h -> h
                    .preTags("<em style='color:red'>")
                    .postTags("</em>")
                    .fragmentSize(150)
                    .numberOfFragments(1)));
            searchBuilder.highlight(Highlight.of(h -> h.fields(fields)));

            SearchResponse<Product> response = esClient.search(
                    searchBuilder.build(), Product.class);

            // ========== 组装结果 ==========
            List<ProductSearchResult> results = new ArrayList<>();
            for (Hit<Product> hit : response.hits().hits()) {
                ProductSearchResult r = new ProductSearchResult();
                Product source = hit.source();
                if (source != null) {
                    r.setId(source.getId() != null ? source.getId().toString() : null);
                    r.setName(source.getName());
                    r.setCategoryId(source.getCategoryId());
                    r.setPrice(source.getPrice());
                    r.setStock(source.getStock());
                    r.setSales(source.getSales());
                    r.setMainImage(source.getMainImage());
                }
                r.setScore(hit.score());

                // 处理高亮
                if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
                    Map<String, List<String>> hlMap = hit.highlight();
                    if (hlMap != null && hlMap.containsKey("name") && !hlMap.get("name").isEmpty()) {
                        r.setNameHighlight(hlMap.get("name").get(0));
                    }
                    if (hlMap != null && hlMap.containsKey("description") && !hlMap.get("description").isEmpty()) {
                        r.setDescHighlight(hlMap.get("description").get(0));
                    }
                }
                results.add(r);
            }

            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("records", results);
            resultMap.put("total", response.hits().total() != null ? response.hits().total().value() : 0);
            resultMap.put("page", req.getPage());
            resultMap.put("size", req.getSize());
            resultMap.put("keyword", req.getKeyword());
            return resultMap;

        } catch (Exception e) {
            log.error("ES 搜索异常", e);
            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("records", Collections.emptyList());
            errorMap.put("total", 0);
            errorMap.put("page", req.getPage());
            errorMap.put("size", req.getSize());
            return errorMap;
        }
    }

    @Override
    public void fullSync() {
        ensureIndex();
        List<Product> products = productMapper.selectList(null);
        if (products.isEmpty()) return;

        List<BulkOperation> operations = products.stream()
                .filter(p -> p.getStatus() == 1) // 只同步上架商品
                .map(p -> {
                    Product source = new Product();
                    source.setId(p.getId());
                    source.setCategoryId(p.getCategoryId());
                    source.setName(p.getName());
                    source.setDescription(p.getDescription());
                    source.setPrice(p.getPrice());
                    source.setStock(p.getStock());
                    source.setSales(p.getSales());
                    source.setMainImage(p.getMainImage());
                    source.setStatus(p.getStatus());

                    IndexOperation<Product> indexOp = IndexOperation.of(i -> i
                            .index(INDEX_NAME)
                            .id(p.getId().toString())
                            .document(source));
                    return BulkOperation.of(b -> b.index(indexOp));
                })
                .collect(Collectors.toList());

        try {
            esClient.bulk(b -> b.operations(operations));
            log.info("ES 全量同步完成: {} 条商品", operations.size());
        } catch (Exception e) {
            log.error("ES 全量同步失败", e);
        }
    }

    @Override
    public void syncProduct(Product product) {
        ensureIndex();
        try {
            Product doc = new Product();
            doc.setId(product.getId());
            doc.setCategoryId(product.getCategoryId());
            doc.setName(product.getName());
            doc.setDescription(product.getDescription());
            doc.setPrice(product.getPrice());
            doc.setStock(product.getStock());
            doc.setSales(product.getSales());
            doc.setMainImage(product.getMainImage());
            doc.setStatus(product.getStatus());

            esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(product.getId().toString())
                    .document(doc));
            log.info("ES 增量同步: 商品[id={}]", product.getId());
        } catch (Exception e) {
            log.error("ES 增量同步失败: 商品[id={}]", product.getId(), e);
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        try {
            esClient.delete(d -> d.index(INDEX_NAME).id(productId.toString()));
            log.info("ES 删除商品: id={}", productId);
        } catch (Exception e) {
            log.warn("ES 删除失败(可能不存在): id={}", productId);
        }
    }

    // ==================== 私有方法 ====================

    private void ensureIndex() {
        try {
            boolean exists = esClient.indices().exists(
                    ExistsRequest.of(e -> e.index(INDEX_NAME))).value();
            if (!exists) {
                esClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(INDEX_NAME)
                        .mappings(m -> m
                                .properties("name", p -> p.text(t -> t.analyzer("ik_max_word").searchAnalyzer("ik_smart")))
                                .properties("description", p -> p.text(t -> t.analyzer("ik_max_word")))
                                .properties("categoryId", p -> p.long_(l -> l))
                                .properties("price", p -> p.scaledFloat(s -> s.scalingFactor(100.0)))
                                .properties("stock", p -> p.integer(i -> i))
                                .properties("sales", p -> p.integer(i -> i))
                                .properties("mainImage", p -> p.keyword(k -> k))
                                .properties("status", p -> p.integer(i -> i))
                        )));
                log.info("ES 索引创建成功: {}", INDEX_NAME);
            }
        } catch (Exception e) {
            log.warn("ES 索引检查失败: {}", e.getMessage());
        }
    }
}
