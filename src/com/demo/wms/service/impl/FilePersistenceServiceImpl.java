package com.demo.wms.service.impl;

import com.demo.wms.entity.Product;
import com.demo.wms.entity.StockRecord;
import com.demo.wms.entity.User;
import com.demo.wms.enums.ProductStatus;
import com.demo.wms.enums.Role;
import com.demo.wms.enums.StockRecordType;
import com.demo.wms.enums.UserStatus;
import com.demo.wms.exception.BizException;
import com.demo.wms.exception.DataParseException;
import com.demo.wms.service.LogService;
import com.demo.wms.service.PersistenceService;
import com.demo.wms.store.WmsDataStore;
import com.demo.wms.util.DateTimeUtil;
import com.demo.wms.util.FileUtil;
import com.demo.wms.util.IdUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件持久化服务。
 * 负责项目启动时的数据恢复，以及关键写操作后的立即保存。
 */
public class FilePersistenceServiceImpl implements PersistenceService {
    private static final String VERSION_LINE = "#version=1";

    private final WmsDataStore dataStore;
    private final LogService logService;
    private final Path dataDir;
    private final Path usersFile;
    private final Path productsFile;
    private final Path stockRecordsFile;

    public FilePersistenceServiceImpl() {
        this(ServiceSupport.createStore(), new FileLogServiceImpl());
    }

    public FilePersistenceServiceImpl(WmsDataStore dataStore, LogService logService) {
        this.dataStore = dataStore;
        this.logService = logService == null ? new FileLogServiceImpl() : logService;
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        this.dataDir = projectRoot.resolve("data");
        this.usersFile = dataDir.resolve("users.txt");
        this.productsFile = dataDir.resolve("products.txt");
        this.stockRecordsFile = dataDir.resolve("stock_records.txt");
    }

    @Override
    public void initialize() {
        // 启动时先把目录和文件准备好，避免后续第一次写入时因为路径不存在而失败。
        FileUtil.ensureDirectory(dataDir);
        FileUtil.ensureFile(usersFile);
        FileUtil.ensureFile(productsFile);
        FileUtil.ensureFile(stockRecordsFile);

        if (FileUtil.readLines(usersFile).isEmpty()) {
            FileUtil.writeLines(usersFile, buildSeedUsersLines());
        }
        if (FileUtil.readLines(productsFile).isEmpty()) {
            FileUtil.writeLines(productsFile, buildEmptySection("PRODUCTS"));
        }
        if (FileUtil.readLines(stockRecordsFile).isEmpty()) {
            FileUtil.writeLines(stockRecordsFile, buildEmptySection("STOCK_RECORDS"));
        }
    }

    @Override
    public void loadAll() {
        // 按“用户 -> 商品 -> 库存记录”的顺序恢复，便于后续记录引用前面的主数据。
        loadUsers();
        loadProducts();
        loadStockRecords();
    }

    @Override
    public void saveUsers() {
        List<String> lines = new ArrayList<>();
        lines.add(VERSION_LINE);
        lines.add("[USERS]");
        for (User user : dataStore.getUsersByUsername().values()) {
            // 不直接写 toString，而是显式写出字段结构，便于解析、排查和后续扩展。
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("id", user.getId());
            fields.put("username", user.getUsername());
            fields.put("password", user.getPassword());
            fields.put("role", user.getRole().name());
            fields.put("status", user.getStatus().name());
            lines.add(FileUtil.buildKeyValueLine(fields));
        }
        writeAll(usersFile, lines, "SAVE_USERS");
    }

