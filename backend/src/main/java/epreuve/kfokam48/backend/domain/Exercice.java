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
 * Exercice déposé par un étudiant pour une session : un lien, pas un fichier (Q13).
 * Un seul dépôt par (étudiant, session) → {@code 409 EXERCICE_DEJA_DEPOSE}.
 */
@Entity
@Table(name = "exercice",
        uniqueConstraints = @UniqueConstraint(name = "uk_exercice_etudiant_session",
                columnNames = {"session_id", "etudiant_id"}))
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(name = "lien", nullable = false, length = 500)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 16)
    private StatutExercice statut;

    @Column(name = "depose_le", nullable = false)
    private LocalDateTime deposeLe;

    @Column(name = "mise_a_jour_le", nullable = false)
    private LocalDateTime miseAJourLe;

    protected Exercice() {
    }

    public Exercice(Session session, Etudiant etudiant, String lien) {
        this.session = session;
        this.etudiant = etudiant;
        this.lien = lien;
        this.statut = StatutExercice.EN_ATTENTE;
        this.deposeLe = LocalDateTime.now();
        this.miseAJourLe = deposeLe;
    }

    /** RG12 / Q13, tranché au §7.2 : « pas encore relu » = statut EN_ATTENTE. */
    public boolean estModifiable() {
        return statut == StatutExercice.EN_ATTENTE;
    }

    /** RG12 / Q13 : le lien reste remplaçable tant que la relecture n'est pas rendue. */
    public void remplacerLien(String nouveauLien) {
        this.lien = nouveauLien;
        this.miseAJourLe = LocalDateTime.now();
    }

    public void marquerRendu() {
        this.statut = StatutExercice.RENDU;
        this.miseAJourLe = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public String getLien() {
        return lien;
    }

    public StatutExercice getStatut() {
        return statut;
    }

    public LocalDateTime getDeposeLe() {
        return deposeLe;
    }

    public LocalDateTime getMiseAJourLe() {
        return miseAJourLe;
    }
}
