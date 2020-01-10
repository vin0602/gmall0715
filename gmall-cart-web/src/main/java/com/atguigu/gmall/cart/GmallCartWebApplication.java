package com.atguigu.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall")
public class GmallCartWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallCartWebApplication.class, args);
	}

}
