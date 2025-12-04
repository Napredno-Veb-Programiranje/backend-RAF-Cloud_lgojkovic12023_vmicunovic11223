package rs.raf.cloud.raf_cloud_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rs.raf.cloud.raf_cloud_backend.dto.LoginRequestDto;
import rs.raf.cloud.raf_cloud_backend.dto.LoginResponseDto;
import rs.raf.cloud.raf_cloud_backend.dto.UserDto;
import rs.raf.cloud.raf_cloud_backend.model.User;
import rs.raf.cloud.raf_cloud_backend.repository.UserRepository;
import rs.raf.cloud.raf_cloud_backend.security.JwtUtil;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200") //Ang
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null || !encoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);

        UserDto userDto = new UserDto(
                user.getUserID(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPermissions().stream()
                        .map(p -> p.getValue())
                        .collect(Collectors.toList()),
                true
        );

        return ResponseEntity.ok(new LoginResponseDto(token, userDto));
    }
}
