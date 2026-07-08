package com.javastudy.ecommerce;

import com.javastudy.ecommerce.module.cart.mapper.CartItemMapper;
import com.javastudy.ecommerce.module.cart.model.dto.CartItemAddRequest;
import com.javastudy.ecommerce.module.cart.model.dto.CartItemUpdateRequest;
import com.javastudy.ecommerce.module.cart.model.entity.CartItem;
import com.javastudy.ecommerce.module.cart.model.vo.CartItemVO;
import com.javastudy.ecommerce.module.cart.service.impl.CartServiceImpl;
import com.javastudy.ecommerce.module.product.mapper.ProductMapper;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 购物车服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Phone");
        product.setPrice(new BigDecimal("2999.00"));
        product.setStock(100);
        product.setStatus(1);
        product.setMainImage("");
    }

    @Test
    void testAddNewItem() {
        when(productMapper.selectById(1L)).thenReturn(product);
        when(cartItemMapper.selectOne(any())).thenReturn(null);
        when(cartItemMapper.insert(any(CartItem.class))).thenAnswer(inv -> {
            CartItem c = inv.getArgument(0);
            c.setId(1L);
            return 1;
        });

        CartItemAddRequest req = new CartItemAddRequest();
        req.setProductId(1L);
        req.setQuantity(2);

        CartItemVO result = cartService.add(1L, req);

        assertNotNull(result);
        assertEquals("Test Phone", result.getProductName());
        assertEquals(2, result.getQuantity());
        assertEquals(1, result.getSelected());
        assertEquals(new BigDecimal("2999.00"), result.getPrice());
    }

    @Test
    void testAddExistingItem() {
        CartItem existing = new CartItem();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setProductId(1L);
        existing.setQuantity(3);
        existing.setSelected(1);

        when(productMapper.selectById(1L)).thenReturn(product);
        when(cartItemMapper.selectOne(any())).thenReturn(existing);

        CartItemAddRequest req = new CartItemAddRequest();
        req.setProductId(1L);
        req.setQuantity(2);

        CartItemVO result = cartService.add(1L, req);

        assertNotNull(result);
        assertEquals(5, result.getQuantity()); // 3 + 2 = 5
    }

    @Test
    void testListEmptyCart() {
        when(cartItemMapper.selectList(any())).thenReturn(List.of());

        List<CartItemVO> result = cartService.list(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateQuantity() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUserId(1L);
        item.setProductId(1L);
        item.setQuantity(2);
        item.setSelected(1);

        when(cartItemMapper.selectById(1L)).thenReturn(item);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(cartItemMapper.updateById(any())).thenReturn(1);

        CartItemUpdateRequest req = new CartItemUpdateRequest();
        req.setQuantity(5);

        CartItemVO result = cartService.update(1L, 1L, req);

        assertEquals(5, result.getQuantity());
    }
}
