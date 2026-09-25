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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Session de cours : le formateur ouvre, le système expire le code (RG1 / Q2),
 * le formateur clôture (RG16 / Q3-Q10-Q12). Les deux instants sont distincts.
 */
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 16)
    private StatutSession statut;

    @Column(name = "ouverture_at", nullable = false)
    private LocalDateTime ouvertureAt;

    /** Fin du code de présence — RG1, Q2. Ne jamais confondre avec {@code clotureAt}. */
    @Column(name = "expiration_at", nullable = false)
    private LocalDateTime expirationAt;

    /** Fin de session — RG16, Q3/Q12 : null tant que le formateur n'a pas clôturé. */
    @Column(name = "cloture_at")
    private LocalDateTime clotureAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Session() {
    }

    public Session(String titre, Promotion promotion, String code,
                   LocalDateTime ouvertureAt, LocalDateTime expirationAt) {
        this.titre = titre;
        this.promotion = promotion;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = expirationAt;
        this.statut = StatutSession.OUVERTE;
    }

    @PrePersist
    void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean estOuverte() {
        return statut == StatutSession.OUVERTE;
    }

    public void cloturer(LocalDateTime maintenant) {
        this.statut = StatutSession.CLOTUREE;
        this.clotureAt = maintenant;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getCode() {
        return code;
    }

    public StatutSession getStatut() {
        return statut;
    }

    public LocalDateTime getOuvertureAt() {
        return ouvertureAt;
    }

    public LocalDateTime getExpirationAt() {
        return expirationAt;
    }

    public LocalDateTime getClotureAt() {
        return clotureAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
