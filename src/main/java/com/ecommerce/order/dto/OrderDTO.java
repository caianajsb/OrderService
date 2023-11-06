package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {

    private Long id;
    private String orderNumber;
    @Valid
    private List<OrderLineItemDTO> items;

}
