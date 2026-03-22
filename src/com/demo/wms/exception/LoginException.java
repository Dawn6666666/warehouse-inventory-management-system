package com.demo.wms.exception;

/**
 * 登录异常。
 * 表示用户名、密码或账号状态校验未通过。
 * <p>
 * 该异常是业务异常在认证场景下的细化，用于明确区分“登录失败”
 * 与其他商品、库存、持久化等业务错误。
 */
public class LoginException extends BizException {
    public LoginException(String message) {
        super(message);
    }
}
