# 项目讲解指南

讲解顺序：由表及里

```
先讲这是什么项目
    │
    ▼
再讲整体架构设计（纵览目录结构）
    │
    ▼
然后按层次讲每个包的职责和设计思想
    │
    ▼
最后讲核心业务流程和数据流转
    │
    ▼
演示 + 总结
```

---

## 第一步：项目定位

### 项目是什么

这是一个用 JavaSE 实现的控制台版仓库管理系统。核心目标不是堆功能，而是展示怎么把课程中学到的知识用在实际项目里。

### 涵盖的知识点

- 分层设计（控制层、服务层、数据层分离）
- 面向对象（实体建模、接口抽象）
- 集合框架（Map、List 的实际应用）
- 接口与多态（权限策略、服务抽象）
- 文件持久化（数据保存与恢复）
- 异常处理（统一异常管理）
- 日志记录（操作留痕）

### 为什么做这个项目

课程项目常见问题是：能跑但设计混乱、功能堆砌但结构不清。这个项目展示如何从"能跑"到"设计良好"，让代码：
- 结构清晰（分层明确）
- 易于扩展（好改好加功能）
- 稳定可靠（异常情况不会崩）
- 可追溯（有日志）
- 可讲解（别人能看懂）

---

## 第二步：整体架构纵览

先从目录结构看系统的分层设计，这是理解整个项目的基础。

### 目录结构

```
src/com/demo/wms
├── app/                      【入口层】
│   └── Application.java
│
├── controller/               【控制层】
│   ├── LoginController.java
│   ├── MainController.java
│   ├── ProductController.java
│   ├── StockController.java
│   └── UserController.java
│
├── service/                  【服务层】
│   ├── AuthService.java      ────┐
│   ├── ProductService.java       │ 接口定义
│   ├── InventoryService.java     │
│   ├── UserService.java          │
│   ├── LogService.java           │
│   ├── PersistenceService.java   │
│   └── impl/                 ────┘
│       ├── AuthServiceImpl.java
│       ├── ProductServiceImpl.java
│       ├── InventoryServiceImpl.java
│       ├── UserServiceImpl.java
│       ├── FileLogServiceImpl.java
│       ├── FilePersistenceServiceImpl.java
│       └── ServiceSupport.java
│
├── entity/                   【实体层】
│   ├── User.java
│   ├── Product.java
│   ├── StockRecord.java
│   └── OperationLog.java
│
├── enums/                    【枚举层】
│   ├── Role.java
│   ├── UserStatus.java
│   ├── ProductStatus.java
│   ├── StockRecordType.java
│   └── LogLevel.java
│
├── permission/               【权限策略层】
│   ├── PermissionPolicy.java
│   ├── AdminPermissionPolicy.java
│   └── OperatorPermissionPolicy.java
│
├── store/                    【数据存储层】
│   └── WmsDataStore.java
│
└── util/                     【工具层】
    ├── InputUtil.java
    ├── DateTimeUtil.java
    ├── FileUtil.java
    └── IdUtil.java
```

### 数据流转架构

```mermaid
graph TB
    A[用户操作] --> B[app/ 入口层<br/>程序启动、对象装配]
    B --> C[controller/ 控制层<br/>菜单展示、输入采集、流程调度]
    C --> D[service/ 服务层<br/>业务规则、数据校验、权限控制]
    D --> E[store/ 数据存储层<br/>内存数据管理]
    D --> F[持久化 & 日志<br/>文件读写、操作记录]
    E <--> F

    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C fill:#ffe1f5
    style D fill:#e1ffe1
    style E fill:#f0f0f0
    style F fill:#f0f0f0
```

### 分层职责图

