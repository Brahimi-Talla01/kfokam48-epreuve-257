package epreuve.kfokam48.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Promotion KFOKAM48 — table {@code promotion}, voir D2 et {@code V1__init.sql}. */
@Entity
@Table(name = "promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 120)
    private String nom;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Promotion() {
    }

    public Promotion(String nom) {
        this.nom = nom;
    }

    @PrePersist
    void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
