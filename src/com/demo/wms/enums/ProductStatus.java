package com.demo.wms.enums;

/**
 * 商品状态枚举。
 * 停用商品仍可查询，但不能继续参与入库和出库。
 * <p>
 * 常量说明：
 * <p>
 * - ACTIVE：启用状态，表示商品可以正常参与库存业务。
 * <p>
 * - DISABLED：停用状态，表示商品保留历史数据，但不再继续流转。
 * <p>
 * 字段说明：
 * <p>
 * - displayName：状态中文名称，用于列表展示。
 */
public enum ProductStatus {
    ACTIVE("启用"),
    DISABLED("停用");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ProductStatus fromString(String text) {
        for (ProductStatus status : values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知商品状态：" + text);
    }
}
