package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.ResultatAnalyse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResultatAnalyseRepository extends JpaRepository<ResultatAnalyse, Long> {
    @Query("SELECT r FROM ResultatAnalyse r WHERE r.image.parcelle.id = :parcelleId ORDER BY r.dateAnalyse DESC")
    List<ResultatAnalyse> findByParcelleIdOrderByDateDesc(@Param("parcelleId") Long parcelleId);

    @Query("SELECT COUNT(r) FROM ResultatAnalyse r WHERE r.image.parcelle.id = :parcelleId AND r.maladieDetectee IS NOT NULL AND r.maladieDetectee <> 'Healthy'")
    long countDiseasesbyParcelle(@Param("parcelleId") Long parcelleId);
}
