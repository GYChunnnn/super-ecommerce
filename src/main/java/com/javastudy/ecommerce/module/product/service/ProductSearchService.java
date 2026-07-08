package com.javastudy.ecommerce.module.product.service;

import com.javastudy.ecommerce.module.product.model.dto.ProductSearchRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductSearchResult;
import com.javastudy.ecommerce.module.product.model.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * ES 搜索服务
 */
public interface ProductSearchService {

    /** 关键词搜索 + 分类/价格筛选 + 排序 */
    Map<String, Object> search(ProductSearchRequest request);

    /** 全量同步：MySQL → ES */
    void fullSync();

    /** 增量同步：单个商品变更后刷新 ES */
    void syncProduct(Product product);

    /** 从 ES 删除商品 */
    void deleteProduct(Long productId);
}
