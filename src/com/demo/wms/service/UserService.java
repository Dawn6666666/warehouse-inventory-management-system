package com.demo.wms.service;

import com.demo.wms.entity.User;
import com.demo.wms.enums.Role;
import com.demo.wms.enums.UserStatus;

import java.util.List;

/**
 * 用户服务接口。
 * 负责账号新增、角色调整、密码修改和启停用管理。
 */
public interface UserService {
    User addUser(String username, String password, Role role);

    void changePassword(String username, String newPassword);

    void changeRole(String username, Role role);

    void changeStatus(String username, UserStatus status, String currentOperator);

    List<User> getAllUsers();

    User getUserByUsername(String username);
}
