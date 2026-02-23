package rs.raf.cloud.raf_cloud_backend.dto;

import lombok.Getter;
import lombok.Setter;
import rs.raf.cloud.raf_cloud_backend.model.MachineStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MachineSearchDto {
    private String machineName;
    private List<MachineStatus> states;
    private LocalDate startDate;
    private LocalDate endDate;
}
