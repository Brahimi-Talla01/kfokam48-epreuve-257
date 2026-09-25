package epreuve.kfokam48.backend.repository;

import epreuve.kfokam48.backend.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByCode(String code);

    boolean existsByCode(String code);

    List<Session> findByPromotionIdOrderByIdDesc(Long promotionId);
}
