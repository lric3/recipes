package com.lric3.recipes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ingredient name is required")
    @Column(nullable = false)
    private String name;

    @Column(name = "amount")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @Column(name = "unit")
    private String unit;

    @Column(name = "notes")
    private String notes;

    @Column(name = "optional")
    private boolean optional = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @NotNull(message = "Recipe is required")
    private Recipe recipe;

    public Ingredient() {}

    public Ingredient(String name, Double amount, String unit, Recipe recipe) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.recipe = recipe;
    }
}
