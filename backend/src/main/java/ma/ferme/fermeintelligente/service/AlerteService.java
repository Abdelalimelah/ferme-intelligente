package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.AlerteDTO;
import ma.ferme.fermeintelligente.entity.Alerte;
import ma.ferme.fermeintelligente.entity.Parcelle;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.AlerteRepository;
import ma.ferme.fermeintelligente.repository.ParcelleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final ParcelleRepository parcelleRepository;

    @Cacheable("alertes")
    public List<AlerteDTO> findAll() {
        return alerteRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Cacheable("alertes_unread")
    public List<AlerteDTO> findUnread() {
        return alerteRepository.findByEstLueFalseOrderByDateCreationDesc().stream().map(this::toDTO).toList();
    }

    @Cacheable("alertes_count")
    public long countUnread() {
        return alerteRepository.countByEstLueFalse();
    }

    @Caching(evict = {
        @CacheEvict(value = "alertes",       allEntries = true),
        @CacheEvict(value = "alertes_unread",allEntries = true),
        @CacheEvict(value = "alertes_count", allEntries = true)
    })
    public void markAsRead(Long id) {
        Alerte alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte not found"));
        alerte.setEstLue(true);
        alerteRepository.save(alerte);
    }

    @Caching(evict = {
        @CacheEvict(value = "alertes",       allEntries = true),
        @CacheEvict(value = "alertes_unread",allEntries = true),
        @CacheEvict(value = "alertes_count", allEntries = true)
    })
    public void markAllAsRead() {
        alerteRepository.markAllAsRead();
    }

    @Caching(evict = {
        @CacheEvict(value = "alertes",       allEntries = true),
        @CacheEvict(value = "alertes_unread",allEntries = true),
        @CacheEvict(value = "alertes_count", allEntries = true)
    })
    public AlerteDTO create(AlerteDTO dto) {
        Parcelle parcelle = parcelleRepository.findById(dto.getParcelleId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle not found"));
        Alerte a = Alerte.builder()
                .type(dto.getType())
                .message(dto.getMessage())
                .niveau(dto.getNiveau())
                .dateCreation(LocalDateTime.now())
                .estLue(false)
                .parcelle(parcelle)
                .build();
        return toDTO(alerteRepository.save(a));
    }

    private AlerteDTO toDTO(Alerte a) {
        return AlerteDTO.builder()
                .id(a.getId()).type(a.getType())
                .message(a.getMessage()).niveau(a.getNiveau())
                .dateCreation(a.getDateCreation()).estLue(a.getEstLue())
                .parcelleId(a.getParcelle().getId())
                .parcelleNom(a.getParcelle().getNom())
                .resultatId(a.getResultat() != null ? a.getResultat().getId() : null)
                .build();
    }
}
