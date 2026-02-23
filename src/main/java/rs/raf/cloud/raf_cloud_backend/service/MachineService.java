package rs.raf.cloud.raf_cloud_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import rs.raf.cloud.raf_cloud_backend.dto.MachineDto;
import rs.raf.cloud.raf_cloud_backend.dto.MachineEventDto;
import rs.raf.cloud.raf_cloud_backend.dto.MachineSearchDto;
import rs.raf.cloud.raf_cloud_backend.model.*;
import rs.raf.cloud.raf_cloud_backend.repository.MachineEventRepository;
import rs.raf.cloud.raf_cloud_backend.repository.MachineRepository;
import rs.raf.cloud.raf_cloud_backend.repository.UserRepository;
import rs.raf.cloud.raf_cloud_backend.security.CurrentUser;
import rs.raf.cloud.raf_cloud_backend.security.CurrentUserProvider;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;
    private final MachineEventRepository eventRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();

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

        if (m.getStatus() != MachineStatus.STOPPED) {
            throw new RuntimeException("Machine must be STOPPED");
        }

        m.setStatus(MachineStatus.IN_PROGRESS);
        machineRepository.save(m);

        executorService.submit(() -> {
            try {
                sleep(10_000 + random.nextInt(6_000));

                m.setStatus(MachineStatus.RUNNING);
                machineRepository.save(m);

                log(m, "START_DONE", "Machine started");
            } catch (Exception e) {
                m.setStatus(MachineStatus.ERROR);
                machineRepository.save(m);
                log(m, "ERROR", e.getMessage());
            }
        });
    }


    public void stop(Long id) {
        Machine m = getMachine(id);

        if (m.getStatus() != MachineStatus.RUNNING) {
            throw new RuntimeException("Machine must be RUNNING");
        }

        m.setStatus(MachineStatus.IN_PROGRESS);
        machineRepository.save(m);

        executorService.submit(() -> {
            try {
                sleep(10_000 + random.nextInt(6_000));
                m.setStatus(MachineStatus.STOPPED);
                machineRepository.save(m);
                log(m, "STOP_DONE", "Machine stopped");
            } catch (Exception e) {
                m.setStatus(MachineStatus.ERROR);
                machineRepository.save(m);
                log(m, "ERROR", e.getMessage());
            }
        });
    }

    public void restart(Long id) {
        Machine m = getMachine(id);

        if (m.getStatus() != MachineStatus.RUNNING) {
            error(m, "Machine not running");
            return;
        }

        // opcionalno: zabrani ako je vec u toku neka operacija
        if (m.getStatus() == MachineStatus.IN_PROGRESS) {
            error(m, "Machine busy");
            return;
        }

        m.setStatus(MachineStatus.IN_PROGRESS);
        machineRepository.save(m);

        executorService.submit(() -> {
            try {
                sleep(2_000 + random.nextInt(6_000));

                Machine m1 = getMachine(id);
                m1.setStatus(MachineStatus.STOPPED);
                machineRepository.save(m1);

                sleep(2_000 + random.nextInt(6_000));

                Machine m2 = getMachine(id);
                m2.setStatus(MachineStatus.IN_PROGRESS);
                machineRepository.save(m2);

                sleep(2_000 + random.nextInt(6_000));

                Machine m3 = getMachine(id);
                m3.setStatus(MachineStatus.RUNNING);
                machineRepository.save(m3);

                log(m3, "RESTART_DONE", "Machine restarted");
            } catch (Exception e) {
                Machine mx = null;
                try {
                    mx = getMachine(id);
                    mx.setStatus(MachineStatus.ERROR);
                    machineRepository.save(mx);
                    log(mx, "ERROR", e.getMessage());
                } catch (Exception ignored) {
                    log(m, "ERROR", e.getMessage());
                }
            }
        });
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

    public void scheduleOperation(Long id, MachineOperationType operationType, Instant when) {
        long delayMs = Duration.between(Instant.now(), when).toMillis();

        scheduledExecutorService.schedule(() -> {
           try {
               switch (operationType) {
                   case POWER_ON -> start(id);
                   case POWER_OFF -> stop(id);
                   case RESTART -> restart(id);
               }
           } catch (Exception ex) {
               Machine m = getMachine(id);
               if (m != null) {
                   log(m, "ERROR", "Scheduled " + operationType + " failed: " + ex.getMessage());
               }
           }
        }, Math.max(delayMs, 0), TimeUnit.MILLISECONDS);
    }

    public List<MachineDto> search(MachineSearchDto dto) {
        Specification<Machine> spec = Specification.where(null);

        if (dto.getMachineName() != null && !dto.getMachineName().isBlank()) {
            String q = dto.getMachineName().trim().toLowerCase();
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + q + "%")
            );
        }

        if (dto.getStates() != null && !dto.getStates().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("status").in(dto.getStates())
            );
        }

        if (dto.getStartDate() != null) {
            LocalDateTime start = dto.getStartDate().atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), start)
            );
        }

        if (dto.getEndDate() != null) {
            LocalDateTime end = dto.getEndDate().plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("createdAt"), end)
            );
        }

        return machineRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toDto)
                .toList();
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
