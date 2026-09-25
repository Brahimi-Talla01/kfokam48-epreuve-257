package epreuve.kfokam48.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Étudiant — un nom, pas de compte, pas de mot de passe (Q1 : aucune authentification). */
@Entity
@Table(name = "etudiant")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 160)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Etudiant() {
    }

    public Etudiant(String nom, Promotion promotion) {
        this.nom = nom;
        this.promotion = promotion;
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

    public Promotion getPromotion() {
        return promotion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
