package com.demo.wms.enums;

/**
 * 日志级别枚举。
 * 对应正常信息、业务警告和系统错误三类日志。
 * <p>
 * 常量说明：
 * <p>
 * - INFO：信息级别，用于记录正常完成的操作。
 * <p>
 * - WARN：告警级别，用于记录可预期但不影响系统继续运行的问题。
 * <p>
 * - ERROR：错误级别，用于记录操作失败或系统异常。
 */
public enum LogLevel {
    INFO,
    WARN,
    ERROR
}
