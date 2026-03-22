package com.demo.wms.service.impl;

import com.demo.wms.store.WmsDataStore;
import com.demo.wms.util.DateTimeUtil;
import com.demo.wms.util.FileUtil;

import java.time.LocalDateTime;

/**
 * Service 层内部使用的轻量辅助工具。
 * 集中放置共享数据仓库、目录常量和少量公共判断，避免实现类里重复样板代码。
 */
final class ServiceSupport {
    static final String DATA_DIR = "data";
    static final String LOG_DIR = "logs";
    static final String SYSTEM_LOG_FILE = LOG_DIR + "/system.log";
    private static final WmsDataStore SHARED_STORE = new WmsDataStore();

    private ServiceSupport() {
    }

    static void ensureLayout() {
        FileUtil.ensureDirectory(DATA_DIR);
        FileUtil.ensureDirectory(LOG_DIR);
        FileUtil.ensureFile(SYSTEM_LOG_FILE);
    }

    static WmsDataStore createStore() {
        // 默认构造器场景下共享同一份数据仓库，避免不同 Service 各自维护一份内存数据。
        return SHARED_STORE;
    }

    static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static String formatTime(LocalDateTime time) {
        return DateTimeUtil.format(time);
    }
}