```mermaid
graph LR
    subgraph 控制层
    C1[菜单展示]
    C2[输入采集]
    C3[流程调度]
    C4[异常处理]
    end

    subgraph 服务层
    S1[业务规则]
    S2[数据校验]
    S3[权限控制]
    S4[调用持久化]
    end

    subgraph 数据层
    D1[内存数据]
    D2[文件存储]
    D3[日志记录]
    end

    C1 --> S1
    C2 --> S2
    C3 --> S3
    C4 --> S4
    S1 --> D1
    S2 --> D2
    S3 --> D3
```

### 设计思想

> "系统按照分层架构组织，每层有明确的职责边界："
>
> "- **入口层**：管启动，不管业务"
> "- **控制层**：管流程，不管规则"
> "- **服务层**：管规则，不管存储"
> "- **存储层**：管数据，不管业务"
>
> "这样分层的好处是：改动一层不影响其他层，代码好维护、好扩展。比如以后想把文件存储换成数据库，只需要改持久化层，其他层都不用动。"

---

## 第三步：按层次讲解设计思想

### 1. app/ 入口层

**文件**：`Application.java`

**职责**：
- 程序启动入口
- 创建各个层的对象
- 完成依赖注入（把需要的服务传给控制器）
- 启动主流程

**设计思想**：

入口类不应该写业务逻辑，它只负责"把东西建好、连起来、启动起来"。这是一种"依赖注入"的思想。

```mermaid
graph TB
    A[Application.main] --> B[创建 WmsDataStore<br/>数据仓库]
    A --> C[创建 Service 实现类<br/>业务服务]
    A --> D[创建 Controller<br/>控制器]
    B --> C
    C --> D
    D --> E[启动主流程<br/>进入登录菜单]

    style A fill:#e1f5ff
    style B fill:#ffe1e1
    style C fill:#e1ffe1
    style D fill:#ffe1f5
    style E fill:#fff4e1
```

**为什么要这样设计**：

如果不这样做，让控制器自己去 new 服务对象，会导致：
- 控制器和具体实现类强耦合
- 想换实现方式时，必须改控制器代码
- 代码难以测试和维护

通过依赖注入：
- 控制器只知道接口，不知道具体实现
- 换实现只需要改入口类的一行代码
- 各层之间解耦，更灵活

**讲解要点**：

> "看入口类，它做的事很简单：创建数据仓库、创建服务、创建控制器，然后把服务注入给控制器，最后启动。"
>
> "为什么不在控制器里直接 new 服务？因为那样控制器就和具体实现绑死了。现在控制器只依赖接口，将来想换实现方式，入口类改一行就行。"

---

### 2. controller/ 控制层

**文件**：
- `LoginController` - 登录流程
- `MainController` - 主菜单调度
- `ProductController` - 商品管理菜单
- `StockController` - 库存操作菜单
- `UserController` - 用户管理菜单

**职责**：
- 展示菜单
- 采集用户输入
- 调用服务层完成业务
- 展示结果或错误信息
- 控制流程跳转

**设计思想**：

控制器只负责"流程怎么走"，不负责"规则怎么定"。具体业务逻辑都委托给服务层。

```mermaid
graph LR
    A[用户输入] --> B[Controller 接收]
    B --> C{输入校验<br/>格式是否合法}
    C -->|不合法| D[提示错误<br/>重新输入]
    C -->|合法| E[调用 Service]
    E --> F[Service 返回结果]
    F --> G{结果类型}
    G -->|成功| H[展示成功信息]
    G -->|失败| I[展示错误原因]
    H --> J[返回菜单/继续流程]
    I --> J

    style B fill:#ffe1f5
    style E fill:#e1ffe1
    style D fill:#ffe1e1
    style I fill:#ffe1e1
```

**为什么分多个控制器**：

如果所有菜单都放在一个控制器里：
- 单个类过于庞大
- 不同业务逻辑混在一起
- 难以维护和扩展

分控制器的好处：
- 每个控制器专注一块业务
- 职责清晰，易于理解
- 修改某块业务不影响其他部分

**讲解要点**：

> "为什么要有这么多控制器？因为不同业务有不同的菜单和流程。"
>
> "控制器只做三件事：收输入、调服务、展示结果。它不判断库存够不够，那是服务层的事。控制器就像前台接待，把客户需求转给后台处理。"

