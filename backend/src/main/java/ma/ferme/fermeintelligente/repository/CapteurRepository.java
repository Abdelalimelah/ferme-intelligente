package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Capteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CapteurRepository extends JpaRepository<Capteur, Long> {
    List<Capteur> findByParcelleId(Long parcelleId);
    List<Capteur> findByStatut(String statut);
    Optional<Capteur> findByParcelleIdAndType(Long parcelleId, String type);
}