    @Override
    public void saveProducts() {
        List<String> lines = new ArrayList<>();
        lines.add(VERSION_LINE);
        lines.add("[PRODUCTS]");
        for (Product product : dataStore.getProductsById().values()) {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("id", product.getId());
            fields.put("name", product.getName());
            fields.put("category", product.getCategory());
            fields.put("price", product.getPrice().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            fields.put("stock", String.valueOf(product.getStock()));
            fields.put("alert", String.valueOf(product.getAlertStock()));
            fields.put("status", product.getStatus().name());
            fields.put("lastModified", DateTimeUtil.format(product.getLastModified()));
            lines.add(FileUtil.buildKeyValueLine(fields));
        }
        writeAll(productsFile, lines, "SAVE_PRODUCTS");
    }

    @Override
    public void saveStockRecords() {
        List<String> lines = new ArrayList<>();
        lines.add(VERSION_LINE);
        lines.add("[STOCK_RECORDS]");
        for (StockRecord record : dataStore.getStockRecords()) {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("id", record.getId());
            fields.put("productId", record.getProductId());
            fields.put("type", record.getType().name());
            fields.put("qty", String.valueOf(record.getQuantity()));
            fields.put("beforeStock", String.valueOf(record.getBeforeStock()));
            fields.put("afterStock", String.valueOf(record.getAfterStock()));
            fields.put("operator", record.getOperator());
            fields.put("time", DateTimeUtil.format(record.getOperateTime()));
            fields.put("remark", record.getRemark());
            lines.add(FileUtil.buildKeyValueLine(fields));
        }
        writeAll(stockRecordsFile, lines, "SAVE_STOCK_RECORDS");
    }

    @Override
    public void saveAll() {
        saveUsers();
        saveProducts();
        saveStockRecords();
    }

    private void loadUsers() {
        Map<String, User> users = new LinkedHashMap<>();
        boolean partialFailure = false;
        boolean seeded = false;
        for (String line : FileUtil.readLines(usersFile)) {
            if (shouldSkipLine(line)) {
                continue;
            }
            try {
                // 加载时坚持“坏一行跳一行”，而不是让整份文件因为局部错误直接失效。
                User user = parseUser(line);
                if (users.containsKey(user.getUsername())) {
                    throw new DataParseException("用户名重复：" + user.getUsername());
                }
                users.put(user.getUsername(), user);
            } catch (Exception ex) {
                partialFailure = true;
                logService.warn("SYSTEM", "PERSISTENCE", "LOAD_USERS", "跳过用户坏行：" + line + "，原因：" + ex.getMessage());
            }
        }

        if (users.isEmpty()) {
            users = buildSeedUsers();
            seeded = true;
            logService.warn("SYSTEM", "PERSISTENCE", "LOAD_USERS", "用户数据为空，已恢复种子账号。");
        }
        dataStore.replaceUsers(users);
        if (seeded || !usersFile.toFile().exists()) {
            // 理论上 initialize 会先建好文件，这里保留兜底逻辑，避免调用顺序异常时出问题。
            saveUsers();
        }
        notifyPartialFailure(partialFailure);
    }

    private void loadProducts() {
        Map<String, Product> products = new LinkedHashMap<>();
        boolean partialFailure = false;
        for (String line : FileUtil.readLines(productsFile)) {
            if (shouldSkipLine(line)) {
                continue;
            }
            try {
                Product product = parseProduct(line);
                if (products.containsKey(product.getId())) {
                    throw new DataParseException("商品编号重复：" + product.getId());
                }
                products.put(product.getId(), product);
            } catch (Exception ex) {
                partialFailure = true;
                logService.warn("SYSTEM", "PERSISTENCE", "LOAD_PRODUCTS", "跳过商品坏行：" + line + "，原因：" + ex.getMessage());
            }
        }
        // 即使文件为空，也保持内存中的商品结构为“空但可用”状态。
        dataStore.replaceProducts(products);
        notifyPartialFailure(partialFailure);
    }

    private void loadStockRecords() {
        List<StockRecord> records = new ArrayList<>();
        boolean partialFailure = false;
        for (String line : FileUtil.readLines(stockRecordsFile)) {
            if (shouldSkipLine(line)) {
                continue;
            }
            try {
                records.add(parseStockRecord(line));
            } catch (Exception ex) {
                partialFailure = true;
                logService.warn("SYSTEM", "PERSISTENCE", "LOAD_STOCK_RECORDS", "跳过库存记录坏行：" + line + "，原因：" + ex.getMessage());
            }
        }
        dataStore.replaceStockRecords(records);
        notifyPartialFailure(partialFailure);
    }

    private void writeAll(Path file, List<String> lines, String action) {
        try {
            FileUtil.writeLines(file, lines);
        } catch (RuntimeException ex) {
            // 持久化异常统一转换成业务异常，避免上层直接依赖底层 IO 细节。
            logService.error("SYSTEM", "PERSISTENCE", action, "保存失败：" + ex.getMessage());
            throw new BizException("数据保存失败：" + file.getFileName(), ex);
        }
    }

    private User parseUser(String line) {
        Map<String, String> fields = parseFields(line);
        return new User(
                required(fields, "id"),
                required(fields, "username"),
                required(fields, "password"),
                Role.valueOf(required(fields, "role")),
                UserStatus.valueOf(required(fields, "status"))
        );
    }

    private Product parseProduct(String line) {
        Map<String, String> fields = parseFields(line);
        return new Product(
                required(fields, "id"),
                required(fields, "name"),
                required(fields, "category"),
                new BigDecimal(required(fields, "price")),
                Integer.parseInt(required(fields, "stock")),
                Integer.parseInt(required(fields, "alert")),
                ProductStatus.valueOf(required(fields, "status")),
                parseTime(required(fields, "lastModified"))
        );
    }

    private StockRecord parseStockRecord(String line) {
        Map<String, String> fields = parseFields(line);
        return new StockRecord(
                required(fields, "id"),
                required(fields, "productId"),
                StockRecordType.valueOf(required(fields, "type")),
                Integer.parseInt(required(fields, "qty")),
                Integer.parseInt(required(fields, "beforeStock")),
                Integer.parseInt(required(fields, "afterStock")),
                required(fields, "operator"),
                parseTime(required(fields, "time")),
                fields.getOrDefault("remark", "")
        );
    }

    private Map<String, String> parseFields(String line) {
        // 统一走工具类解析，支持对 |、=、\ 这类分隔字符做转义处理。
        Map<String, String> fields = FileUtil.parseKeyValueLine(line);
        if (fields.isEmpty()) {
            throw new DataParseException("字段格式错误：" + line);
        }
        return fields;
    }

    private String required(Map<String, String> fields, String key) {
        String value = fields.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new DataParseException("缺少字段：" + key);
        }
        return value.trim();
    }

