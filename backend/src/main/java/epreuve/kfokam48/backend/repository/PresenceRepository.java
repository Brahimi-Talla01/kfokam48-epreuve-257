package epreuve.kfokam48.backend.repository;

import epreuve.kfokam48.backend.domain.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Presence> findBySessionIdOrderByMarqueLeAsc(Long sessionId);

    /** RG6 : le relecteur est tiré au hasard parmi les présents, auteur de l'exercice exclu. */
    @Query("""
            select p.etudiant.id
              from Presence p
             where p.session.id = :sessionId and p.etudiant.id <> :auteurId
            """)
    List<Long> idsPresentsSaufAuteur(@Param("sessionId") Long sessionId,
                                     @Param("auteurId") Long auteurId);
}
