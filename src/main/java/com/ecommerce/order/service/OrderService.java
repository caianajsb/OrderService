/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.order.service;

import com.ecommerce.order.dto.InventoryDTO;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderLineItemDTO;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderLineItem;
import com.ecommerce.order.repository.OrderRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final WebClient.Builder webClientBuilder;

    public String placeOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItem> items = orderDTO.getItems()
                .stream()
                .map(this::mapFromDTO)
                .toList();
        order.setItems(items);

        List<String> skuCodes = items.stream()
                .map(orderLineItem -> orderLineItem.getSkuCode())
                .toList();

        InventoryDTO[] inventoryDTOs = webClientBuilder.build().get()
                .uri("http://inventory-service/ecommerce/inventories",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryDTO[].class)
                .block();

        Map<String, InventoryDTO> mapInventory = new HashMap<>();
        for (InventoryDTO item : inventoryDTOs) {
            mapInventory.put(item.getSkuCode(), item);
        }

        for (OrderLineItemDTO orderLineItemDTO : orderDTO.getItems()) {
            InventoryDTO item = mapInventory.get(orderLineItemDTO.getSkuCode());
            if (item == null
                    || item.getQuantity() < orderLineItemDTO.getQuantity()) {
                throw new IllegalArgumentException("Product is not in stock");
            } else if (orderLineItemDTO.getQuantity() > 0){
                item.setQuantity(item.getQuantity() - orderLineItemDTO.getQuantity());
            }
        }

        repository.save(order);

        String result = webClientBuilder.build().put()
                .uri("http://inventory-service/ecommerce/inventories")
                .body(Mono.just(Arrays.asList(inventoryDTOs)), new ParameterizedTypeReference<List<InventoryDTO>>() {
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();       
        return "Order placed sucessfully";
    }

    private OrderLineItem mapFromDTO(OrderLineItemDTO item) {
        OrderLineItem orderLineItem = OrderLineItem
                .builder()
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .skuCode(item.getSkuCode())
                .build();

        return orderLineItem;
    }
    
    private OrderLineItemDTO mapToDTO(OrderLineItem item) {
        OrderLineItemDTO orderLineItemDTO = OrderLineItemDTO
                .builder()
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .skuCode(item.getSkuCode())
                .build();

        return orderLineItemDTO;
    }

    public List<OrderDTO> getAllOrders() {
        List<Order> orders = repository.findAll();
        List<OrderDTO> ordersDTOs = orders.stream().map(order -> OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .items(order.getItems().stream().map(item -> mapToDTO(item)).toList())
                .build()
        ).toList();
        return ordersDTOs;
    }
}
