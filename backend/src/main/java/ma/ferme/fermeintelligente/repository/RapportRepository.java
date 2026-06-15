package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Rapport;
import ma.ferme.fermeintelligente.enums.StatutRapport;
import ma.ferme.fermeintelligente.enums.TypeRapport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RapportRepository extends JpaRepository<Rapport, Long> {
    List<Rapport> findByAuteurIdOrderByDateCreationDesc(Long auteurId);
    List<Rapport> findByStatutOrderByDateCreationDesc(StatutRapport statut);
    List<Rapport> findByTypeOrderByDateCreationDesc(TypeRapport type);
    long countByStatut(StatutRapport statut);
    List<Rapport> findAllByOrderByDateCreationDesc();
}
