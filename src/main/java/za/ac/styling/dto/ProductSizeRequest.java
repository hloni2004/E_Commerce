package za.ac.styling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSizeRequest {
    private Integer sizeId;  // For updates
    private String sizeName;
    private int stockQuantity;
}
