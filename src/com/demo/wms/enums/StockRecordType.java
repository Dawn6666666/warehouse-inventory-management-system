package com.demo.wms.enums;

/**
 * 库存记录类型枚举。
 * 用来区分一条流水是入库记录还是出库记录。
 * <p>
 * 常量说明：
 * <p>
 * - IN：入库记录，表示库存增加。
 * <p>
 * - OUT：出库记录，表示库存减少。
 * <p>
 * 字段说明：
 * <p>
 * - displayName：记录类型中文名称，用于界面展示。
 */
public enum StockRecordType {
    IN("入库"),
    OUT("出库");

    private final String displayName;

    StockRecordType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static StockRecordType fromString(String text) {
        for (StockRecordType type : values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知记录类型：" + text);
    }
}
