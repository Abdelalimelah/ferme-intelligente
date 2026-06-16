package ma.ferme.fermeintelligente.unit;

import ma.ferme.fermeintelligente.dto.ParcelleDetailDTO;
import ma.ferme.fermeintelligente.entity.ImageParcelle;
import ma.ferme.fermeintelligente.entity.Parcelle;
import ma.ferme.fermeintelligente.entity.ResultatAnalyse;
import ma.ferme.fermeintelligente.repository.*;
import ma.ferme.fermeintelligente.service.AIClassificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test for the disease-mapping logic, with all repositories mocked.
 * Guards against regressing the bug where "healthy" (model output, lowercase)
 * was compared against "Healthy" (capitalized) and leaked into disease lists
 * styled as alerts.
 */
@ExtendWith(MockitoExtension.class)
class AIClassificationServiceTest {

    @Mock private ImageParcelleRepository imageParcelleRepository;
    @Mock private ModeleIARepository modeleIARepository;
    @Mock private ResultatAnalyseRepository resultatAnalyseRepository;
    @Mock private AlerteRepository alerteRepository;
    @Mock private ParcelleRepository parcelleRepository;

    private AIClassificationService service() {
        // RestTemplate is unused by getDiseasesByParcelle — a real instance avoids
        // needing Mockito to instrument a concrete framework class for no reason.
        return new AIClassificationService(
                imageParcelleRepository, modeleIARepository, resultatAnalyseRepository,
                alerteRepository, parcelleRepository, new RestTemplate());
    }

    private ResultatAnalyse resultatWith(String maladieDetectee, Parcelle parcelle) {
        ImageParcelle image = ImageParcelle.builder()
                .cheminFichier("dataset-simulated")
                .dateCapture(LocalDateTime.now())
                .parcelle(parcelle)
                .build();
        return ResultatAnalyse.builder()
                .maladieDetectee(maladieDetectee)
                .niveauConfiance(0.9)
                .dateAnalyse(LocalDateTime.now())
                .image(image)
                .build();
    }

    @Test
    void getDiseasesByParcelle_marksLowercaseHealthyAsSain() {
        Parcelle parcelle = Parcelle.builder().id(1L).nom("Parcelle A1").typeCulture("Tomates").build();
        when(resultatAnalyseRepository.findByParcelleIdOrderByDateDesc(1L))
                .thenReturn(List.of(resultatWith("healthy", parcelle)));

        List<ParcelleDetailDTO.DiseaseResultDTO> results = service().getDiseasesByParcelle(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isSain()).isTrue();
    }

    @Test
    void getDiseasesByParcelle_marksActualDiseaseAsNotSain() {
        Parcelle parcelle = Parcelle.builder().id(1L).nom("Parcelle A1").typeCulture("Tomates").build();
        when(resultatAnalyseRepository.findByParcelleIdOrderByDateDesc(1L))
                .thenReturn(List.of(resultatWith("Late_blight", parcelle)));

        List<ParcelleDetailDTO.DiseaseResultDTO> results = service().getDiseasesByParcelle(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isSain()).isFalse();
    }
}
