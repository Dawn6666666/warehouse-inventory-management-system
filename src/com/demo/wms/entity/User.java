package com.demo.wms.entity;

import com.demo.wms.enums.Role;
import com.demo.wms.enums.UserStatus;

/**
 * 用户实体。
 * 表示系统中的登录账号、角色和启用状态。
 * <p>
 * 字段说明：
 * <p>
 * - id：用户编号，作为用户数据的唯一标识。
 * <p>
 * - username：登录用户名，用于身份识别和登录校验。
 * <p>
 * - password：登录密码。
 * <p>
 * - role：用户角色，用于区分管理员和操作员。
 * <p>
 * - status：账号状态，用于表示当前账号是否可登录。
 */
public class User {
    private String id;
    private String username;
    private String password;
    private Role role;
    private UserStatus status;

    public User() {
    }

    public User(String id, String username, String password, Role role, UserStatus status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public User(User other) {
        this(other.id, other.username, other.password, other.role, other.status);
    }

    public User copy() {
        // 通过复制对象隔离共享数据，避免控制层直接持有仓库中的原始对象。
        return new User(this);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
