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
 * Présence d'un étudiant à une session. Unicité (session, étudiant) → RG14 / {@code 409 DEJA_PRESENT},
 * source {@code ETUDIANT} ou {@code FORMATEUR} → RG13 / Q14.
 */
@Entity
@Table(name = "presence",
        uniqueConstraints = @UniqueConstraint(name = "uk_presence_etudiant_session",
                columnNames = {"session_id", "etudiant_id"}))
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    private SourcePresence source;

    @Column(name = "marque_le", nullable = false)
    private LocalDateTime marqueLe;

    protected Presence() {
    }

    public Presence(Session session, Etudiant etudiant, SourcePresence source) {
        this.session = session;
        this.etudiant = etudiant;
        this.source = source;
        this.marqueLe = LocalDateTime.now();
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

    public SourcePresence getSource() {
        return source;
    }

    public LocalDateTime getMarqueLe() {
        return marqueLe;
    }
}