---

### 3. service/ 服务层

**接口定义**：
- `AuthService` - 认证服务
- `ProductService` - 商品服务
- `InventoryService` - 库存服务
- `UserService` - 用户服务
- `LogService` - 日志服务
- `PersistenceService` - 持久化服务

**实现类**：
- `AuthServiceImpl` - 登录校验、权限判断
- `ProductServiceImpl` - 商品增删改查
- `InventoryServiceImpl` - 入库出库核心逻辑
- `UserServiceImpl` - 用户管理
- `FileLogServiceImpl` - 日志写入
- `FilePersistenceServiceImpl` - 文件读写

**职责**：
- 定义业务规则
- 执行数据校验
- 进行权限判断
- 调用持久化层保存数据

**设计思想**：

先定义接口，再写实现。接口定义"要做什么"，实现类定义"怎么做"。

```mermaid
graph TB
    subgraph 服务层接口
    SI1[AuthService]
    SI2[ProductService]
    SI3[InventoryService]
    end

    subgraph 服务层实现
    SIM1[AuthServiceImpl]
    SIM2[ProductServiceImpl]
    SIM3[InventoryServiceImpl]
    end

    subgraph 控制层
    C[Controllers]
    end

    C --> SI1
    C --> SI2
    C --> SI3
    SI1 --> SIM1
    SI2 --> SIM2
    SI3 --> SIM3

    style SI1 fill:#e1f5ff
    style SI2 fill:#e1f5ff
    style SI3 fill:#e1f5ff
    style SIM1 fill:#e1ffe1
    style SIM2 fill:#e1ffe1
    style SIM3 fill:#e1ffe1
```

**为什么要接口和实现分离**：

如果只有实现类没有接口：
- 控制器直接依赖具体实现
- 换实现方式需要改控制器代码
- 难以进行单元测试（无法 mock）

接口和实现分离的好处：
- 控制器只依赖接口，不依赖具体实现
- 换实现方式只需要新增一个实现类
- 可以方便地进行单元测试

**核心服务说明**：

1. **InventoryService**（最重要）
   - 入库：增加库存、生成入库记录
   - 出库：减少库存、生成出库记录
   - 核心业务规则都在这里

2. **PersistenceService**
   - 启动时加载数据文件
   - 关键操作后保存数据
   - 数据恢复的保证

3. **AuthService**
   - 登录校验
   - 权限判断（能不能做某操作）

**讲解要点**：

> "服务层分为接口和实现两部分。"
>
> "接口定义能力：比如'需要有入库方法'、'需要有查询方法'。"
>
> "实现类定义具体怎么做：比如怎么校验、怎么修改数据、怎么写文件。"
>
> "这样做的好处：未来如果想把文件存储换成数据库，只需要新增一个 DatabasePersistenceServiceImpl，控制器和服务接口都不用改。这就是面向接口编程的价值。"

---

### 4. entity/ 实体层

**文件**：
- `User` - 用户信息
- `Product` - 商品信息
- `StockRecord` - 库存变动记录
- `OperationLog` - 操作日志

**职责**：
- 定义业务数据模型
- 封装业务对象的属性和行为
- 提供类型安全的数据访问

**设计思想**：

实体类对应的是业务对象，不是数据库表。每个字段都有明确的业务含义。

```mermaid
classDiagram
    class User {
        String id
        String username
        String password
        Role role
        UserStatus status
        +copy() User
        +isActive() boolean
    }

    class Product {
        String id
        String name
        String category
        BigDecimal price
        int stock
        int alertStock
        ProductStatus status
        LocalDateTime lastModified
        +copy() Product
        +isActive() boolean
    }

    class StockRecord {
        String id
        String productId
        StockRecordType type
        int quantity
        int beforeStock
        int afterStock
        String operator
        LocalDateTime operateTime
        String remark
        +copy() StockRecord
    }

    User "1" --> "0..*" Product : manages
    Product "1" --> "0..*" StockRecord : has
```

