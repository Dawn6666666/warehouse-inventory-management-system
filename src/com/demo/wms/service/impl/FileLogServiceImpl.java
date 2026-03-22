package com.demo.wms.service.impl;

import com.demo.wms.service.LogService;
import com.demo.wms.util.FileUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 文件日志服务。
 * 采用追加写入的方式，把关键操作保留到项目根目录下的日志文件中。
 */
public class FileLogServiceImpl implements LogService {

    public FileLogServiceImpl() {
        ServiceSupport.ensureLayout();
    }

    @Override
    public void info(String operator, String module, String action, String result) {
        append("INFO", operator, module, action, result);
    }

    @Override
    public void warn(String operator, String module, String action, String reason) {
        append("WARN", operator, module, action, reason);
    }

    @Override
    public void error(String operator, String module, String action, String reason) {
        append("ERROR", operator, module, action, reason);
    }

    @Override
    public List<String> readLogs() {
        // 控制层展示日志时只关心“逐行可读”，这里直接返回原始文本更简单。
        return FileUtil.readLines(ServiceSupport.SYSTEM_LOG_FILE);
    }

    private void append(String level, String operator, String module, String action, String detail) {
        ServiceSupport.ensureLayout();
        // 日志格式保持纯文本，便于直接打开文件观察系统行为。
        String line = String.join(" | ",
                FileUtil.escapeField(ServiceSupport.formatTime(LocalDateTime.now())),
                FileUtil.escapeField(operator == null ? "system" : operator.trim()),
                FileUtil.escapeField(module),
                FileUtil.escapeField(action),
                FileUtil.escapeField(level),
                FileUtil.escapeField(detail));
        FileUtil.appendLines(ServiceSupport.SYSTEM_LOG_FILE, Collections.singletonList(line));
    }
}
