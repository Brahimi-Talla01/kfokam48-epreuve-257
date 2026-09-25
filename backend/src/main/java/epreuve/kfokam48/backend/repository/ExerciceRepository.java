package epreuve.kfokam48.backend.repository;

import epreuve.kfokam48.backend.domain.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Exercice> findBySessionIdOrderByIdAsc(Long sessionId);

    List<Exercice> findByEtudiantIdOrderByIdDesc(Long etudiantId);
}
