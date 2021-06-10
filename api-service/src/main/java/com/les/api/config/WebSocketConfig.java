package com.les.api.config;

import com.les.api.handler.OrderCountWSHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Autowired
    private OrderCountWSHandler orderCountWSHandler;

    @Bean
    public HandlerMapping orderCountWSHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/orders/count/current", orderCountWSHandler);
        
        return new SimpleUrlHandlerMapping(map, -1);
    }

    @Bean
    public HandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
