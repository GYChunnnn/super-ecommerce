package com.javastudy.ecommerce.module.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.javastudy.ecommerce.common.exception.BusinessException;
import com.javastudy.ecommerce.module.cart.mapper.CartItemMapper;
import com.javastudy.ecommerce.module.cart.model.dto.CartItemAddRequest;
import com.javastudy.ecommerce.module.cart.model.dto.CartItemUpdateRequest;
import com.javastudy.ecommerce.module.cart.model.entity.CartItem;
import com.javastudy.ecommerce.module.cart.model.vo.CartItemVO;
import com.javastudy.ecommerce.module.cart.service.CartService;
import com.javastudy.ecommerce.module.product.mapper.ProductMapper;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CartItemVO> list(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getCreateTime)
        );

        if (items.isEmpty()) {
            return List.of();
        }

        // 批量查询关联商品
        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 组装 VO
        List<CartItemVO> result = new ArrayList<>();
        for (CartItem item : items) {
            Product product = productMap.get(item.getProductId());
            if (product == null) continue; // 商品已删除则跳过

            CartItemVO vo = new CartItemVO();
            vo.setId(item.getId());
            vo.setProductId(product.getId());
            vo.setProductName(product.getName());
            vo.setMainImage(product.getMainImage());
            vo.setPrice(product.getPrice());
            vo.setStock(product.getStock());
            vo.setQuantity(item.getQuantity());
            vo.setSelected(item.getSelected());
            vo.setCreateTime(item.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public CartItemVO add(Long userId, CartItemAddRequest request) {
        // 检查商品是否存在且上架
        Product product = productMapper.selectById(request.getProductId());
        BusinessException.throwIfNull(product, "商品不存在");
        BusinessException.throwIf(product.getStatus() == 0, "商品已下架");
        BusinessException.throwIf(product.getStock() < request.getQuantity(), "库存不足");

        // 检查是否已在购物车中
        CartItem exist = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getProductId, request.getProductId())
        );

        if (exist != null) {
            // 已存在则累加数量
            exist.setQuantity(exist.getQuantity() + request.getQuantity());
            cartItemMapper.updateById(exist);

            CartItemVO vo = buildVO(exist, product);
            return vo;
        }

        // 新增
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setSelected(1);
        cartItemMapper.insert(item);

        return buildVO(item, product);
    }

    @Override
    @Transactional
    public CartItemVO update(Long userId, Long cartItemId, CartItemUpdateRequest request) {
        CartItem item = getOwnCartItem(userId, cartItemId);

        if (request.getQuantity() != null) {
            // 检查库存
            Product product = productMapper.selectById(item.getProductId());
            BusinessException.throwIf(product.getStock() < request.getQuantity(), "库存不足");
            item.setQuantity(request.getQuantity());
        }
        if (request.getSelected() != null) {
            item.setSelected(request.getSelected());
        }

        cartItemMapper.updateById(item);

        Product product = productMapper.selectById(item.getProductId());
        return buildVO(item, product);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long cartItemId) {
        getOwnCartItem(userId, cartItemId);
        cartItemMapper.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void selectAll(Long userId, Integer selected) {
        cartItemMapper.update(null,
                new LambdaUpdateWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .set(CartItem::getSelected, selected)
        );
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
        );
    }

    // ==================== 私有方法 ====================

    private CartItem getOwnCartItem(Long userId, Long cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        BusinessException.throwIfNull(item, "购物车项不存在");
        BusinessException.throwIf(!item.getUserId().equals(userId), "无权操作该购物车项");
        return item;
    }

    private CartItemVO buildVO(CartItem item, Product product) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setProductId(product.getId());
        vo.setProductName(product.getName());
        vo.setMainImage(product.getMainImage());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setQuantity(item.getQuantity());
        vo.setSelected(item.getSelected());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }
}
