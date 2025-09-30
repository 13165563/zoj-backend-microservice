package com.zluolan.zojbackenduserservice.controller.inner;

import com.zluolan.zojbackendcommon.common.BaseResponse;
import com.zluolan.zojbackendcommon.common.ErrorCode;
import com.zluolan.zojbackendcommon.common.ResultUtils;
import com.zluolan.zojbackendcommon.exception.BusinessException;
import com.zluolan.zojbackendmodel.entity.User;
import com.zluolan.zojbackendmodel.vo.LoginUserVO;
import com.zluolan.zojbackendmodel.vo.UserVO;
import com.zluolan.zojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用户内部服务接口
 * 供其他微服务调用
 */
@RestController
@RequestMapping("/inner/user")
public class UserInnerController {

    @Resource
    private UserService userService;

    /**
     * 根据id获取用户
     *
     * @param userId
     * @return
     */
    @GetMapping("/get/id")
    public User getById(@RequestParam("userId") long userId) {
        return userService.getById(userId);
    }

    /**
     * 根据id列表获取用户列表
     *
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("idList") Collection<Long> idList) {
        return userService.listByIds(idList);
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    @PostMapping("/get/vo")
    public BaseResponse<UserVO> getUserVO(@RequestBody User user) {
        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param userId
     * @param userRole
     * @param userAccount
     * @return
     */
    @GetMapping("/get/login")
    public LoginUserVO getLoginUser(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @RequestHeader(value = "X-User-Role", required = false) String userRole,
                                    @RequestHeader(value = "X-User-Account", required = false) String userAccount) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 从数据库获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        return userService.getLoginUserVO(user);
    }

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    @PostMapping("/is/admin")
    public boolean isAdmin(@RequestBody User user) {
        return userService.isAdmin(user);
    }
}