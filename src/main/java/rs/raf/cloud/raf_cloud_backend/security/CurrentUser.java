package rs.raf.cloud.raf_cloud_backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rs.raf.cloud.raf_cloud_backend.model.Permission;

import java.util.Set;

@Getter
@AllArgsConstructor
public class CurrentUser {
    private Long id;
    private String email;
    private Set<Permission> permissions;
}
