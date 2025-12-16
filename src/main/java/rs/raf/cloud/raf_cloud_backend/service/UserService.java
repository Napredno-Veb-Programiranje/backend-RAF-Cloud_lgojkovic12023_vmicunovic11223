package rs.raf.cloud.raf_cloud_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rs.raf.cloud.raf_cloud_backend.dto.CreateUserDto;
import rs.raf.cloud.raf_cloud_backend.dto.UpdateUserDto;
import rs.raf.cloud.raf_cloud_backend.dto.UserDto;
import rs.raf.cloud.raf_cloud_backend.model.Permission;
import rs.raf.cloud.raf_cloud_backend.model.User;
import rs.raf.cloud.raf_cloud_backend.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(CreateUserDto dto) {
        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .passwordHash(encoder.encode(dto.getPassword()))
                .permissions(dto.getPermissions().stream()
                        .map(Permission::valueOf)
                        .collect(Collectors.toSet()))
                .active(true)
                .build();

        return toDto(userRepository.save(user));
    }

    public UserDto updateUser(Long id, UpdateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getActive() != null) user.setActive(dto.getActive());

        if (dto.getPermissions() != null) {
            Set<Permission> perms = dto.getPermissions().stream()
                    .map(Permission::valueOf)
                    .collect(Collectors.toSet());
            user.setPermissions(perms);
        }

        return toDto(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getUserID(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPermissions().stream()
                        .map(Permission::getValue)
                        .collect(Collectors.toList()),
                false
        );
    }
}
