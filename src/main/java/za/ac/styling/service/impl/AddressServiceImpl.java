package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.User;
import za.ac.styling.repository.AddressRepository;
import za.ac.styling.service.AddressService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Address entity
 */
@Service
public class AddressServiceImpl implements AddressService {

    private AddressRepository addressRepository;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Address create(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public Address read(Long id) {
        return addressRepository.findById(id).orElse(null);
    }

    @Override
    public Address update(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public List<Address> getAll() {
        return addressRepository.findAll();
    }

    @Override
    public List<Address> findByUser(User user) {
        return addressRepository.findByUser(user);
    }

    @Override
    public List<Address> findByUserId(Integer userId) {
        return addressRepository.findByUserUserId(userId);
    }

    @Override
    public Optional<Address> findDefaultAddressByUser(User user) {
        return addressRepository.findByUserAndIsDefaultTrue(user);
    }

    @Override
    public List<Address> findByUserAndAddressType(User user, AddressType addressType) {
        return addressRepository.findByUserAndAddressType(user, addressType);
    }

    @Override
    public Address setAsDefault(Long addressId) {
        Address address = read(addressId);
        if (address != null) {
            removeDefaultStatus(address.getUser());
            address.setDefault(true);
            return update(address);
        }
        return null;
    }

    @Override
    public void removeDefaultStatus(User user) {
        List<Address> userAddresses = findByUser(user);
        for (Address address : userAddresses) {
            if (address.isDefault()) {
                address.setDefault(false);
                update(address);
            }
        }
    }

    @Override
    public void delete(Long id) {
        addressRepository.deleteById(id);
    }
}