**为什么用实体类不用 Map**：

如果用 Map 存储业务数据：
```java
Map<String, Object> user = new HashMap<>();
user.put("username", "admin");
user.put("role", "ADMIN");
// 访问时没有类型检查
String role = (String) user.get("role"); // 容易出错
```

用实体类的好处：
```java
User user = new User();
user.setUsername("admin");
user.setRole(Role.ADMIN);
// 编译器会检查类型
Role role = user.getRole(); // 类型安全
```

**讲解要点**：

> "实体类对应的是业务对象，不是数据库表。"
>
> "比如 Product 有什么字段？看业务需求：编号、名称、分类、价格、库存、预警线、状态、最后修改时间。"
>
> "为什么不用 Map？因为 Map 没有类型约束，容易写错字段名，取值时要强制类型转换。实体类有编译器检查，更安全，IDE 也有提示。"

---

### 5. enums/ 枚举层

**文件**：
- `Role` - 角色（ADMIN、OPERATOR）
- `UserStatus` - 用户状态（ACTIVE、DISABLED）
- `ProductStatus` - 商品状态（ACTIVE、DISABLED）
- `StockRecordType` - 记录类型（IN、OUT）
- `LogLevel` - 日志级别（INFO、WARNING、ERROR）

**职责**：
- 定义业务中的固定选项
- 限定可选值范围
- 提供类型安全的常量

**设计思想**：

把可选值限定在明确范围内，比用字符串常量更安全。

**字符串 vs 枚举对比**：

```java
// 用字符串（容易出错）
if ("ADMIN".equals(user.getRole())) {  // 可能写成 "ADIMN"
    // ...
}

// 用枚举（编译器检查）
if (Role.ADMIN.equals(user.getRole())) {  // 拼写错误编译器会报错
    // ...
}
```

**讲解要点**：

> "为什么用枚举不用字符串？"
>
> "字符串容易写错，写成 'ADIMN' 编译器也不会报错，要到运行时才发现问题。"
>
> "枚举写错了，编译器直接告诉你，IDE 还有自动提示。而且枚举可以有方法和属性，比字符串更强大。"

---

### 6. permission/ 权限策略层

**文件**：
- `PermissionPolicy`（接口）- 权限策略接口
- `AdminPermissionPolicy` - 管理员权限实现
- `OperatorPermissionPolicy` - 操作员权限实现

**职责**：
- 封装不同角色的权限规则
- 判断某个角色能不能做某操作

**设计思想**：

用策略模式而不是 if-else 判断角色。不同角色有不同权限实现类。

**策略模式结构**：

```mermaid
classDiagram
    class PermissionPolicy {
        <<interface>>
        +canManageUser() boolean
        +canManageProduct() boolean
        +canStockIn() boolean
        +canStockOut() boolean
        +canViewInventory() boolean
        +canViewStockRecords() boolean
        +canViewLogs() boolean
    }

    class AdminPermissionPolicy {
        +canManageUser() true
        +canManageProduct() true
        +canStockIn() true
        +canStockOut() true
        +canViewInventory() true
        +canViewStockRecords() true
        +canViewLogs() true
    }

    class OperatorPermissionPolicy {
        +canManageUser() false
        +canManageProduct() false
        +canStockIn() true
        +canStockOut() true
        +canViewInventory() true
        +canViewStockRecords() true
        +canViewLogs() false
    }

    PermissionPolicy <|-- AdminPermissionPolicy
    PermissionPolicy <|-- OperatorPermissionPolicy
```

**传统 if-else 方式的问题**：

```java
// 到处都是角色判断
public void manageProducts() {
    if (user.getRole() == Role.ADMIN) {
        // 允许
    } else {
        // 拒绝
    }
}

public void manageUsers() {
    if (user.getRole() == Role.ADMIN) {
        // 允许
    } else {
        // 拒绝
    }
}
// 每个方法都要判断，代码重复，难以维护
```

