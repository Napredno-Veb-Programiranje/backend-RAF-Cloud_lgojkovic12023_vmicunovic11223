package rs.raf.cloud.raf_cloud_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.raf.cloud.raf_cloud_backend.dto.MachineDto;
import rs.raf.cloud.raf_cloud_backend.dto.MachineEventDto;
import rs.raf.cloud.raf_cloud_backend.dto.MachineSearchDto;
import rs.raf.cloud.raf_cloud_backend.model.MachineOperationType;
import rs.raf.cloud.raf_cloud_backend.model.Permission;
import rs.raf.cloud.raf_cloud_backend.security.aop.RequiresPermission;
import rs.raf.cloud.raf_cloud_backend.service.MachineService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/machines")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MachineController {

    private final MachineService machineService;

    @GetMapping
    @RequiresPermission({Permission.MACHINE_SEARCH})
    public List<MachineDto> list() {
        return machineService.list();
    }

    //search by name: /machines/search?q=abc
    @PostMapping("/search")
    @RequiresPermission({Permission.MACHINE_SEARCH})
    public List<MachineDto> search(@RequestBody MachineSearchDto dto) {
        return machineService.search(dto);
    }

    //create: ---/machines?name=m1--
    @PostMapping
    @RequiresPermission({Permission.MACHINE_CREATE})
    public MachineDto create(@RequestParam String name) {
        return machineService.create(name);
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Void> schedule(@PathVariable Long id, @RequestParam MachineOperationType operation, @RequestParam Instant when) {
        machineService.scheduleOperation(id, operation, when);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/start")
    @RequiresPermission({Permission.MACHINE_START})
    public void start(@PathVariable Long id) {
        machineService.start(id);
    }

    @PostMapping("/{id}/stop")
    @RequiresPermission({Permission.MACHINE_STOP})
    public void stop(@PathVariable Long id) {
        machineService.stop(id);
    }

    @PostMapping("/{id}/restart")
    @RequiresPermission({Permission.MACHINE_RESTART})
    public void restart(@PathVariable Long id) {
        machineService.restart(id);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission({Permission.MACHINE_DESTROY})
    public void destroy(@PathVariable Long id) {
        machineService.destroy(id);
    }
    @GetMapping("/errors")
    @RequiresPermission({Permission.MACHINE_SEARCH})
    public List<MachineEventDto> errors() {
        return machineService.allErrors();
    }


    @GetMapping("/{id}/history")
    @RequiresPermission({Permission.MACHINE_SEARCH})
    public List<MachineEventDto> history(@PathVariable Long id) {
        return machineService.history(id);
    }
}
