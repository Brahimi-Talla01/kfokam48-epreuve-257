package epreuve.kfokam48.backend.repository;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.SourcePresence;
import epreuve.kfokam48.backend.domain.StatutRelecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    List<Etudiant> findByPromotionIdOrderByNomAsc(Long promotionId);

    long countByPromotionId(Long promotionId);

    /**
     * Le tableau du formateur (Q16 / RG15) : présences, exercices déposés, moyenne des
     * notes reçues (RG17, null si aucune note), exercices non relus (RG10 / Q11) et,
     * pour RG13 / Q14, le détail des présences relevées par le formateur.
     * Une seule requête pour rester sous les 2 s à 60 étudiants (ENF2).
     */
    @Query("""
            select e.id, e.nom,
                   (select count(p) from Presence p
                     where p.session.promotion.id = :promotionId and p.etudiant.id = e.id),
                   (select count(x) from Exercice x
                     where x.session.promotion.id = :promotionId and x.etudiant.id = e.id),
                   (select avg(r.note) from Relecture r
                     where r.exercice.session.promotion.id = :promotionId
                       and r.exercice.etudiant.id = e.id),
                   (select count(r) from Relecture r
                     where r.exercice.session.promotion.id = :promotionId
                       and r.exercice.etudiant.id = e.id
                       and r.statut = :attente),
                   (select count(p) from Presence p
                     where p.session.promotion.id = :promotionId and p.etudiant.id = e.id
                       and p.source = :formateur)
              from Etudiant e
             where e.promotion.id = :promotionId
             order by e.nom
            """)
    List<Object[]> tableauPromotion(@Param("promotionId") Long promotionId,
                                    @Param("attente") StatutRelecture attente,
                                    @Param("formateur") SourcePresence formateur);
}
