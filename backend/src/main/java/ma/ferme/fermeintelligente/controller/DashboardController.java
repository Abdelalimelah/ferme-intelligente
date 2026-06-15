package ma.ferme.fermeintelligente.controller;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.DashboardStats;
import ma.ferme.fermeintelligente.service.*;
import ma.ferme.fermeintelligente.enums.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final FermeService fermeService;
    private final UtilisateurService utilisateurService;
    private final ParcelleService parcelleService;
    private final CapteurService capteurService;
    private final AlerteService alerteService;
    private final TacheService tacheService;
    private final RapportService rapportService;
    private final ma.ferme.fermeintelligente.repository.TacheRepository tacheRepository;
    private final ma.ferme.fermeintelligente.repository.RapportRepository rapportRepository;
    private final ma.ferme.fermeintelligente.repository.CapteurRepository capteurRepository;
    private final ma.ferme.fermeintelligente.repository.DonneeCapteurRepository donneeCapteurRepository;

    @GetMapping("/owner/{userId}")
    public ResponseEntity<DashboardStats.OwnerStats> getOwnerStats(@PathVariable Long userId) {
        var fermes = fermeService.findByProprietaire(userId);
        var gestionnaires = utilisateurService.findByRole(Role.GESTIONNAIRE);
        var agriculteurs = utilisateurService.findByRole(Role.AGRICULTEUR);
        long rapportsEnAttente = rapportRepository.countByStatut(StatutRapport.NON_TRAITE);
        var recentRapports = rapportService.findAll().stream().limit(5).toList();

        return ResponseEntity.ok(DashboardStats.OwnerStats.builder()
                .totalFermes(fermes.size())
                .totalGestionnaires(gestionnaires.size())
                .totalAgriculteurs(agriculteurs.size())
                .rapportsEnAttente(rapportsEnAttente)
                .recentRapports(recentRapports)
                .build());
    }

    @GetMapping("/manager/{userId}")
    public ResponseEntity<DashboardStats.ManagerStats> getManagerStats(@PathVariable Long userId) {
        var parcelles = parcelleService.findAll();
        long capteursActifs = capteurRepository.findByStatut("ACTIF").size();
        long alertesActives = alerteService.countUnread();
        long tachesEnCours = tacheRepository.countByStatut(StatutTache.EN_COURS);

        var allCapteurs = capteurService.findAll();
        Double avgTemp = allCapteurs.stream()
                .filter(c -> "Température".equalsIgnoreCase(c.getType()) && c.getDerniereValeur() != null)
                .mapToDouble(c -> c.getDerniereValeur()).average().orElse(0);
        Double avgHum = allCapteurs.stream()
                .filter(c -> "Humidité".equalsIgnoreCase(c.getType()) && c.getDerniereValeur() != null)
                .mapToDouble(c -> c.getDerniereValeur()).average().orElse(0);
        Double avgPH = allCapteurs.stream()
                .filter(c -> "pH".equalsIgnoreCase(c.getType()) && c.getDerniereValeur() != null)
                .mapToDouble(c -> c.getDerniereValeur()).average().orElse(0);

        var recentAlertes = alerteService.findAll().stream().limit(5).toList();

        return ResponseEntity.ok(DashboardStats.ManagerStats.builder()
                .totalParcelles(parcelles.size())
                .capteursActifs(capteursActifs)
                .alertesActives(alertesActives)
                .tachesEnCours(tachesEnCours)
                .moyenneTemperature(avgTemp)
                .moyenneHumidite(avgHum)
                .moyennePH(avgPH)
                .recentAlertes(recentAlertes)
                .build());
    }

    @GetMapping("/worker/{userId}")
    public ResponseEntity<DashboardStats.WorkerStats> getWorkerStats(@PathVariable Long userId) {
        long aFaire = tacheRepository.countByAgriculteurIdAndStatut(userId, StatutTache.A_FAIRE);
        long enCours = tacheRepository.countByAgriculteurIdAndStatut(userId, StatutTache.EN_COURS);
        long terminee = tacheRepository.countByAgriculteurIdAndStatut(userId, StatutTache.TERMINEE);
        var recentTaches = tacheService.findByAgriculteur(userId).stream().limit(5).toList();

        return ResponseEntity.ok(DashboardStats.WorkerStats.builder()
                .tachesAFaire(aFaire)
                .tachesEnCours(enCours)
                .tachesTerminees(terminee)
                .recentTaches(recentTaches)
                .build());
    }
}
