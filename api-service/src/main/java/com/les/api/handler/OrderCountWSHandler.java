package com.les.api.handler;

import com.les.api.service.OrdersBigQueryService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class OrderCountWSHandler implements WebSocketHandler {

    private final OrdersBigQueryService ordersBigQueryService;

    public OrderCountWSHandler(OrdersBigQueryService ordersBigQueryService) {
        this.ordersBigQueryService = ordersBigQueryService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<WebSocketMessage> message = ordersBigQueryService.getOrdersCount()
                .map(Objects::toString)
                .map(session::textMessage);

        return session.send(message);
    }
}
