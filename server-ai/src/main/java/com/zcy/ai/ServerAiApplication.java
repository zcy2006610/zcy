package com.zcy.ai;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = "com.zcy.ai.mapper")
@SpringBootApplication(scanBasePackages = "com.zcy.ai")
public class ServerAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerAiApplication.class, args);
    }

}
