package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.ImageParcelle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageParcelleRepository extends JpaRepository<ImageParcelle, Long> {
    List<ImageParcelle> findByParcelleId(Long parcelleId);
    List<ImageParcelle> findByParcelleIdOrderByDateCaptureDesc(Long parcelleId);
    List<ImageParcelle> findTop10ByParcelleIdOrderByDateCaptureDesc(Long parcelleId);
}
