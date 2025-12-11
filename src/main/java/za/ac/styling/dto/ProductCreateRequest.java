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
    private Integer productId;  // For updates
    private String name;
    private String description;
    private double basePrice;
    private Double comparePrice;
    private String sku;
    private Double weight;
    private Long categoryId;
    private boolean isActive;
    private List<String> imageBase64List;  // Base64 encoded images
    private List<Long> existingImageIds;  // IDs of images to keep during update
    private List<ProductColourRequest> colours;
}
