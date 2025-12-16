package rs.raf.cloud.raf_cloud_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.raf.cloud.raf_cloud_backend.model.MachineEvent;

import java.util.List;

public interface MachineEventRepository extends JpaRepository<MachineEvent, Long> {

    List<MachineEvent> findByMachineId(Long machineId);

    //sve greske (admin)
    List<MachineEvent> findByActionOrderByTimestampDesc(String action);

    // greske samo za masine odredjenog korisnika (ako imas ideju da ne kucamo query dodaj)
    @Query("""
        select e from MachineEvent e
        where e.action = 'ERROR'
          and e.machine.owner.userID = :ownerId
        order by e.timestamp desc
    """)
    List<MachineEvent> findErrorsForOwner(@Param("ownerId") Long ownerId);
}
