package com.example.petmanagement.service;

import com.example.petmanagement.dto.AddressDto;
import com.example.petmanagement.model.Address;
import com.example.petmanagement.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AddressService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;

    /**
     * This method retrieves address based on give Dto input
     * @param addressDto contains address dto from client
     * @return Address
     */
    @Transactional
    public Address getOrCreateAddress(AddressDto addressDto) {
        LOGGER.info("Fetch address details for the given dto {}",addressDto);
        Optional<Address> existingAddress = addressRepository.findByCityAndTypeAndAddressNameAndNumber(
                addressDto.city(),
                addressDto.type(),
                addressDto.addressName(),
                addressDto.number()
        );

        return existingAddress.orElseGet(() -> createAddress(addressDto));
    }

    /**
     * This method creates address based on fetched data from the Address table
     * @param addressDto contains address dto from client
     * @return Address furnished address object
     */
    private Address createAddress(AddressDto addressDto) {
        Address address = new Address();
        address.setCity(addressDto.city());
        address.setType(addressDto.type());
        address.setAddressName(addressDto.addressName());
        address.setNumber(addressDto.number());
        return addressRepository.save(address);
    }
}