package za.ac.styling.service;

import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.User;

import java.util.List;
import java.util.Optional;

public interface AddressService extends IService<Address, Long> {

    List<Address> findByUser(User user);

    List<Address> findByUserId(Integer userId);

    Optional<Address> findDefaultAddressByUser(User user);

    List<Address> findByUserAndAddressType(User user, AddressType addressType);

    Address setAsDefault(Long addressId);

    void removeDefaultStatus(User user);
}