**策略模式的方式**：

```java
// 每个用户有一个权限策略对象
PermissionPolicy policy = user.getPermissionPolicy();

// 判断时直接调用
if (policy.canManageProducts()) {
    // 允许
}

// 要加新角色，新增实现类就行
public class SuperAdminPermissionPolicy implements PermissionPolicy {
    public boolean canManageProducts() { return true; }
    public boolean canManageUsers() { return true; }
    public boolean canViewLogs() { return true; }
}
```

**权限获取流程**：

```mermaid
graph TB
    A[用户登录] --> B{用户角色}
    B -->|ADMIN| C[创建<br/>AdminPermissionPolicy]
    B -->|OPERATOR| D[创建<br/>OperatorPermissionPolicy]
    C --> E[附加到 User 对象]
    D --> E
    E --> F[业务操作时判断权限]
    F --> G[调用 policy.canXXX]
    G --> H{返回结果}
    H -->|true| I[允许操作]
    H -->|false| J[拒绝操作]

    style C fill:#e1ffe1
    style D fill:#ffe1e1
```

**讲解要点**：

> "不同角色的权限不一样，如果用 if-else 到处判断，代码会很难维护。"
>
> "这里用策略模式：定义一个权限策略接口，不同角色有不同实现。判断权限时直接调用策略对象的方法。"
>
> "要加新角色？新增一个实现类，不用改已有代码。这就是开闭原则的实际应用：对扩展开放，对修改关闭。"

---

### 7. store/ 数据存储层

**文件**：`WmsDataStore`

**职责**：
- 集中管理运行期所有内存数据
- 提供统一的数据访问接口
- 支持快照回滚机制

**设计思想**：

所有共享数据都放在这一个类里。服务层不直接持有数据，而是通过 DataStore 访问。便于统一管理和实现快照回滚。

**数据结构设计**：

```java
// 用户数据：用户名 → 用户对象
Map<String, User> usersByUsername;

// 商品数据：商品编号 → 商品对象
Map<String, Product> productsById;

// 库存记录：保持时间顺序
List<StockRecord> stockRecords;

// 商品对应记录：商品编号 → 该商品的记录列表
Map<String, List<StockRecord>> stockRecordsByProductId;
```

**为什么这样选择集合类型**：

```mermaid
graph TB
    subgraph 需要快速查找
    A1[用户登录<br/>按用户名查找] --> B1[Map&lt;String, User&gt;]
    A2[商品管理<br/>按编号查找] --> B2[Map&lt;String, Product&gt;]
    end

    subgraph 需要保持顺序
    A3[库存记录<br/>按时间查询] --> B3[List&lt;StockRecord&gt;]
    A4[商品历史<br/>按商品查询] --> B4[Map&lt;String, List&gt;]
    end

    style B1 fill:#e1ffe1
    style B2 fill:#e1ffe1
    style B3 fill:#ffe1e1
    style B4 fill:#ffe1e1
```

**访问模式对比**：

| 操作 | 用 Map | 用 List |
|------|--------|---------|
| 按编号查找商品 | O(1) 直接定位 | O(n) 遍历查找 |
| 按用户名登录 | O(1) 直接定位 | O(n) 遍历查找 |
| 查询所有记录 | - | O(1) 顺序访问 |
| 保持时间顺序 | 需额外排序 | 天然有序 |

**快照回滚机制**：

```mermaid
graph TB
    A[准备修改数据] --> B[创建当前状态的快照<br/>深拷贝所有数据]
    B --> C[修改内存数据]
    C --> D[尝试写文件]
    D --> E{写入成功?}
    E -->|是| F[完成操作]
    E -->|否| G[恢复快照<br/>替换为修改前的数据]
    G --> H[抛出异常]

    style B fill:#fff4e1
    style G fill:#ffe1e1
    style F fill:#e1ffe1
```

