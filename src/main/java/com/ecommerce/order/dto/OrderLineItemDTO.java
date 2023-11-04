package com.ecommerce.order.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemDTO {

    private Long id;
    private String skuCode;
    private Integer quantity;
    private BigDecimal price;
}
