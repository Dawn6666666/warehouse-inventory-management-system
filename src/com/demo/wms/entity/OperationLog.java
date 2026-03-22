package com.demo.wms.entity;

import com.demo.wms.enums.LogLevel;

import java.time.LocalDateTime;

/**
 * 操作日志实体。
 * 用来描述一条日志记录的结构，便于统一组织日志信息。
 * <p>
 * 字段说明：
 * <p>
 * - operateTime：日志产生时间。
 * <p>
 * - operator：操作人，可以是具体用户名，也可以是系统标识。
 * <p>
 * - module：所属模块，例如认证、商品、库存等。
 * <p>
 * - action：具体动作，例如登录、入库、出库、删除。
 * <p>
 * - level：日志级别，例如信息、告警、错误。
 * <p>
 * - detail：日志详细内容，用于补充本次操作结果。
 */
public class OperationLog {
    private LocalDateTime operateTime;
    private String operator;
    private String module;
    private String action;
    private LogLevel level;
    private String detail;

    public OperationLog(LocalDateTime operateTime, String operator, String module,
                        String action, LogLevel level, String detail) {
        this.operateTime = operateTime;
        this.operator = operator;
        this.module = module;
        this.action = action;
        this.level = level;
        this.detail = detail;
    }

    public LocalDateTime getOperateTime() {
        return operateTime;
    }

    public String getOperator() {
        return operator;
    }

    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getDetail() {
        return detail;
    }
}
