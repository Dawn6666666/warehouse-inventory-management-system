package com.demo.wms.enums;

/**
 * 用户状态枚举。
 * 用来区分账号当前是否允许登录系统。
 * <p>
 * 常量说明：
 * <p>
 * - ACTIVE：启用状态，表示该账号可以正常登录。
 * <p>
 * - DISABLED：禁用状态，表示该账号暂时不可登录。
 * <p>
 * 字段说明：
 * <p>
 * - displayName：状态中文名称，用于界面展示。
 */
public enum UserStatus {
    ACTIVE("启用"),
    DISABLED("禁用");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserStatus fromString(String text) {
        for (UserStatus status : values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知用户状态：" + text);
    }
}
