package com.example.petmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Pet entity in the system.
 * This class is mapped to the "pet" table in the database.
 * It contains details about the pet such as name, age, type, and deceased status.
 * Additionally, it establishes a many-to-many relationship with the User entity.
 */
@Entity
@Table(name = "pet")
@Getter
@Setter
public class Pet extends Auditable{

    /**
     * The unique identifier for the pet.
     * It is auto-generated using the IDENTITY strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the pet.
     * This field is mandatory.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The age of the pet.
     * This field is mandatory.
     */
    @Column(nullable = false)
    private Integer age;

    /**
     * The type of the pet, e.g., spider, snake, cat, dog.
     * This field is mandatory.
     */
    @Column(nullable = false)
    private String type;

    /**
     * Indicates whether the pet is deceased.This is to handle death of a user scenario.
     * Defaults to false if not explicitly set.
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deceased = false;

    /**
     * The set of owners associated with the pet.
     * This establishes a many-to-many relationship with the User entity.
     * The relationship is mapped by the "pets" field in the User entity.
     */
    @ManyToMany(mappedBy = "pets", cascade = CascadeType.ALL)
    private Set<User> owners = new HashSet<>();
}