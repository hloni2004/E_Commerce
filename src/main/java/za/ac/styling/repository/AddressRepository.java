package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Address entity
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find all addresses for a user
     */
    List<Address> findByUser(User user);

    /**
     * Find all addresses for a user by user ID
     */
    List<Address> findByUserUserId(Integer userId);

    /**
     * Find default address for a user
     */
    Optional<Address> findByUserAndIsDefaultTrue(User user);

    /**
     * Find addresses by type for a user
     */
    List<Address> findByUserAndAddressType(User user, AddressType addressType);

    /**
     * Find default shipping address
     */
    Optional<Address> findByUserAndAddressTypeAndIsDefaultTrue(User user, AddressType addressType);
}
