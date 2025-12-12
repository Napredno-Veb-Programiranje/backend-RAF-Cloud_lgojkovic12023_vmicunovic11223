package rs.raf.cloud.raf_cloud_backend.security.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import rs.raf.cloud.raf_cloud_backend.model.Permission;
import rs.raf.cloud.raf_cloud_backend.security.CurrentUser;
import rs.raf.cloud.raf_cloud_backend.security.CurrentUserProvider;

import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
public class PermissionAspect {

    private final CurrentUserProvider currentUserProvider;

    public PermissionAspect(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Around("@annotation(requiresPermission)")
    public Object checkMethodPermission(ProceedingJoinPoint pjp,
                                        RequiresPermission requiresPermission) throws Throwable {
        enforce(requiresPermission.value());
        return pjp.proceed();
    }

    @Around("@within(requiresPermission)")
    public Object checkClassPermission(ProceedingJoinPoint pjp,
                                       RequiresPermission requiresPermission) throws Throwable {
        enforce(requiresPermission.value());
        return pjp.proceed();
    }

    private void enforce(Permission[] required) {
        CurrentUser cu = currentUserProvider.get();
        System.out.println("AOP HIT: cu=" + (cu == null ? "null" : cu.getEmail())
                + ", required=" + Arrays.toString(required));
        System.out.println("check: user=" + cu.getEmail() + ", required=" + Arrays.toString(required));

        if (cu == null) {
            throw new ForbiddenException("Not authenticated");
        }

        Set<Permission> userPerms = cu.getPermissions();
        boolean allowed = Arrays.stream(required).anyMatch(userPerms::contains);

        if (!allowed) {
            throw new ForbiddenException("Missing permission: " + Arrays.toString(required));
        }


    }
}
