package rs.raf.cloud.raf_cloud_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.cloud.raf_cloud_backend.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
