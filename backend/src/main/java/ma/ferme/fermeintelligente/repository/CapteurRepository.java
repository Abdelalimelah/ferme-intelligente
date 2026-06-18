package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Capteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CapteurRepository extends JpaRepository<Capteur, Long> {
    List<Capteur> findByParcelleId(Long parcelleId);
    List<Capteur> findByStatut(String statut);
    Optional<Capteur> findByParcelleIdAndType(Long parcelleId, String type);

    @Query("SELECT AVG(c.derniereValeur) FROM Capteur c WHERE LOWER(c.type) LIKE LOWER(:pattern) AND c.derniereValeur IS NOT NULL")
    Double avgDerniereValeurByTypePattern(@Param("pattern") String pattern);
}
