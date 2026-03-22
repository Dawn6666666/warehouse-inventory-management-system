package com.demo.wms.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件工具类。
 * 负责目录/文件创建、UTF-8 读写，以及项目自定义文本格式的转义与解析。
 */
public final class FileUtil {
    private FileUtil() {
    }

    public static void ensureDirectory(String directory) {
        ensureDirectory(Paths.get(directory));
    }

    public static void ensureDirectory(Path path) {
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("创建目录失败: " + path, ex);
        }
    }

    public static void ensureFile(String filePath) {
        ensureFile(Paths.get(filePath));
    }

    public static void ensureFile(Path path) {
        ensureFile(path, Collections.emptyList());
    }

    public static void ensureFile(Path path, List<String> initialLines) {
        Path parent = path.getParent();
        if (parent != null) {
            ensureDirectory(parent);
        }
        try {
            if (Files.notExists(path)) {
                Files.write(path, initialLines, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("创建文件失败: " + path, ex);
        }
    }

    public static List<String> readLines(String filePath) {
        return readAllLines(Paths.get(filePath));
    }

    public static List<String> readLines(Path path) {
        return readAllLines(path);
    }

    public static List<String> readAllLines(Path path) {
        try {
            ensureFile(path);
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("读取文件失败: " + path, ex);
        }
    }

    public static void writeLines(String filePath, List<String> lines) {
        writeAllLines(Paths.get(filePath), lines);
    }

    public static void writeLines(Path path, List<String> lines) {
        writeAllLines(path, lines);
    }

    public static void writeAllLines(Path path, List<String> lines) {
        ensureFile(path);
        try {
            // 覆盖写用于保存完整数据快照，确保文件内容与当前内存状态一致。
            Files.write(path, lines == null ? List.of() : lines, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException ex) {
            throw new IllegalStateException("写入文件失败: " + path, ex);
        }
    }

    public static void appendLine(Path path, String line) {
        appendLines(path, Collections.singletonList(line));
    }

    public static void appendLines(String filePath, Collection<String> lines) {
        appendLines(Paths.get(filePath), lines);
    }

    public static void appendLines(Path path, Collection<String> lines) {
        ensureFile(path);
        try {
            if (lines == null || lines.isEmpty()) {
                return;
            }
            Files.write(path, new ArrayList<>(lines), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new IllegalStateException("追加写入文件失败: " + path, ex);
        }
    }

    public static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        // 项目文件格式采用 key=value|key=value。
        // 因此字段值里如果出现分隔符，必须先转义后再写入文件。
        StringBuilder builder = new StringBuilder();
        for (char ch : raw.toCharArray()) {
            if (ch == '\\' || ch == '|' || ch == '=') {
                builder.append('\\');
            }
            if (ch == '\n' || ch == '\r') {
                builder.append(' ');
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    public static String escapeField(String raw) {
        return escape(raw);
    }

    public static String unescape(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }
        // 转义还原逻辑采用简单状态机：
        // 读到反斜杠后，把下一个字符按普通字符处理。
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (char ch : encoded.toCharArray()) {
            if (escaping) {
                builder.append(ch);
                escaping = false;
                continue;
            }
            if (ch == '\\') {
                escaping = true;
                continue;
            }
            builder.append(ch);
        }
        if (escaping) {
            builder.append('\\');
        }
        return builder.toString();
    }

    public static String unescapeField(String encoded) {
        return unescape(encoded);
    }

    public static List<String> splitEscaped(String text, char delimiter) {
        List<String> parts = new ArrayList<>();
        if (text == null) {
            return parts;
        }
        StringBuilder current = new StringBuilder();
        boolean escaping = false;
        // 不能直接使用 String.split，因为被转义的分隔符不应该参与切分。
        for (char ch : text.toCharArray()) {
            if (ch == delimiter && !escaping) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
            if (escaping) {
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            }
        }
        parts.add(current.toString());
        return parts;
    }

    public static int findUnescapedChar(String text, char target) {
        boolean escaping = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            // 只有未被转义的目标字符，才真正具有“分隔符”含义。
            if (ch == target && !escaping) {
                return i;
            }
            if (escaping) {
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            }
        }
        return -1;
    }

    public static Map<String, String> parseKeyValueLine(String line) {
        Map<String, String> result = new LinkedHashMap<>();
        if (line == null || line.trim().isEmpty()) {
            return result;
        }
        // 一行文本会被解析成“字段名 -> 字段值”的结构，供持久化层恢复实体对象使用。
        for (String part : splitEscaped(line, '|')) {
            if (part.isBlank()) {
                continue;
            }
            int index = findUnescapedChar(part, '=');
            if (index < 0) {
                continue;
            }
            String key = unescapeField(part.substring(0, index).trim());
            String value = unescapeField(part.substring(index + 1));
            result.put(key, value);
        }
        return result;
    }

    public static String buildKeyValueLine(Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (!first) {
                builder.append('|');
            }
            first = false;
            // 写出时统一做转义，保证字段值中出现特殊字符时仍可被正确解析回来。
            builder.append(escapeField(entry.getKey()));
            builder.append('=');
            builder.append(escapeField(entry.getValue()));
        }
        return builder.toString();
    }
}
