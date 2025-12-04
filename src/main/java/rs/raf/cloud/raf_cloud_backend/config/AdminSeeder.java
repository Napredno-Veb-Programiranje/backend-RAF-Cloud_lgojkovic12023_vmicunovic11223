package rs.raf.cloud.raf_cloud_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import rs.raf.cloud.raf_cloud_backend.model.Permission;
import rs.raf.cloud.raf_cloud_backend.model.User;
import rs.raf.cloud.raf_cloud_backend.repository.UserRepository;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("admin@cloud.com").isPresent()) {
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@cloud.com")
                .passwordHash(encoder.encode("admin"))
                .permissions(Set.of(Permission.values()))
                .active(true)
                .build();

        userRepository.save(admin);
        System.out.println(">>> ADMIN USER CREATED (admin@cloud.com / admin)");
    }
}
