package com.xiao.cs209a_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Cs209aProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(Cs209aProjectApplication.class, args);
        System.out.println("http://localhost:8080");
    }
}