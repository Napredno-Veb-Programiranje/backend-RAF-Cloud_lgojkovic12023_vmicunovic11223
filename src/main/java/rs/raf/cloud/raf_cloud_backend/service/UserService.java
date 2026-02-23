package rs.raf.cloud.raf_cloud_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import rs.raf.cloud.raf_cloud_backend.dto.CreateUserDto;
import rs.raf.cloud.raf_cloud_backend.dto.UpdateUserDto;
import rs.raf.cloud.raf_cloud_backend.dto.UserDto;
import rs.raf.cloud.raf_cloud_backend.model.Permission;
import rs.raf.cloud.raf_cloud_backend.model.User;
import rs.raf.cloud.raf_cloud_backend.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        Set<Permission> permissions = Optional.ofNullable(dto.getPermissions())
                .orElse(Collections.emptySet())
                .stream()
                .map(String::trim)
                .map(Permission::fromValue)
                .collect(Collectors.toSet());

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .passwordHash(encoder.encode(dto.getPassword()))
                .permissions(permissions)
                .build();

        return toDto(userRepository.save(user));
    }

    public UserDto updateUser(Long id, UpdateUserDto dto) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        else {
            User user = optionalUser.get();
            if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
            if (dto.getLastName() != null) user.setLastName(dto.getLastName());

            if (dto.getPermissions() != null) {
                Set<Permission> perms = dto.getPermissions().stream()
                        .map(String::trim)
                        .map(Permission::fromValue)
                        .collect(Collectors.toSet());

                user.setPermissions(perms);
            }

            return toDto(userRepository.save(user));
        }
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
                u.isAdmin()
        );
    }
}