**讲解要点**：

> "为什么用户和商品用 Map，记录用 List？"
>
> "- 用户登录需要按用户名查找，用 Map 是 O(1)，用 List 要 O(n) 遍历"
> "- 商品管理需要按编号查找，同样用 Map 更快"
> "- 库存记录天然有顺序，要按时间查询，用 List 更合适"
>
> "不是所有数据都用同一个结构，要看你怎么用。需要快速查找的用 Map，需要保持顺序的用 List。"
>
> "关键操作前先拍快照，如果失败就恢复。这样保证内存和磁盘数据一致，不会出现'内存改了但文件没写成功'的问题。"

---

### 8. util/ 工具层

**文件**：
- `InputUtil` - 输入处理（读取、校验）
- `DateTimeUtil` - 时间格式化
- `FileUtil` - 文件操作
- `IdUtil` - ID 生成

**职责**：
- 提供跨层使用的通用能力
- 封装重复代码
- 统一处理逻辑

**设计思想**：

把重复使用的功能抽取成工具类。它们不属于任何业务层，各层都可以调用。

**工具类使用示例**：

```mermaid
graph TB
    subgraph 各层都可以用
    A[Controller<br/>需要读取输入]
    B[Service<br/>需要生成ID]
    C[Service Impl<br/>需要格式化时间]
    D[Service Impl<br/>需要操作文件]
    end

    A --> E[InputUtil]
    B --> F[IdUtil]
    C --> G[DateTimeUtil]
    D --> H[FileUtil]

    style E fill:#e1f5ff
    style F fill:#e1f5ff
    style G fill:#e1f5ff
    style H fill:#e1f5ff
```

**讲解要点**：

> "工具类提供通用的能力：输入处理、时间格式化、文件操作、ID生成。"
>
> "它们不属于任何业务层，各层都可以用。这样可以避免重复代码，统一处理逻辑。"

---

## 第四步：核心业务流程

### 入库/出库完整流程

这是系统最重要的业务逻辑，展示了各层如何协同工作。

```mermaid
graph TB
    A[用户输入<br/>商品编号、数量] --> B[Controller 接收请求]
    B --> C[调用 InventoryService]
    C --> D{校验商品是否存在}
    D -->|不存在| E[抛出异常<br/>商品不存在]
    D -->|存在| F{校验商品是否启用}
    F -->|已停用| G[抛出异常<br/>商品已停用]
    F -->|已启用| H{校验数量是否合法}
    H -->|不合法| I[抛出异常<br/>数量错误]
    H -->|合法| J{校验备注长度}
    J -->|超限| K[抛出异常<br/>备注长度超限]
    J -->|合法| L{出库时<br/>库存是否足够}
    L -->|不足| M[抛出异常<br/>库存不足]
    L -->|足够/入库| N[创建内存快照]
    N --> O[修改商品库存]
    O --> P[生成库存记录]
    P --> Q[调用 PersistenceService<br/>保存文件]
    Q --> R{写入成功}
    R -->|是| S[写入日志]
    R -->|否| T[恢复快照]
    T --> U[抛出异常]
    S --> V[返回成功]
    V --> W[Controller 展示结果]

    style E fill:#ffe1e1
    style G fill:#ffe1e1
    style I fill:#ffe1e1
    style K fill:#ffe1e1
    style M fill:#ffe1e1
    style T fill:#ffe1e1
    style U fill:#ffe1e1
    style N fill:#fff4e1
    style S fill:#e1ffe1
    style V fill:#e1ffe1
```

**流程要点**：

1. **多层校验**：商品存在性 → 商品状态 → 数量合法性 → 备注长度 → 库存充足性
2. **快照保护**：修改前先保存当前状态
3. **事务保证**：要么全成功，要么回滚到原点
4. **日志留痕**：成功后记录操作

**入库和出库的差异**：

