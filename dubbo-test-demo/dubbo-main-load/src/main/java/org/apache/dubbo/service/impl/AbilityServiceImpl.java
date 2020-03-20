package org.apache.dubbo.service.impl;

import org.apache.dubbo.demo.DemoService;
import org.springframework.stereotype.Component;

@Component("abilityServiceImpl")

public class AbilityServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {

        System.out.println("provider call success!!!!");
        return "provider service call success!"+name;
    }
}
