package za.ac.styling.factory;

import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.User;
import za.ac.styling.util.ValidationHelper;

public class AddressFactory {

    public static Address createAddress(String street, String city, String state,
            String zipCode, String country, AddressType addressType,
            User user) {

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

    public static Address createDefaultAddress(String street, String city, String state,
            String zipCode, String country,
            AddressType addressType, User user) {

        Address address = createAddress(street, city, state, zipCode, country, addressType, user);
        address.setDefault(true);

        return address;
    }

    public static Address createShippingAddress(String street, String city, String state,
            String zipCode, String country, User user) {

        return createAddress(street, city, state, zipCode, country, AddressType.SHIPPING, user);
    }

    public static Address createBillingAddress(String street, String city, String state,
            String zipCode, String country, User user) {

        return createAddress(street, city, state, zipCode, country, AddressType.BILLING, user);
    }

    public static Address createBothAddress(String street, String city, String state,
            String zipCode, String country, User user) {

        return createAddress(street, city, state, zipCode, country, AddressType.BOTH, user);
    }

    public static Address createDefaultShippingAddress(String street, String city, String state,
            String zipCode, String country, User user) {

        Address address = createShippingAddress(street, city, state, zipCode, country, user);
        address.setDefault(true);

        return address;
    }
}
