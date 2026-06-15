package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Tache;
import ma.ferme.fermeintelligente.enums.StatutTache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long> {
    List<Tache> findByAgriculteurIdOrderByDateCreationDesc(Long agriculteurId);
    List<Tache> findByGestionnaireIdOrderByDateCreationDesc(Long gestionnaireId);
    List<Tache> findByStatut(StatutTache statut);
    long countByStatut(StatutTache statut);
    long countByAgriculteurIdAndStatut(Long agriculteurId, StatutTache statut);
}