    private LocalDateTime parseTime(String text) {
        try {
            return DateTimeUtil.parse(text);
        } catch (Exception ex) {
            throw new DataParseException("时间格式错误：" + text, ex);
        }
    }

    private boolean shouldSkipLine(String line) {
        String trimmed = line == null ? "" : line.trim();
        // 支持跳过空行、版本声明和节标题，让文件既可读又易于手工检查。
        return trimmed.isEmpty() || trimmed.startsWith("#") || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    private void notifyPartialFailure(boolean partialFailure) {
        if (partialFailure) {
            System.out.println("提示：部分数据加载失败，异常行已跳过，请查看 logs/system.log。");
        }
    }

    private Map<String, User> buildSeedUsers() {
        Map<String, User> users = new LinkedHashMap<>();
        users.put("admin", new User("U001", "admin", "123456", Role.ADMIN, UserStatus.ACTIVE));
        users.put("tom", new User("U002", "tom", "123456", Role.OPERATOR, UserStatus.ACTIVE));
        return users;
    }

    private List<String> buildSeedUsersLines() {
        List<String> lines = new ArrayList<>();
        lines.add(VERSION_LINE);
        lines.add("[USERS]");
        // 首次启动自动生成种子账号，方便直接演示登录流程，也能体现“数据与代码分离”。
        for (User user : buildSeedUsers().values()) {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("id", user.getId());
            fields.put("username", user.getUsername());
            fields.put("password", user.getPassword());
            fields.put("role", user.getRole().name());
            fields.put("status", user.getStatus().name());
            lines.add(FileUtil.buildKeyValueLine(fields));
        }
        return lines;
    }

    private List<String> buildEmptySection(String sectionName) {
        List<String> lines = new ArrayList<>();
        lines.add(VERSION_LINE);
        lines.add("[" + sectionName + "]");
        return lines;
    }
}
