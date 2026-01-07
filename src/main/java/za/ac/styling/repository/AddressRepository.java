package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser(User user);

    List<Address> findByUserUserId(Integer userId);

    Optional<Address> findByUserAndIsDefaultTrue(User user);

    List<Address> findByUserAndAddressType(User user, AddressType addressType);

    Optional<Address> findByUserAndAddressTypeAndIsDefaultTrue(User user, AddressType addressType);
}
