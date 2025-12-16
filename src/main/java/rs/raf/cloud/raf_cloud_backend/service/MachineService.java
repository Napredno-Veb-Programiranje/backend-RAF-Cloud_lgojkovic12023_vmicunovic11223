package rs.raf.cloud.raf_cloud_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.raf.cloud.raf_cloud_backend.dto.MachineDto;
import rs.raf.cloud.raf_cloud_backend.dto.MachineEventDto;
import rs.raf.cloud.raf_cloud_backend.model.*;
import rs.raf.cloud.raf_cloud_backend.repository.MachineEventRepository;
import rs.raf.cloud.raf_cloud_backend.repository.MachineRepository;
import rs.raf.cloud.raf_cloud_backend.repository.UserRepository;
import rs.raf.cloud.raf_cloud_backend.security.CurrentUser;
import rs.raf.cloud.raf_cloud_backend.security.CurrentUserProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;
    private final MachineEventRepository eventRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    public MachineDto create(String name) {
        CurrentUser cu = currentUserProvider.get();

        User owner = userRepository.findById(cu.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Machine m = Machine.builder()
                .name(name)
                .status(MachineStatus.STOPPED)
                .createdAt(LocalDateTime.now())
                .owner(owner)
                .build();

        machineRepository.save(m);
        log(m, "CREATE", "Machine created");
        return toDto(m);
    }

    public void start(Long id) {
        Machine m = getMachine(id);

        if (m.getStatus() == MachineStatus.RUNNING) {
            error(m, "Machine already running");
            return;
        }

        m.setStatus(MachineStatus.RUNNING);
        machineRepository.save(m);
        log(m, "START", "Machine started");
    }

    public void stop(Long id) {
        Machine m = getMachine(id);

        if (m.getStatus() == MachineStatus.STOPPED) {
            error(m, "Machine already stopped");
            return;
        }

        m.setStatus(MachineStatus.STOPPED);
        machineRepository.save(m);
        log(m, "STOP", "Machine stopped");
    }

    public void restart(Long id) {
        Machine m = getMachine(id);

        if (m.getStatus() != MachineStatus.RUNNING) {
            error(m, "Machine not running");
            return;
        }

        log(m, "RESTART", "Machine restarting");
        m.setStatus(MachineStatus.RUNNING);
        machineRepository.save(m);
    }

    public void destroy(Long id) {
        Machine m = getMachine(id);

        // pravilo: ne brisi ako je RUNNING
        if (m.getStatus() == MachineStatus.RUNNING) {
            error(m, "Cannot destroy while RUNNING");
            return;
        }

        log(m, "DESTROY", "Machine destroyed");
        machineRepository.delete(m);
    }

    public List<MachineDto> list() {
        return machineRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<MachineDto> searchByName(String q) {
        String query = q == null ? "" : q.trim().toLowerCase();
        return machineRepository.findAll()
                .stream()
                .filter(m -> m.getName() != null && m.getName().toLowerCase().contains(query))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<MachineEventDto> history(Long machineId) {
        return eventRepository.findByMachineId(machineId)
                .stream()
                .map(e -> new MachineEventDto(
                        e.getId(),
                        e.getAction(),
                        e.getMessage(),
                        String.valueOf(e.getTimestamp())
                ))
                .collect(Collectors.toList());
    }

    private Machine getMachine(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found"));
    }

    public List<MachineEventDto> allErrors() {
        CurrentUser cu = currentUserProvider.get();
        if (cu == null) throw new RuntimeException("Not authenticated");

        boolean isAdmin = cu.getPermissions().containsAll(List.of(Permission.values()));

        List<MachineEvent> events = isAdmin
                ? eventRepository.findByActionOrderByTimestampDesc("ERROR")
                : eventRepository.findErrorsForOwner(cu.getId());

        return events.stream()
                .map(e -> new MachineEventDto(
                        e.getId(),
                        e.getAction(),
                        e.getMessage(),
                        String.valueOf(e.getTimestamp())
                ))
                .toList();
    }

    private void log(Machine m, String action, String msg) {
        eventRepository.save(
                MachineEvent.builder()
                        .machine(m)
                        .action(action)
                        .message(msg)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    private void error(Machine m, String msg) {
        m.setStatus(MachineStatus.ERROR);
        machineRepository.save(m);
        log(m, "ERROR", msg);
    }

    private MachineDto toDto(Machine m) {
        return new MachineDto(
                m.getId(),
                m.getName(),
                String.valueOf(m.getStatus()),
                m.getOwner() != null ? m.getOwner().getEmail() : null,
                String.valueOf(m.getCreatedAt())
        );
    }
}
