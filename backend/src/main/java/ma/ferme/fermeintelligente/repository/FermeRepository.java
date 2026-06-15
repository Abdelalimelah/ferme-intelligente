package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Ferme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FermeRepository extends JpaRepository<Ferme, Long> {
    List<Ferme> findByProprietaireId(Long proprietaireId);
}
