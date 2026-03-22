package com.demo.wms.service;

import java.util.List;

/**
 * 日志服务接口。
 * 用于统一记录系统中的关键操作信息，并提供日志查询能力。
 */
public interface LogService {
    /**
     * 记录一条成功级别的日志。
     * <p>
     * 适用于业务正常完成且结果明确的场景，例如登录成功、商品新增成功、
     * 入库成功等。调用方应传入能够反映业务结果的简要说明，便于后续查看日志时
     * 快速定位操作含义。
     *
     * @param operator 操作人
     * @param module 所属模块
     * @param action 执行动作
     * @param result 成功结果说明
     */
    void info(String operator, String module, String action, String result);

    /**
     * 记录一条告警级别的日志。
     * <p>
     * 适用于出现可预期问题但系统仍能继续运行的场景，例如输入不合法、
     * 查询结果为空、业务条件不满足等。这类日志通常不表示系统故障，
     * 但能够反映用户操作中的异常分支。
     *
     * @param operator 操作人
     * @param module 所属模块
     * @param action 执行动作
     * @param reason 告警原因说明
     */
    void warn(String operator, String module, String action, String reason);

    /**
     * 记录一条错误级别的日志。
     * <p>
     * 适用于业务执行失败、系统异常、持久化异常等场景。该方法强调的是
     * “操作未按预期完成”，因此应尽量提供明确的失败原因，便于问题定位。
     *
     * @param operator 操作人
     * @param module 所属模块
     * @param action 执行动作
     * @param reason 错误原因说明
     */
    void error(String operator, String module, String action, String reason);

    /**
     * 读取当前系统中已经保存的日志内容。
     * <p>
     * 返回结果通常按日志存储顺序组织，每个元素代表一条可直接展示的日志记录。
     * 控制层可基于该结果执行查询、打印或导出等操作。
     *
     * @return 日志文本列表；若当前没有日志，通常返回空列表
     */
    List<String> readLogs();
}
