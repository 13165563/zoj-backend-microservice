package com.zluolan.zojbackendcommon.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文工具类
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取 ApplicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过名称获取 Bean
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 通过类型获取 Bean
     *
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanType) {
        return applicationContext.getBean(beanType);
    }

    /**
     * 通过名称和类型获取 Bean
     *
     * @param beanName
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName, Class<T> beanType) {
        return applicationContext.getBean(beanName, beanType);
    }
}