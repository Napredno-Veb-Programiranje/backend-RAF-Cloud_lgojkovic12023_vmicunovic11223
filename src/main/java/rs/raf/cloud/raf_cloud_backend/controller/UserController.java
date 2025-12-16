package rs.raf.cloud.raf_cloud_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.raf.cloud.raf_cloud_backend.dto.CreateUserDto;
import rs.raf.cloud.raf_cloud_backend.dto.UpdateUserDto;
import rs.raf.cloud.raf_cloud_backend.dto.UserDto;
import rs.raf.cloud.raf_cloud_backend.model.Permission;
import rs.raf.cloud.raf_cloud_backend.security.aop.RequiresPermission;
import rs.raf.cloud.raf_cloud_backend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    @GetMapping
    @RequiresPermission({Permission.USER_READ})
    public List<UserDto> getAll() {
        return userService.getAllUsers();
    }

    @PostMapping
    @RequiresPermission({Permission.USER_CREATE})
    public UserDto create(@RequestBody CreateUserDto dto) {
        return userService.createUser(dto);
    }

    @PutMapping("/{id}")
    @RequiresPermission({Permission.USER_UPDATE})
    public UserDto update(@PathVariable Long id,
                          @RequestBody UpdateUserDto dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission({Permission.USER_DELETE})
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
