package rs.raf.cloud.raf_cloud_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.cloud.raf_cloud_backend.model.Machine;

public interface MachineRepository extends JpaRepository<Machine, Long> {
}

