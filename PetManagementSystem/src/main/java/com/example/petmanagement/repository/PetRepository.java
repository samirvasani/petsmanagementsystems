package com.example.petmanagement.repository;

import com.example.petmanagement.model.Pet;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Pet entities and their associations.
 * Provides custom queries for user-pet relationships and location-based searches.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    /**
     * Finds active/alive pets by the owner's ID.
     *
     * @param userId the ID of the owner for which pets needs to be find out
     * @return a list of pets owned by the specified owner
     */
    @Query("SELECT p FROM Pet p JOIN p.owners u WHERE u.id = :userId and p.deceased = false")
    List<Pet> findByOwnerId(Long userId);

    /**
     * find active pets in the specified city
     * @param city  The city name to filter pets by (case-sensitive). Must not be blank or empty.
     * @param pageable pagination data like page number,page size,sorting criteria
     * @return active page for the given size
     */
    @Query("SELECT p FROM Pet p JOIN p.owners u WHERE u.address.city = :city and p.deceased = false")
    Page<Pet> findByCity(
            @Param("city") @Nullable String city,
            Pageable pageable
    );

    /**
     * fetch active pets for female user in city
     * @param city The city name to filter pets by (case-sensitive). Must not be blank or empty.
     * @param pageable pagination data like page number,page size,sorting criteria
     * @return Pet with given page size
     */
    @QueryHints({
            @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
            @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    @Query("SELECT DISTINCT p FROM Pet p JOIN FETCH p.owners u " +
            "WHERE u.gender = 'FEMALE' " +
            "AND p.deceased = false " +
            "AND (:city IS NULL OR LOWER(u.address.city) = LOWER(:city)) " +
            "ORDER BY p.name")
    Page<Pet> findPetsByWomenOwnersInCity(
            @Param("city") @Nullable String city,
            Pageable pageable
    );


    @Query("SELECT p FROM Pet p LEFT JOIN FETCH p.owners o LEFT JOIN FETCH o.address " +
            "WHERE p.id = :id AND p.deceased = false")
    Optional<Pet> findActivePetWithOwners(@Param("id") Long id);
}