package za.ac.styling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
    private Integer productId;
    private String name;
    private String description;
    private double basePrice;
    private Double comparePrice;
    private String sku;
    private Double weight;
    private Long categoryId;
    private boolean isActive;
    private List<String> imageBase64List;
    private List<Long> existingImageIds;
    private List<ProductColourRequest> colours;
}
