package com.zluolan.zojbackendserviceclient.service;

import com.zluolan.zojbackendmodel.entity.User;
import com.zluolan.zojbackendmodel.vo.LoginUserVO;
import com.zluolan.zojbackendmodel.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "zoj-backend-user-service", path = "/api/user/inner/user")
public interface UserFeignClient {

    /**
     * 根据id获取用户
     *
     * @param userId
     * @return
     */
    @GetMapping("/get/id")
    User getById(@RequestParam("userId") long userId);

    /**
     * 根据id列表获取用户列表
     *
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam("idList") Collection<Long> idList);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    @PostMapping("/get/vo")
    UserVO getUserVO(@RequestBody User user);

    /**
     * 获取当前登录用户
     *
     * @param userId
     * @param userRole
     * @param userAccount
     * @return
     */
    @GetMapping("/get/login")
    LoginUserVO getLoginUser(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @RequestHeader(value = "X-User-Role", required = false) String userRole,
                             @RequestHeader(value = "X-User-Account", required = false) String userAccount);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    @PostMapping("/is/admin")
    boolean isAdmin(@RequestBody User user);
}