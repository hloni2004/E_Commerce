package za.ac.styling.factory;

import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.User;
import za.ac.styling.util.ValidationHelper;

/**
 * Factory class responsible for creating Address objects
 */
public class AddressFactory {

    /**
     * Creates a new Address with basic information
     */
    public static Address createAddress(String street, String city, String state,
            String zipCode, String country, AddressType addressType,
            User user) {

        // Validate input data
        if (ValidationHelper.isNullOrEmpty(street)) {
            throw new IllegalArgumentException("Street address cannot be empty");
        }

        if (ValidationHelper.isNullOrEmpty(city)) {
            throw new IllegalArgumentException("City cannot be empty");
        }

        if (ValidationHelper.isNullOrEmpty(state)) {
            throw new IllegalArgumentException("State cannot be empty");
        }

        if (!ValidationHelper.isValidZipCode(zipCode)) {
            throw new IllegalArgumentException("Invalid zip code format");
        }

        if (ValidationHelper.isNullOrEmpty(country)) {
            throw new IllegalArgumentException("Country cannot be empty");
        }

        if (addressType == null) {
            throw new IllegalArgumentException("Address type is required");
        }

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        return Address.builder()
                .addressLine1(street)
                .city(city)
                .province(state)
                .postalCode(zipCode)
                .country(country)
                .addressType(addressType)
                .user(user)
                .isDefault(false)
                .build();
    }

    /**
     * Creates a new default Address
     */
    public static Address createDefaultAddress(String street, String city, String state,
            String zipCode, String country,
            AddressType addressType, User user) {

        Address address = createAddress(street, city, state, zipCode, country, addressType, user);
        address.setDefault(true);

        return address;
    }

    /**
     * Creates a shipping address
     */
    public static Address createShippingAddress(String street, String city, String state,
            String zipCode, String country, User user) {

        return createAddress(street, city, state, zipCode, country, AddressType.SHIPPING, user);
    }

    /**
     * Creates a billing address
     */
    public static Address createBillingAddress(String street, String city, String state,
            String zipCode, String country, User user) {

        return createAddress(street, city, state, zipCode, country, AddressType.BILLING, user);
    }

    /**
     * Creates an address that serves both as shipping and billing
     */
    public static Address createBothAddress(String street, String city, String state,
            String zipCode, String country, User user) {

        return createAddress(street, city, state, zipCode, country, AddressType.BOTH, user);
    }

    /**
     * Creates a default shipping address
     */
    public static Address createDefaultShippingAddress(String street, String city, String state,
            String zipCode, String country, User user) {

        Address address = createShippingAddress(street, city, state, zipCode, country, user);
        address.setDefault(true);

        return address;
    }
}
