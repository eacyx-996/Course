package com.example.spring.common;

/**
 * 当前登录用户上下文(由 AuthInterceptor 写入)
 */
public class UserContext {

    public record LoginUser(Long userId, String role) {
        public boolean isAdmin() {
            return "admin".equals(role);
        }

        public boolean isTeacherOrAdmin() {
            return isAdmin() || "teacher".equals(role);
        }
    }

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser u = get();
        return u == null ? null : u.userId();
    }

    public static void requireRole(boolean allowed) {
        if (!allowed) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    public static void clear() {
        HOLDER.remove();
    }
}
