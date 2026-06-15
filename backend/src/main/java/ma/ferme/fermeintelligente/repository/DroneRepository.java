package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Drone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DroneRepository extends JpaRepository<Drone, Long> {
}
