package com.demo.wms.exception;

/**
 * 库存不足异常。
 * 主要用于出库场景下的业务拒绝提示。
 * <p>
 * 当出库数量大于当前可用库存时抛出该异常，用于突出“失败原因是库存不足”，
 * 而不是一般性的参数或状态错误。
 */
public class StockNotEnoughException extends BizException {
    public StockNotEnoughException(String message) {
        super(message);
    }
}
