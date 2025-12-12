package rs.raf.cloud.raf_cloud_backend.security.aop;

import rs.raf.cloud.raf_cloud_backend.model.Permission;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    Permission[] value();
}
