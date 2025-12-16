package rs.raf.cloud.raf_cloud_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MachineEventDto {
    private Long id;
    private String action;
    private String message;
    private String timestamp;
}
