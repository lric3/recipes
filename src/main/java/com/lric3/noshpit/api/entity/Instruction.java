package com.lric3.noshpit.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "instructions")
public class Instruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_number")
    @Positive(message = "Step number must be positive")
    @NotNull(message = "Step number is required")
    private Integer stepNumber;

    @NotBlank(message = "Instruction description is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "estimated_time")
    private Integer estimatedTime; // in minutes

    @Column(name = "tips")
    private String tips;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @NotNull(message = "Recipe is required")
    private Recipe recipe;

    public Instruction() {}

    public Instruction(Integer stepNumber, String description, Recipe recipe) {
        this.stepNumber = stepNumber;
        this.description = description;
        this.recipe = recipe;
    }
}
