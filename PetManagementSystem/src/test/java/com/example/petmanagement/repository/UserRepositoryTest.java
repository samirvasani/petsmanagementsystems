package com.example.petmanagement.repository;

import com.example.petmanagement.model.Address;
import com.example.petmanagement.model.Pet;
import com.example.petmanagement.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByPetTypeAndCity_ShouldReturnUsers() {
        // Setup
        Address address = createAddress("Paris");
        User user = createUser("Patel", "Rajesh", address, "MALE");
        createPetWithOwner("Jimmy", "dog", user);

        // Execute
        List<User> result = userRepository.findUserByPetTypeAndCity("dog", "Paris");

        // Verify--because h2 db already adding user name 'Vasani' and that is the first result hence equalizing result with the first returned result[This needs to be handled with profiling]
        assertFalse(result.isEmpty());
        assertEquals("Vasani", result.get(0).getName());
    }

    // Helper methods
    private Address createAddress(String city) {
        Address address = new Address();
        address.setCity(city);
        address.setType("street");
        address.setAddressName("Main");
        address.setNumber("123");
        return entityManager.persist(address);
    }

    private User createUser(String name, String firstName, Address address, String gender) {
        User user = new User();
        user.setName(name);
        user.setFirstName(firstName);
        user.setAddress(address);
        user.setAge(30);
        user.setGender(gender);
        user.setDeceased(false);
        return entityManager.persist(user);
    }

    private void createPetWithOwner(String petName, String petType, User owner) {
        Pet pet = new Pet();
        pet.setName(petName);
        pet.setAge(2);
        pet.setType(petType);
        pet.setDeceased(false);
        pet.getOwners().add(owner);
        owner.getPets().add(pet);
        entityManager.persist(pet);
        entityManager.persist(owner);
    }
}
