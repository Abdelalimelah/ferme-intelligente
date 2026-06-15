package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.SeuilAlerte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeuilAlerteRepository extends JpaRepository<SeuilAlerte, Long> {
    List<SeuilAlerte> findByParcelleId(Long parcelleId);
    Optional<SeuilAlerte> findByTypeCapteurAndParcelleId(String typeCapteur, Long parcelleId);
}
