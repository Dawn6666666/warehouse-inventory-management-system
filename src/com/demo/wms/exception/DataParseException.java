package com.demo.wms.exception;

/**
 * 数据解析异常。
 * 用于描述文件内容格式错误、字段缺失或类型转换失败等问题。
 * <p>
 * 该异常主要服务于持久化层，在读取文本文件并恢复实体对象时使用，
 * 便于将“数据格式问题”与一般业务异常区分开来。
 */
public class DataParseException extends BizException {
    public DataParseException(String message) {
        super(message);
    }

    public DataParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
