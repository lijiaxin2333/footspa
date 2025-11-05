-- =============================================
-- 重置数据库：先删除触发器、索引、表（按外键顺序）
-- =============================================

-- 关闭外键检查，保证可以按任意顺序删除
PRAGMA foreign_keys = OFF;

-- -----------------------------
-- 删除 bills_all 相关触发器
-- -----------------------------
DROP TRIGGER IF EXISTS validate_money_from_to_insert;
DROP TRIGGER IF EXISTS validate_money_from_to_update;
DROP TRIGGER IF EXISTS validate_tags_insert;
DROP TRIGGER IF EXISTS validate_tags_update;

-- 删除 money_node 相关触发器
DROP TRIGGER IF EXISTS validate_phone_numbers_insert;
DROP TRIGGER IF EXISTS validate_phone_numbers_update;

-- -----------------------------
-- 删除索引
-- -----------------------------
DROP INDEX IF EXISTS one_public_type;
DROP INDEX IF EXISTS one_outside_type;

-- -----------------------------
-- 删除表（先删子表，再删父表）
-- -----------------------------
DROP TABLE IF EXISTS bills_all;
DROP TABLE IF EXISTS money_node;

-- 开启外键检查
PRAGMA foreign_keys = ON;

-- =============================================
-- 创建 money_node 表及约束/触发器
-- =============================================

CREATE TABLE money_node (
    id INTEGER PRIMARY KEY AUTOINCREMENT,  
    name TEXT DEFAULT '',                   
    type TEXT NOT NULL CHECK(type IN ('public','outside','employer','employee','customer')), 
    phone_numbers TEXT,                     
    CHECK(json_valid(phone_numbers) AND json_type(phone_numbers) = 'array') 
);

-- 创建唯一索引：确保只有一个 public 类型
CREATE UNIQUE INDEX one_public_type
ON money_node(type)
WHERE type = 'public';

-- 创建唯一索引：确保只有一个 outside 类型
CREATE UNIQUE INDEX one_outside_type
ON money_node(type)
WHERE type = 'outside';

-- 创建触发器：插入前检查 phone_numbers
CREATE TRIGGER validate_phone_numbers_insert
BEFORE INSERT ON money_node
FOR EACH ROW
BEGIN
    SELECT
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM json_each(NEW.phone_numbers)
            WHERE json_each.type != 'text'
               OR json_each.value NOT GLOB '[0-9]*'
        )
        THEN RAISE(ABORT, 'phone_numbers must be an array of digit strings')
    END;
END;

-- 创建触发器：更新前检查 phone_numbers
CREATE TRIGGER validate_phone_numbers_update
BEFORE UPDATE OF phone_numbers ON money_node
FOR EACH ROW
BEGIN
    SELECT
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM json_each(NEW.phone_numbers)
            WHERE json_each.type != 'text'
               OR json_each.value NOT GLOB '[0-9]*'
        )
        THEN RAISE(ABORT, 'phone_numbers must be an array of digit strings')
    END;
END;

-- 插入默认数据
INSERT INTO money_node(name, type, phone_numbers) VALUES ('public', 'public', '["999"]');
INSERT INTO money_node(name, type, phone_numbers) VALUES ('outside', 'outside', '["888"]');

-- =============================================
-- 创建 bills_all 表及约束/触发器
-- =============================================

CREATE TABLE bills_all (
    id INTEGER PRIMARY KEY AUTOINCREMENT,  
    time INTEGER NOT NULL DEFAULT (strftime('%s','now')), 
    money_from INTEGER NOT NULL,           
    money_to INTEGER NOT NULL,             
    money INTEGER NOT NULL,      
    valid INTEGER NOT NULL DEFAULT 1 CHECK(valid IN (0,1)), 
    image TEXT,                             
    tags TEXT,                               
    remark TEXT,                             
    FOREIGN KEY(money_from) REFERENCES money_node(id),
    FOREIGN KEY(money_to) REFERENCES money_node(id),
    CHECK(json_valid(tags) AND json_type(tags) = 'array') 
);

-- 创建触发器：插入前检查 money_from ≠ money_to
CREATE TRIGGER validate_money_from_to_insert
BEFORE INSERT ON bills_all
FOR EACH ROW
WHEN NEW.money_from = NEW.money_to
BEGIN
    SELECT RAISE(ABORT, 'money_from and money_to cannot be equal');
END;

-- 创建触发器：更新前检查 money_from ≠ money_to
CREATE TRIGGER validate_money_from_to_update
BEFORE UPDATE ON bills_all
FOR EACH ROW
WHEN NEW.money_from = NEW.money_to
BEGIN
    SELECT RAISE(ABORT, 'money_from and money_to cannot be equal');
END;

-- 创建触发器：插入前检查 tags 数组内都是字符串
CREATE TRIGGER validate_tags_insert
BEFORE INSERT ON bills_all
FOR EACH ROW
BEGIN
    SELECT
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM json_each(NEW.tags)
            WHERE json_each.type != 'text'
        )
        THEN RAISE(ABORT, 'tags must be json array contain strings')
    END;
END;

-- 创建触发器：更新前检查 tags 数组内都是字符串
CREATE TRIGGER validate_tags_update
BEFORE UPDATE OF tags ON bills_all
FOR EACH ROW
BEGIN
    SELECT
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM json_each(NEW.tags)
            WHERE json_each.type != 'text'
        )
        THEN RAISE(ABORT, 'tags must be json array contain strings')
    END;
END;