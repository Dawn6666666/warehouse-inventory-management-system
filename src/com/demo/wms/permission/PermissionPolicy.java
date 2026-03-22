package com.demo.wms.permission;

/**
 * 权限策略接口。
 * 用统一的方法描述“某个角色能做什么”，便于通过多态切换不同实现。
 * <p>
 * 该接口的意义在于把“角色判断”从控制层中抽离出来。
 * 控制层只关心当前能力是否开放，而不需要直接写大量角色分支。
 */
public interface PermissionPolicy {
    /**
     * 是否允许管理用户。
     *
     * @return {@code true} 表示允许进入用户管理模块
     */
    boolean canManageUser();

    /**
     * 是否允许管理商品主数据。
     *
     * @return {@code true} 表示允许新增、修改、删除或停用商品
     */
    boolean canManageProduct();

    /**
     * 是否允许执行入库操作。
     *
     * @return {@code true} 表示可以增加商品库存
     */
    boolean canStockIn();

    /**
     * 是否允许执行出库操作。
     *
     * @return {@code true} 表示可以减少商品库存
     */
    boolean canStockOut();

    /**
     * 是否允许查看库存信息。
     *
     * @return {@code true} 表示允许查看库存列表和低库存预警
     */
    boolean canViewInventory();

    /**
     * 是否允许查看进出库记录。
     *
     * @return {@code true} 表示允许查询库存流水
     */
    boolean canViewStockRecords();

    /**
     * 是否允许查看系统日志。
     *
     * @return {@code true} 表示允许进入日志查看功能
     */
    boolean canViewLogs();
}
