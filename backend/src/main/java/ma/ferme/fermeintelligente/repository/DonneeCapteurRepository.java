package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.DonneeCapteur;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonneeCapteurRepository extends JpaRepository<DonneeCapteur, Long> {
    List<DonneeCapteur> findByCapteurIdOrderByDateReleveDesc(Long capteurId);
    List<DonneeCapteur> findByCapteurIdAndDateReleveBetween(Long capteurId, LocalDateTime start, LocalDateTime end);
    Optional<DonneeCapteur> findFirstByCapteurIdOrderByDateReleveDesc(Long capteurId);

    @Query("SELECT AVG(d.valeur) FROM DonneeCapteur d WHERE d.capteur.parcelle.id = :parcelleId AND d.capteur.type = :type AND d.dateReleve > :since")
    Optional<Double> avgByParcelleAndTypeSince(@Param("parcelleId") Long parcelleId, @Param("type") String type, @Param("since") LocalDateTime since);

    @Query("SELECT d FROM DonneeCapteur d WHERE d.capteur.parcelle.id = :parcelleId AND d.dateReleve > :since ORDER BY d.dateReleve DESC")
    List<DonneeCapteur> findRecentByParcelle(@Param("parcelleId") Long parcelleId, @Param("since") LocalDateTime since);

    List<DonneeCapteur> findTop100ByCapteurIdOrderByDateReleveDesc(Long capteurId);
}
