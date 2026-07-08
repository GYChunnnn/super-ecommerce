package com.javastudy.ecommerce.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javastudy.ecommerce.common.exception.BusinessException;
import com.javastudy.ecommerce.module.product.mapper.CategoryMapper;
import com.javastudy.ecommerce.module.product.mapper.ProductMapper;
import com.javastudy.ecommerce.module.product.model.dto.CategoryRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductCreateRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductQueryRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductUpdateRequest;
import com.javastudy.ecommerce.module.product.model.entity.Category;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import com.javastudy.ecommerce.module.product.service.ProductCacheService;
import com.javastudy.ecommerce.module.product.service.ProductSearchService;
import com.javastudy.ecommerce.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    private final ProductSearchService productSearchService;

    // ==================== 分类 ====================

    @Override
    public List<Category> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
        );
    }

    @Override
    @Transactional
    public Category createCategory(CategoryRequest request) {
        // 同名检查
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getName, request.getName())
        );
        BusinessException.throwIf(count > 0, "分类名称已存在");

        Category category = new Category();
        BeanUtils.copyProperties(request, category);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        BusinessException.throwIfNull(category, "分类不存在");

        // 同名检查（排除自己）
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getName, request.getName())
                        .ne(Category::getId, id)
        );
        BusinessException.throwIf(count > 0, "分类名称已存在");

        BeanUtils.copyProperties(request, category);
        categoryMapper.updateById(category);
        return category;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        BusinessException.throwIfNull(category, "分类不存在");

        // 检查是否有关联商品
        Long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, id)
        );
        BusinessException.throwIf(count > 0, "该分类下还有商品，无法删除");

        categoryMapper.deleteById(id);
    }

    // ==================== 商品 ====================

    @Override
    @Transactional
    public Product createProduct(ProductCreateRequest request) {
        // 检查分类存在
        Category category = categoryMapper.selectById(request.getCategoryId());
        BusinessException.throwIfNull(category, "分类不存在");

        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setSales(0);
        productMapper.insert(product);
        // 写入缓存
        productCacheService.updateCache(product);
        // 同步 ES
        productSearchService.syncProduct(product);
        return product;
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productMapper.selectById(id);
        BusinessException.throwIfNull(product, "商品不存在");

        // 只更新非 null 字段
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            BusinessException.throwIfNull(category, "分类不存在");
            product.setCategoryId(request.getCategoryId());
        }
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getDetail() != null) {
            product.setDetail(request.getDetail());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getMainImage() != null) {
            product.setMainImage(request.getMainImage());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        productMapper.updateById(product);
        // 刷新缓存
        productCacheService.updateCache(product);
        // 同步 ES
        productSearchService.syncProduct(product);
        return product;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        BusinessException.throwIfNull(product, "商品不存在");
        productMapper.deleteById(id);
        // 删除缓存
        productCacheService.evictCache(id);
        // 从 ES 删除
        productSearchService.deleteProduct(id);
    }

    @Override
    public Product getProductById(Long id) {
        // 优先从缓存读取
        return productCacheService.getById(id);
    }

    @Override
    public Page<Product> queryProducts(ProductQueryRequest req) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查上架商品（如果未指定分类，默认展示上架商品）
        wrapper.eq(Product::getStatus, 1);

        // 分类筛选
        if (req.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, req.getCategoryId());
        }

        // 关键词模糊搜索（名称）
        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.like(Product::getName, req.getKeyword());
        }

        // 价格区间
        if (req.getPriceMin() != null) {
            wrapper.ge(Product::getPrice, req.getPriceMin());
        }
        if (req.getPriceMax() != null) {
            wrapper.le(Product::getPrice, req.getPriceMax());
        }

        // 排序
        if (StringUtils.hasText(req.getSortBy())) {
            boolean asc = !"DESC".equalsIgnoreCase(req.getSortDir());
            switch (req.getSortBy()) {
                case "price":
                    wrapper.orderBy(true, asc, Product::getPrice);
                    break;
                case "sales":
                    wrapper.orderBy(true, asc, Product::getSales);
                    break;
                case "create_time":
                    wrapper.orderBy(true, asc, Product::getCreateTime);
                    break;
                default:
                    wrapper.orderByDesc(Product::getCreateTime);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(Product::getCreateTime);
        }

        Page<Product> page = new Page<>(req.getPage(), req.getSize());
        return productMapper.selectPage(page, wrapper);
    }
}
