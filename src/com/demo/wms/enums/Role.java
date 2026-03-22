package com.demo.wms.enums;

/**
 * 系统角色枚举。
 * 管理员负责主数据维护，操作员主要参与日常库存操作。
 * <p>
 * 常量说明：
 * <p>
 * - ADMIN：管理员，拥有商品管理、库存管理、用户管理、日志查看等完整权限。
 * <p>
 * - OPERATOR：操作员，主要负责入库、出库和记录查询等日常业务操作。
 * <p>
 * 字段说明：
 * <p>
 * - displayName：角色中文名称，用于控制台界面展示。
 */
public enum Role {
    ADMIN("管理员"),
    OPERATOR("操作员");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String text) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知角色：" + text);
    }
}
