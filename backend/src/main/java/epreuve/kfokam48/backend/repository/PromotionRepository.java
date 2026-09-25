package epreuve.kfokam48.backend.repository;

import epreuve.kfokam48.backend.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findAllByOrderByIdAsc();

    boolean existsByNom(String nom);
}
