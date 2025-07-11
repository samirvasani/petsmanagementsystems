package com.example.petmanagement.repository;

import com.example.petmanagement.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByCityAndTypeAndAddressNameAndNumber(
            String city, String type, String addressName, String number);
}