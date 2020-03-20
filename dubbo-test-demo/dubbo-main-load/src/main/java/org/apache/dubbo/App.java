package org.apache.dubbo;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Hello world!
 */
@ComponentScan("org.apache.dubbo.service.impl")
@Configuration
public class App {
    public static void main(String[] args) {
        org.apache.dubbo.container.Main.main(args);
        System.out.println("Hello World!");
    }
}
