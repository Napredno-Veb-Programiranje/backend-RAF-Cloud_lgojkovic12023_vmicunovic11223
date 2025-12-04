package rs.raf.cloud.raf_cloud_backend.security;

import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    public void set(CurrentUser user) {
        CURRENT.set(user);
    }

    public CurrentUser get() {
        return CURRENT.get();
    }

    public void clear() {
        CURRENT.remove();
    }
}
