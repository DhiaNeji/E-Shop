package org.sid.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.sid.config.WebClientConfig;
import org.sid.dto.InventoryResponse;
import org.sid.dto.OrderLineItemsDto;
import org.sid.dto.OrderRequest;
import org.sid.event.OrderNotificationEvent;
import org.sid.model.Order;
import org.sid.model.OrderLineItems;
import org.sid.repository.OrderRepository;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.UriSpec;
import org.springframework.web.util.UriBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {


    private final OrderRepository orderRepository;
    
    private final WebClient.Builder webClientBuilder;

    private final KafkaTemplate<String,OrderNotificationEvent> kafkaTemplate;
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes=order.getOrderLineItemsList().stream()
        		.map(OrderLineItems::getSkuCode).toList();
        
        InventoryResponse[] invenvtoryResponseArray = webClientBuilder.build().get()
        		.uri("http://inventory-service/api/inventory/",uriBuilder->uriBuilder.queryParam("skuCode", skuCodes).build())
        		.retrieve()
        		.bodyToMono(InventoryResponse[].class)
        		.block();
        boolean allProductsInStock=Arrays.stream(invenvtoryResponseArray)
        		.allMatch(InventoryResponse::isInStock);    
        if(allProductsInStock)
        {
        	orderRepository.save(order);
        	kafkaTemplate.send("NotificationTopic",new OrderNotificationEvent(order.getOrderNumber()));
        	return "Order placed Successfully";
        }
        else
        throw new IllegalArgumentException("Produt is not in stock");
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
