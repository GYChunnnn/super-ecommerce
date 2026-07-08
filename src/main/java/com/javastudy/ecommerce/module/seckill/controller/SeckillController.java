package com.javastudy.ecommerce.module.seckill.controller;

import com.javastudy.ecommerce.common.result.Result;
import com.javastudy.ecommerce.config.SecurityContextUtil;
import com.javastudy.ecommerce.module.seckill.model.dto.SeckillResult;
import com.javastudy.ecommerce.module.seckill.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "秒杀", description = "秒杀（需 JWT 认证）")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "秒杀下单")
    @PostMapping
    public Result<SeckillResult> seckill(@RequestParam Long productId,
                                         @RequestParam(defaultValue = "1") Integer quantity) {
        return Result.success(seckillService.seckill(SecurityContextUtil.getCurrentUserId(), productId, quantity));
    }
}