```mermaid
graph LR
    subgraph 入库
    A1[数量为正] --> B1[库存增加]
    B1 --> C1[生成入库记录]
    end

    subgraph 出库
    A2[数量为负] --> B2[库存减少]
    B2 --> C2[生成出库记录<br/>需校验库存充足]
    end

    style C1 fill:#e1ffe1
    style C2 fill:#ffe1e1
```

**讲解要点**：

> "入库出库共用一条流程，只是数量正负不同。这样代码复用性更好，也保证了两边的校验规则一致。"
>
> "关键操作前先拍快照，如果失败就回滚。这样保证数据一致性：要么全成功，要么回到原点，不会出现中间状态。"

---

## 第五步：数据持久化

### 持久化职责

**文件**：`FilePersistenceServiceImpl`

**职责**：
- 程序启动时加载数据文件
- 关键操作后保存数据
- 处理文件不存在的情况
- 容错处理（坏行跳过）

### 启动加载流程

```mermaid
graph TB
    A[程序启动] --> B[检查 data/ 目录]
    B --> C{目录是否存在}
    C -->|否| D[创建目录]
    C -->|是| E[检查数据文件]
    D --> E
    E --> F{文件是否存在}
    F -->|否| G[创建文件<br/>写入默认数据]
    F -->|是| H[逐行读取文件]
    G --> I[完成初始化]
    H --> J{行内容是否合法}
    J -->|合法| K[解析成对象<br/>加载到内存]
    J -->|非法| L[记录错误日志<br/>跳过该行]
    K --> M{还有数据}
    L --> M
    M -->|是| H
    M -->|否| I

    style G fill:#e1ffe1
    style K fill:#e1ffe1
    style L fill:#fff4e1
```

### 数据保存时机

```mermaid
graph TB
    A[关键操作] --> B[修改内存数据]
    B --> C[立即调用保存方法]
    C --> D[序列化为文本]
    D --> E[写入临时文件]
    E --> F{写入成功}
    F -->|是| G[替换原文件]
    F -->|否| H[保留原文件<br/>抛出异常]
    G --> I[返回成功]

    style G fill:#e1ffe1
    style H fill:#ffe1e1
```

**为什么立即保存**：

- 保证数据不丢失（程序异常关闭也能恢复）
- 避免批量操作失败导致全部丢失
- 简化实现（不需要缓存机制）

**讲解要点**：

> "程序启动时自动检查文件，不存在就创建，存在就加载。"
>
> "读取时遇到坏行不会让程序崩溃，而是记录日志并跳过。这是工程化的容错设计。"
>
> "关键操作（入库出库）完成后立即写文件，保证程序关闭后数据不丢失。虽然频繁写文件有性能开销，但对于课程项目来说，简单可靠更重要。"

---

## 第六步：演示建议

### 推荐演示流程

```mermaid
graph LR
    A[1.启动程序<br/>展示登录界面] --> B[2.admin登录<br/>展示管理员菜单]
    B --> C[3.新增商品<br/>演示数据录入]
    C --> D[4.入库20件<br/>演示库存变更]
    D --> E[5.出库2件<br/>演示业务校验]
    E --> F[6.查询库存<br/>查看当前状态]
    F --> G[7.查询记录<br/>查看流水]
    G --> H[8.退出程序]
    H --> I[9.重新启动<br/>验证数据恢复]
    I --> J[10.打开文件<br/>展示存储格式]

    style A fill:#e1f5ff
    style D fill:#e1ffe1
    style E fill:#ffe1e1
    style I fill:#fff4e1
    style J fill:#f0f0f0
```

### 演示要点

- 围绕"新增 → 入库 → 出库 → 查询 → 重启恢复"这条主线
- 每一步说明在哪个层处理
- 重点展示数据不会丢失
- 可以打开文件展示数据格式

### 异常场景演示

可以演示一些异常情况，展示系统的健壮性：

- 输入不存在的商品编号
- 出库数量超过库存
- 输入负数数量
- 文件被损坏（手动改坏文件，看程序能否容错）
