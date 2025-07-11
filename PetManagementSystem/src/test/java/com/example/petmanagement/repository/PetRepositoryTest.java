package com.example.petmanagement.repository;

import com.example.petmanagement.model.Address;
import com.example.petmanagement.model.Pet;
import com.example.petmanagement.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PetRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PetRepository petRepository;

    @Test
    void findByOwnerId_ValidOwnerId_ReturnsPets() {
        // Setup
        Address address = createAddress("Paris");
        User owner = createUser("Owner", "Test", address, "MALE");
        Pet pet = createPet("TestPet", "dog", owner);

        // Execute
        List<Pet> result = petRepository.findByOwnerId(owner.getId());

        // Verify
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(pet.getName(), result.get(0).getName());
        assertFalse(result.get(0).isDeceased());
    }

    @Test
    void findByCity_ValidCity_ReturnsPage() {
        // Setup
        Address address = createAddress("Paris");
        User owner = createUser("Owner", "Test", address, "MALE");
        Pet pet = createPet("TestPet", "dog", owner);

        // Execute
        //result return 2 results . 1 from this test case and one from data.sql where owner also owning pet 1,address
        Page<Pet> result = petRepository.findByCity(
                "Paris",
                PageRequest.of(0, 10)
        );

        // Verify
        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals("Fido", result.getContent().get(0).getName());
    }

    @Test
    void findPetsByWomenOwnersInCity_ValidCity_ReturnsPage() {
        // Setup
        Address address = createAddress("London");
        User owner = createUser("Owner", "Test", address, "FEMALE");
        Pet pet = createPet("TestPet", "dog", owner);

        // Execute
        Page<Pet> result = petRepository.findPetsByWomenOwnersInCity(
                "London",
                PageRequest.of(0, 10)
        );

        // Verify
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(pet.getName(), result.getContent().get(0).getName());
    }

    @Test
    void findPetsByWomenOwnersInCity_NullCity_ReturnsAllWomenOwnedPets() {
        // Setup - two pets in different cities owned by women
        Address address1 = createAddress("London");
        User owner1 = createUser("Owner1", "Test1", address1, "FEMALE");
        Pet pet1 = createPet("Pet1", "dog", owner1);

        Address address2 = createAddress("Paris");
        User owner2 = createUser("Owner2", "Test2", address2, "FEMALE");
        Pet pet2 = createPet("Pet2", "cat", owner2);

        // Execute with null city
        //result return 3 results . 2 from this test case and one from data.sql where owner also owning pet 1
        Page<Pet> result = petRepository.findPetsByWomenOwnersInCity(
                null,
                PageRequest.of(0, 10)
        );

        // Verify
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void findActivePetWithOwners_ActivePet_ReturnsPetWithOwners() {
        // Setup
        Address address = createAddress("New York");
        User owner = createUser("Owner", "Test", address, "MALE");
        Pet pet = createPet("ActivePet", "cat", owner);

        // Execute
        Optional<Pet> result = petRepository.findActivePetWithOwners(pet.getId());

        // Verify
        assertTrue(result.isPresent());
        assertEquals(pet.getName(), result.get().getName());
        assertFalse(result.get().getOwners().isEmpty());
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

    private Pet createPet(String name, String type, User owner) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setAge(1);
        pet.setType(type);
        pet.setDeceased(false);
        pet.getOwners().add(owner);
        owner.getPets().add(pet); // Maintain bidirectional relationship
        entityManager.persist(owner); // Re-persist owner to update relationship
        return entityManager.persist(pet);
    }
}
