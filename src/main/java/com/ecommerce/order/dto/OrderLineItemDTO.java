package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineItemDTO {

    private Long id;
    @NotBlank
    private String skuCode;
    @Min(0)
    private Integer quantity;
    @Min(0)
    private BigDecimal price;
}
