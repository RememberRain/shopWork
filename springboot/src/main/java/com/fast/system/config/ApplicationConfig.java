package com.fast.system.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 程序基础配置类
 *
 * 这个类是springboot应用程序的"基础设置中心", 相当于整个系统的"基础配置办公室"
 * 它负责配置一些通用ed全局的应用程序设置
 */
@Configuration //告诉spring: "我是配置类, 你启动的时候要读取我的配置"
@MapperScan("com.fast.**.mapper") //告诉mybatis 去哪里找数据库接口
public class ApplicationConfig {

    /**
     * 时区配置bean
     */
    @Bean //告诉spring: "把我返回的对象放到容器里, 其他地方可以拿来用"
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder ->
                //设置时区为系统默认时区
                jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }


}
