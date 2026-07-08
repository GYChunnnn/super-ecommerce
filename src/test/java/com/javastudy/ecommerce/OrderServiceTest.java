package com.javastudy.ecommerce;

import com.javastudy.ecommerce.module.cart.mapper.CartItemMapper;
import com.javastudy.ecommerce.module.cart.model.entity.CartItem;
import com.javastudy.ecommerce.module.order.mapper.OrderItemMapper;
import com.javastudy.ecommerce.module.order.mapper.OrderMapper;
import com.javastudy.ecommerce.module.order.model.dto.OrderCreateRequest;
import com.javastudy.ecommerce.module.order.model.entity.Order;
import com.javastudy.ecommerce.module.order.model.vo.OrderVO;
import com.javastudy.ecommerce.module.order.service.impl.OrderServiceImpl;
import com.javastudy.ecommerce.module.payment.mapper.PaymentMapper;
import com.javastudy.ecommerce.module.product.mapper.ProductMapper;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import com.javastudy.ecommerce.module.product.service.ProductCacheService;
import com.javastudy.ecommerce.module.order.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * 订单服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private ProductCacheService productCacheService;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Phone");
        product.setPrice(new BigDecimal("2999.00"));
        product.setStock(100);
        product.setSales(0);
        product.setStatus(1);
        product.setMainImage("");

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setUserId(1L);
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        cartItem.setSelected(1);
    }

    @Test
    void testCreateOrderSuccess() {
        when(cartItemMapper.selectList(any())).thenReturn(List.of(cartItem));
        when(productMapper.selectBatchIds(anyList())).thenReturn(List.of(product));
        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        });
        when(orderItemMapper.insert(any())).thenReturn(1);
        when(productMapper.updateById(any())).thenReturn(1);
        when(cartItemMapper.deleteById(anyLong())).thenReturn(1);
        doNothing().when(messageService).sendOrderDelayCancelMessage(anyLong(), anyLong());
        doNothing().when(productCacheService).updateCache(any());

        OrderCreateRequest req = new OrderCreateRequest();
        req.setReceiverName("Zhang San");
        req.setReceiverPhone("13800138000");
        req.setReceiverAddress("Beijing");

        OrderVO result = orderService.createOrder(1L, req);

        assertNotNull(result);
        assertEquals("Zhang San", result.getReceiverName());
        assertEquals("待支付", result.getStatusDesc());
        assertEquals(new BigDecimal("5998.00"), result.getTotalAmount()); // 2999 * 2
    }

    @Test
    void testCancelOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(0); // 待支付
        order.setTotalAmount(new BigDecimal("5998.00"));

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(List.of());
        // product.selectId/updateById 不一定会被调用（items为空时跳过恢复库存）

        orderService.cancelOrder(1L, 1L);

        // 验证订单状态已改为取消
        assertEquals(4, order.getStatus());
    }
}
