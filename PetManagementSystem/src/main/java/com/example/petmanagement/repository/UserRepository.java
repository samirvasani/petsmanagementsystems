package com.example.petmanagement.repository;

import com.example.petmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing User entities and their associations.
 * Provides custom queries for user-pet relationships and location-based searches.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds active users who own a specific active pet type in a given city.
     *
     * @param petType the type of pet to filter by (e.g., "dog", "cat")
     * @param city    the city name to filter by
     * @return list of matching users (empty if none found)
     */
    @Query("SELECT u FROM User u JOIN u.pets p WHERE p.type = :petType AND u.address.city = :city and u.deceased = false and p.deceased = false")
    List<User> findUserByPetTypeAndCity(String petType, String city);

    /**
     * Finds an active (non-deceased) user with their address eagerly loaded.
     *
     * @param id the user ID to search for (must not be null)
     * @return Optional containing the user if found and active, empty otherwise
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.address " +
            "WHERE u.id = :id AND u.deceased = false")
    Optional<User> findActiveUserWithAddress(@Param("id") Long id);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.name = :name AND u.firstName = :firstName and u.deceased = false")
    List<User> homonymExists(@Param("name") String name, @Param("firstName") String firstName);

}