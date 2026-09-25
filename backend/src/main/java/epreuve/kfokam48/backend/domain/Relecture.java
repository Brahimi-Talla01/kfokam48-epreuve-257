package epreuve.kfokam48.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Relecture d'un exercice par un pair, tiré au hasard parmi les présents (RG6), note
 * entière 0–20 (RG8), définitive une fois rendue (RG9 / Q15).
 * {@code relecteur_id} est nullable : aucun pair éligible à la session (cahier §7.2).
 * Étape 3 (RG18/RG19, cahier §7.3) : un exercice a jusqu'à **deux** relectures distinctes
 * (V3) ; la note retenue (moyenne ou provisoire) est calculée par {@code RelectureService},
 * jamais stockée sur cette entité.
 */
@Entity
@Table(name = "relecture",
        uniqueConstraints = @UniqueConstraint(name = "uk_relecture_exercice_relecteur",
                columnNames = {"exercice_id", "relecteur_id"}))
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    /** Nullable : aucun autre étudiant présent à la session. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relecteur_id")
    private Etudiant relecteur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 16)
    private StatutRelecture statut;

    @Column(name = "note")
    private Integer note;

    @Column(name = "commentaire", length = 2000)
    private String commentaire;

    @Column(name = "rendue_le")
    private LocalDateTime rendueLe;

    protected Relecture() {
    }

    public Relecture(Exercice exercice, Etudiant relecteur) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.statut = StatutRelecture.EN_ATTENTE;
    }

    /** RG9 : la note est définitive une fois rendue — seconde soumission → 409. */
    public void rendre(Integer note, String commentaire) {
        this.note = note;
        this.commentaire = commentaire;
        this.statut = StatutRelecture.RENDUE;
        this.rendueLe = LocalDateTime.now();
    }

    public boolean estRendue() {
        return statut == StatutRelecture.RENDUE;
    }

    public Long getId() {
        return id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public StatutRelecture getStatut() {
        return statut;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public LocalDateTime getRendueLe() {
        return rendueLe;
    }
}
