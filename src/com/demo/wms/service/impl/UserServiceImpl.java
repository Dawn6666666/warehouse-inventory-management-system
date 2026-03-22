package com.demo.wms.service.impl;

import com.demo.wms.entity.User;
import com.demo.wms.enums.Role;
import com.demo.wms.enums.UserStatus;
import com.demo.wms.exception.BizException;
import com.demo.wms.service.PersistenceService;
import com.demo.wms.service.UserService;
import com.demo.wms.store.WmsDataStore;
import com.demo.wms.util.IdUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现。
 * 负责用户新增、角色调整、状态变更以及管理员底线规则的维护。
 */
public class UserServiceImpl implements UserService {
    private final WmsDataStore dataStore;
    private final PersistenceService persistenceService;

    public UserServiceImpl() {
        this(ServiceSupport.createStore());
    }

    public UserServiceImpl(WmsDataStore dataStore) {
        this(dataStore, new FilePersistenceServiceImpl(dataStore, new FileLogServiceImpl()));
    }

    public UserServiceImpl(WmsDataStore dataStore, PersistenceService persistenceService) {
        this.dataStore = dataStore;
        this.persistenceService = persistenceService;
    }

    @Override
    public User addUser(String username, String password, Role role) {
        validateUsername(username);
        validatePassword(password);
        if (role == null) {
            throw new BizException("用户角色不能为空。");
        }
        if (dataStore.getUsersByUsername().containsKey(username.trim())) {
            throw new BizException("用户名已存在：" + username);
        }

        Map<String, User> snapshot = dataStore.snapshotUsers();
        User user = new User(
                IdUtil.nextId(dataStore.getUsersByUsername().values().stream().map(User::getId).toList(), "U"),
                username.trim(),
                password.trim(),
                role,
                UserStatus.ACTIVE
        );
        dataStore.getUsersByUsername().put(user.getUsername(), user);
        try {
            persistenceService.saveUsers();
            return user.copy();
        } catch (BizException ex) {
            dataStore.replaceUsers(snapshot);
            throw ex;
        }
    }

    @Override
    public void changePassword(String username, String newPassword) {
        User user = requireUser(username);
        validatePassword(newPassword);
        Map<String, User> snapshot = dataStore.snapshotUsers();
        user.setPassword(newPassword.trim());
        try {
            persistenceService.saveUsers();
        } catch (BizException ex) {
            dataStore.replaceUsers(snapshot);
            throw ex;
        }
    }

    @Override
    public void changeRole(String username, Role role) {
        User user = requireUser(username);
        if (role == null) {
            throw new BizException("目标角色不能为空。");
        }
        // 如果当前用户是系统中最后一个启用管理员，就不能直接降级为普通操作员。
        if (user.getRole() == Role.ADMIN && role != Role.ADMIN && user.isActive() && countActiveAdmins() <= 1) {
            throw new BizException("系统至少需要保留 1 个启用状态的管理员。");
        }
        Map<String, User> snapshot = dataStore.snapshotUsers();
        user.setRole(role);
        try {
            persistenceService.saveUsers();
        } catch (BizException ex) {
            dataStore.replaceUsers(snapshot);
            throw ex;
        }
    }

    @Override
    public void changeStatus(String username, UserStatus status, String currentOperator) {
        User user = requireUser(username);
        if (status == null) {
            throw new BizException("目标状态不能为空。");
        }
        if (currentOperator != null && currentOperator.trim().equals(user.getUsername()) && status == UserStatus.DISABLED) {
            // 当前登录账号不允许把自己禁用掉，否则当前会话和文件数据会立刻出现矛盾。
            throw new BizException("不允许禁用当前登录账号自己。");
        }
        if (user.getRole() == Role.ADMIN
                && user.isActive()
                && status == UserStatus.DISABLED
                && countActiveAdmins() <= 1) {
            // 至少保留一个启用管理员，这是系统可维护性的最低保障。
            throw new BizException("系统至少需要保留 1 个启用状态的管理员。");
        }
        Map<String, User> snapshot = dataStore.snapshotUsers();
        user.setStatus(status);
        try {
            persistenceService.saveUsers();
        } catch (BizException ex) {
            dataStore.replaceUsers(snapshot);
            throw ex;
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        for (User user : dataStore.getUsersByUsername().values()) {
            result.add(user.copy());
        }
        result.sort(Comparator.comparing(User::getUsername));
        return result;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = dataStore.getUsersByUsername().get(username);
        return user == null ? null : user.copy();
    }

    private User requireUser(String username) {
        User user = dataStore.getUsersByUsername().get(username);
        if (user == null) {
            throw new BizException("用户不存在：" + username);
        }
        return user;
    }

    private void validateUsername(String username) {
        if (username == null || !username.trim().matches("[A-Za-z0-9_]{3,20}")) {
            throw new BizException("用户名必须为 3~20 位字母、数字或下划线。");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().length() < 6 || password.trim().length() > 20) {
            throw new BizException("密码长度必须为 6~20 位。");
        }
    }

    private int countActiveAdmins() {
        int count = 0;
        for (User user : dataStore.getUsersByUsername().values()) {
            if (user.getRole() == Role.ADMIN && user.getStatus() == UserStatus.ACTIVE) {
                count++;
            }
        }
        return count;
    }
}
