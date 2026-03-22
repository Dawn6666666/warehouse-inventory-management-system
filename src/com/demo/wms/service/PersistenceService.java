package com.demo.wms.service;

/**
 * 持久化服务接口。
 * 负责项目数据文件的初始化、加载和保存。
 */
public interface PersistenceService {
    void initialize();

    void loadAll();

    void saveUsers();

    void saveProducts();

    void saveStockRecords();

    void saveAll();
}
