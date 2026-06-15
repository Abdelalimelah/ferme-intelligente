package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.entity.Drone;
import ma.ferme.fermeintelligente.repository.DroneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DroneService {
    private final DroneRepository droneRepository;

    public List<Drone> findAll() {
        return droneRepository.findAll();
    }
}
