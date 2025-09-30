package com.zluolan.zojbackenduserservice.controller;

import com.zluolan.zojbackendcommon.common.BaseResponse;
import com.zluolan.zojbackendcommon.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试用户服务是否正常
     */
    @GetMapping("/ping")
    public BaseResponse<String> testPing() {
        return ResultUtils.success("User service is running!");
    }
}
