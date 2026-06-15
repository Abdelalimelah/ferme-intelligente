package ma.ferme.fermeintelligente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class DashboardStats {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OwnerStats {
        private long totalFermes;
        private long totalGestionnaires;
        private long totalAgriculteurs;
        private long rapportsEnAttente;
        private List<RapportDTO> recentRapports;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ManagerStats {
        private long totalParcelles;
        private long capteursActifs;
        private long alertesActives;
        private long tachesEnCours;
        private Double moyenneTemperature;
        private Double moyenneHumidite;
        private Double moyennePH;
        private List<AlerteDTO> recentAlertes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WorkerStats {
        private long tachesAFaire;
        private long tachesEnCours;
        private long tachesTerminees;
        private List<TacheDTO> recentTaches;
        private List<ParcelleDTO> parcelles;
    }
}
