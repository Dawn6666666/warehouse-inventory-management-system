package com.demo.wms.exception;

/**
 * 业务异常基类。
 * Service 层发现业务规则不满足时，统一抛出这类异常供控制层处理。
 * <p>
 * 典型场景包括参数不合法、对象不存在、状态不允许、保存失败等。
 * 控制层通常会捕获该异常并将异常信息转换为用户可理解的提示语。
 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
