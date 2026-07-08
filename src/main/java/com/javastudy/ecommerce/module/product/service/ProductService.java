package com.javastudy.ecommerce.module.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javastudy.ecommerce.module.product.model.dto.CategoryRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductCreateRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductQueryRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductUpdateRequest;
import com.javastudy.ecommerce.module.product.model.entity.Category;
import com.javastudy.ecommerce.module.product.model.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    // ==================== 分类 ====================

    /** 查询所有分类 */
    List<Category> listCategories();

    /** 创建分类 */
    Category createCategory(CategoryRequest request);

    /** 更新分类 */
    Category updateCategory(Long id, CategoryRequest request);

    /** 删除分类 */
    void deleteCategory(Long id);

    // ==================== 商品 ====================

    /** 创建商品 */
    Product createProduct(ProductCreateRequest request);

    /** 更新商品 */
    Product updateProduct(Long id, ProductUpdateRequest request);

    /** 删除商品（逻辑删除） */
    void deleteProduct(Long id);

    /** 查询商品详情 */
    Product getProductById(Long id);

    /** 分页查询 + 模糊搜索 + 条件筛选 */
    Page<Product> queryProducts(ProductQueryRequest request);
}
