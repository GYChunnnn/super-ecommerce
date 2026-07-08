package com.javastudy.ecommerce.module.cart.service;

import com.javastudy.ecommerce.module.cart.model.dto.CartItemAddRequest;
import com.javastudy.ecommerce.module.cart.model.dto.CartItemUpdateRequest;
import com.javastudy.ecommerce.module.cart.model.vo.CartItemVO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /** 查询用户购物车列表 */
    List<CartItemVO> list(Long userId);

    /** 添加商品到购物车 */
    CartItemVO add(Long userId, CartItemAddRequest request);

    /** 更新购物车项（数量/选中状态） */
    CartItemVO update(Long userId, Long cartItemId, CartItemUpdateRequest request);

    /** 删除购物车项 */
    void delete(Long userId, Long cartItemId);

    /** 全选/取消全选 */
    void selectAll(Long userId, Integer selected);

    /** 清空购物车 */
    void clear(Long userId);
}
