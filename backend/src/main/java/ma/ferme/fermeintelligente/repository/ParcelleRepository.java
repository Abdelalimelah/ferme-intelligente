package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Parcelle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelleRepository extends JpaRepository<Parcelle, Long> {
    List<Parcelle> findByFermeId(Long fermeId);
}
