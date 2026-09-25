package epreuve.kfokam48.backend.repository;

import epreuve.kfokam48.backend.domain.Relecture;
import epreuve.kfokam48.backend.domain.StatutRelecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExerciceId(Long exerciceId);

    boolean existsByExerciceId(Long exerciceId);

    List<Relecture> findByRelecteurIdAndStatutOrderByIdDesc(Long relecteurId, StatutRelecture statut);

    List<Relecture> findByRelecteurIdOrderByIdDesc(Long relecteurId);

    List<Relecture> findByExerciceEtudiantIdOrderByIdDesc(Long etudiantId);
}
