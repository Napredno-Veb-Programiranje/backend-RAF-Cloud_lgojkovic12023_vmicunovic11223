package rs.raf.cloud.raf_cloud_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MachineDto {
    private Long id;
    private String name;
    private String status;
    private String ownerEmail;
    private String createdAt;
}
