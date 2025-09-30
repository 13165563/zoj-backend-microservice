package com.zluolan.zojbackendjudgeservice.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API客户端工具类
 */
@Slf4j
public class ApiClientUtil {

    /**
     * 使用简单密钥验证方式调用远程代码沙箱
     *
     * @param baseUrl 基础URL
     * @param code 代码
     * @param language 语言
     * @param inputList 输入列表
     * @return 响应字符串
     */
    public static String executeCodeWithSimpleAuth(String baseUrl, String code, String language, List<String> inputList) {
        // 构建请求对象
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("code", code);
        requestBody.put("language", language);
        requestBody.put("inputList", inputList);
        
        // 使用JSONUtil正确序列化请求对象
        String requestBodyStr = JSONUtil.toJsonStr(requestBody);
        
        log.info("发送请求到远程代码沙箱: {}", baseUrl);
        log.info("请求参数: {}", requestBodyStr);

        HttpResponse response = HttpRequest.post(baseUrl + "/executeCode")
                .header("Content-Type", "application/json")
                .header("auth", "secretKey")
                .body(requestBodyStr)
                .execute();

        String responseStr = response.body();
        log.info("远程代码沙箱响应: {}", responseStr);
        return responseStr;
    }
}
