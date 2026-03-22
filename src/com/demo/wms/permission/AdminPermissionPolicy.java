package com.demo.wms.permission;

/**
 * 管理员权限策略。
 * 管理员拥有系统中的全部功能权限。
 * <p>
 * 该实现用于体现“完整权限角色”的能力边界，是权限策略中的全开放版本。
 */
public class AdminPermissionPolicy implements PermissionPolicy {
    // 管理员是“全权限角色”，这样和操作员的差异最直观。
    @Override
    public boolean canManageUser() {
        return true;
    }

    @Override
    public boolean canManageProduct() {
        return true;
    }

    @Override
    public boolean canStockIn() {
        return true;
    }

    @Override
    public boolean canStockOut() {
        return true;
    }

    @Override
    public boolean canViewInventory() {
        return true;
    }

    @Override
    public boolean canViewStockRecords() {
        return true;
    }

    @Override
    public boolean canViewLogs() {
        return true;
    }
}
