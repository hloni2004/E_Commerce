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
public class ProductColourRequest {
    private Integer colourId;  // For updates
    private String name;
    private String hexCode;
    private List<ProductSizeRequest> sizes;
}
