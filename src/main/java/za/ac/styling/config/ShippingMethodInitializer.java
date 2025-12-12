package za.ac.styling.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import za.ac.styling.domain.ShippingMethod;
import za.ac.styling.repository.ShippingMethodRepository;

@Component
@RequiredArgsConstructor
public class ShippingMethodInitializer implements CommandLineRunner {

    private final ShippingMethodRepository shippingMethodRepository;

    @Override
    public void run(String... args) {
        // Only initialize if no shipping methods exist
        if (shippingMethodRepository.count() == 0) {
            initializeShippingMethods();
        }
    }

    private void initializeShippingMethods() {
        ShippingMethod standard = ShippingMethod.builder()
                .name("Standard Delivery")
                .description("Delivery within 5-7 business days")
                .cost(50.00)
                .estimatedDays(7)
                .isActive(true)
                .build();

        ShippingMethod express = ShippingMethod.builder()
                .name("Express Delivery")
                .description("Delivery within 2-3 business days")
                .cost(120.00)
                .estimatedDays(3)
                .isActive(true)
                .build();

        ShippingMethod overnight = ShippingMethod.builder()
                .name("Overnight Delivery")
                .description("Next business day delivery")
                .cost(200.00)
                .estimatedDays(1)
                .isActive(true)
                .build();

        ShippingMethod pickup = ShippingMethod.builder()
                .name("Store Pickup")
                .description("Collect from our store - FREE")
                .cost(0.00)
                .estimatedDays(0)
                .isActive(true)
                .build();

        shippingMethodRepository.save(standard);
        shippingMethodRepository.save(express);
        shippingMethodRepository.save(overnight);
        shippingMethodRepository.save(pickup);

        System.out.println(" Initialized 4 shipping methods");
    }
}
