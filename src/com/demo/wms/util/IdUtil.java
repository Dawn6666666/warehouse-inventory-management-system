package com.demo.wms.util;

import java.util.Collection;

/**
 * 编号生成工具。
 * 按“前缀 + 固定位数数字”的规则生成下一个业务编号，例如 P001、U002、R010。
 */
public final class IdUtil {
    private IdUtil() {
    }

    public static String nextId(String prefix, Collection<String> existingIds) {
        return nextId(existingIds, prefix, 3);
    }

    public static String nextId(Collection<String> existingIds, String prefix) {
        return nextId(existingIds, prefix, 3);
    }

    public static String nextId(String prefix, Collection<String> existingIds, int width) {
        return nextId(existingIds, prefix, width);
    }

    public static String nextId(Collection<String> existingIds, String prefix, int width) {
        int max = 0;
        if (existingIds != null) {
            for (String id : existingIds) {
                if (id == null || !id.startsWith(prefix)) {
                    continue;
                }
                String numberPart = id.substring(prefix.length());
                try {
                    max = Math.max(max, Integer.parseInt(numberPart));
                } catch (NumberFormatException ignored) {
                    // 非标准编号不参与比较，避免脏数据影响正常编号递增。
                }
            }
        }
        // 这种编号方案便于人工阅读，也方便在文本文件中直接排查数据。
        return prefix + String.format("%0" + Math.max(width, 1) + "d", max + 1);
    }
}
