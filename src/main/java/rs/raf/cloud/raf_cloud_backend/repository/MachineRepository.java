package rs.raf.cloud.raf_cloud_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.raf.cloud.raf_cloud_backend.model.Machine;
import rs.raf.cloud.raf_cloud_backend.model.MachineStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long>, JpaSpecificationExecutor<Machine> {

}