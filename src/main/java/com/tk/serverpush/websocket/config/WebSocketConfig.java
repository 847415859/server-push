package com.tk.serverpush.websocket.config;

import org.apache.catalina.Context;
import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @Description:
 * @Date : 2023/03/30 20:13
 * @Auther : tiankun
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }


    /**
     * 兼容HTTPS协议
     * WebSocket的ws协议是基于HTTP协议实现的
     * WebSocket的wss协议是基于HTTPS协议实现的
     * 一旦你的项目里面使用了https协议，你的websocket就要使用wss协议才可以。
     * @return
     */
    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        return context -> context.addServletContainerInitializer(new WsSci(), null);
    }
}
