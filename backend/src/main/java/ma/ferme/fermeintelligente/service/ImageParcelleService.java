package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.entity.ImageParcelle;
import ma.ferme.fermeintelligente.repository.ImageParcelleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageParcelleService {
    private final ImageParcelleRepository imageParcelleRepository;

    public List<ImageParcelle> findByParcelle(Long parcelleId) {
        return imageParcelleRepository.findByParcelleId(parcelleId);
    }
}
