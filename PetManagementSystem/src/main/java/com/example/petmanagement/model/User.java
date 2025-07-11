package com.example.petmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a User entity in the system.
 * This class is mapped to the "user" table in the database.
 * It contains details about the user such as name, first name, age, gender, address, and associated pets.
 */
@Entity
@Table(name = "\"user\"",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"name", "first_name"}))
@Getter
@Setter
public class User extends Auditable {

    /**
     * The unique identifier for the user.
     * It is auto-generated using the IDENTITY strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the user.
     * This field is mandatory.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The first name of the user.
     * This field is mapped to the "first_name" column and is mandatory.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * The address associated with the user.
     * This is a mandatory field and is mapped to the "address_id" column.
     * It establishes a many-to-one relationship with the Address entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    /**
     * The age of the user.
     * This field is mandatory.
     */
    @Column(nullable = false)
    private Integer age;

    /**
     * The gender of the user.
     * This field is mandatory and can be one of the following values: MALE, FEMALE, OTHER.
     */
    @Column(nullable = false)
    private String gender;

    //TODO : add below column and remove String gender
//    @Column(nullable = false)
//    @Enumerated(EnumType.STRING)
//    private Gender gender;

    /**
     * Indicates whether the user is deceased.This is to handle death of a user scenario.
     * Defaults to false if not explicitly set.
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deceased = false;

    /**
     * The set of pets associated with the user.
     * This establishes a many-to-many relationship with the Pet entity.
     * The relationship is mapped through the "user_pet" join table.
     */
    @ManyToMany
    @JoinTable(
            name = "user_pet",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "pet_id")
    )
    private Set<Pet> pets = new HashSet<>();

    public void addPet(Pet pet) {
        if (pet == null) return;
        this.pets.add(pet);
        pet.getOwners().add(this); // Maintain bidirectional relationship
    }
}