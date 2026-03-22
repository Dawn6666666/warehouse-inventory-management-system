package com.demo.wms.exception;

/**
 * 权限异常。
 * 当调用方尝试执行越权操作时使用。
 * <p>
 * 例如操作员尝试进入用户管理、日志查看等仅管理员可执行的功能时，
 * 可以使用该异常表达“当前角色不具备对应权限”。
 */
public class PermissionDeniedException extends BizException {
    public PermissionDeniedException(String message) {
        super(message);
    }
}
