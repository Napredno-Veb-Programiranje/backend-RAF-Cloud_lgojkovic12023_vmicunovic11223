package rs.raf.cloud.raf_cloud_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.raf.cloud.raf_cloud_backend.model.Permission;
import rs.raf.cloud.raf_cloud_backend.security.aop.RequiresPermission;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/user-read")
    @RequiresPermission({Permission.USER_READ})
    public String userRead() {
        return "OK: user-read allowed";
    }

    @GetMapping("/machine-destroy")
    @RequiresPermission({Permission.MACHINE_DESTROY})
    public String machineDestroy() {
        return "OK: machine-destroy allowed";
    }
}
