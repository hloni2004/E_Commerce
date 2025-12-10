package za.ac.styling.service;

import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Address entity
 */
public interface AddressService extends IService<Address, Long> {

    /**
     * Find all addresses for a user
     */
    List<Address> findByUser(User user);

    /**
     * Find all addresses for a user by user ID
     */
    List<Address> findByUserId(Integer userId);

    /**
     * Find default address for a user
     */
    Optional<Address> findDefaultAddressByUser(User user);

    /**
     * Find addresses by type for a user
     */
    List<Address> findByUserAndAddressType(User user, AddressType addressType);

    /**
     * Set address as default for user
     */
    Address setAsDefault(Long addressId);

    /**
     * Remove default status from all user addresses
     */
    void removeDefaultStatus(User user);
}
