package com.demo.wms.permission;

/**
 * 操作员权限策略。
 * 操作员只参与库存相关业务，不负责商品主数据和用户维护。
 * <p>
 * 该实现用于体现“受限权限角色”的能力边界，只保留日常库存操作所需权限。
 */
public class OperatorPermissionPolicy implements PermissionPolicy {
    // 操作员保留库存相关能力，但不开放用户和商品维护，体现同接口下的差异实现。
    @Override
    public boolean canManageUser() {
        return false;
    }

    @Override
    public boolean canManageProduct() {
        return false;
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
        return false;
    }
}
