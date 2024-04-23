# BS-GNN-TOOLS
结合了AST-GNN和BC-GNN的可视化工具，用springboot以及mysql实现的一个在线平台。

## 使用方法
1.安装java8环境和mysql5.7数据库

2.创建数据库demo，并创建对应的数据表。

```mysql
CREATE TABLE BCCRecord (
    recordId VARCHAR(255) PRIMARY KEY,
    filePath VARCHAR(255),
    uploadTime VARCHAR(255),
    totalBytes VARCHAR(255),
    vulnerability VARCHAR(255),
    state VARCHAR(255),
    log VARCHAR(255),
    hash VARCHAR(255),
    userId VARCHAR(255)
);

CREATE TABLE Record (
    recordId VARCHAR(255) PRIMARY KEY,
    filePath VARCHAR(255),
    uploadTime VARCHAR(255),
    totalBytes VARCHAR(255),
    vulnerability VARCHAR(255),
    state VARCHAR(255),
    log VARCHAR(255),
    hash VARCHAR(255),
    userId VARCHAR(255)
);

CREATE TABLE SCLRecord (
    recordId VARCHAR(255) PRIMARY KEY,
    filePath VARCHAR(255),
    uploadTime VARCHAR(255),
    totalBytes VARCHAR(255),
    vulnerability VARCHAR(255),
    state VARCHAR(255),
    log VARCHAR(255),
    hash VARCHAR(255),
    userId VARCHAR(255)
);

CREATE TABLE user (
    userId VARCHAR(20),
    username VARCHAR(20) UNIQUE,
    password VARCHAR(20)
);
```

3.启动项目后，通过ip:8080/index进入首页，并开始后续的使用过程。