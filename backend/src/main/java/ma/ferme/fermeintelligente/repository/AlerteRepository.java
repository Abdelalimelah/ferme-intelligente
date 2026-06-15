package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    List<Alerte> findByParcelleIdOrderByDateCreationDesc(Long parcelleId);
    List<Alerte> findByEstLueFalseOrderByDateCreationDesc();
    long countByEstLueFalse();

    @Modifying
    @Transactional
    @Query("UPDATE Alerte a SET a.estLue = TRUE WHERE a.estLue = FALSE")
    void markAllAsRead();
}
