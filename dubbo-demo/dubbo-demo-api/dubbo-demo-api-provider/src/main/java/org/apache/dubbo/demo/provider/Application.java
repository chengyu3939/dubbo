/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.dubbo.demo.provider;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.demo.DemoService;

public class Application {
    public static void main(String[] args) throws Exception {
//        ServiceConfig<DemoServiceImpl> service = new ServiceConfig<>();
//        service.setApplication(new ApplicationConfig("dubbo-demo-api-provider"));
//        service.setRegistry(new RegistryConfig("zookeeper://192.168.50.72:2181"));
//        service.setInterface(DemoService.class);
//        service.setRef(new DemoServiceImpl());
//        service.export();
//        System.in.read();

        //详细配置

        //1.构建应用
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("dubbo-demo-api-provider");

        //构建协议
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("dubbo");
        protocolConfig.setPort(20881);
        protocolConfig.setThreads(200);

        //2.构建注册中心
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://192.168.50.72:2181");


        //开始应用引用


        ServiceConfig<DemoServiceImpl> serviceConfig = new ServiceConfig<>();
        //设置应用
        serviceConfig.setApplication(applicationConfig);
        //设置注册中心
        serviceConfig.setRegistry(registry);
        //设置协议
        serviceConfig.setProtocol(protocolConfig);

        //接口方法的设置
        serviceConfig.setInterface(DemoService.class);

        serviceConfig.setRef(new DemoServiceImpl());

        //版本号设置
        serviceConfig.setVersion("1.0.0");


        //暴露接口

        serviceConfig.export();

        System.out.println("结束了");
        //阻塞当前主线程
        System.in.read();


    }
}
