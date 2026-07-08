package com.javastudy.ecommerce.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javastudy.ecommerce.module.user.model.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除（绕过 @TableLogic）
     */
    @Delete("DELETE FROM user WHERE username = #{username}")
    int deleteAdminForce(@Param("username") String username);
}
