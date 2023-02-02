package com.zuijianren.jpaSwaggerSqlserverAutoComment.core;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 是否注入 自动添加sqlserver注释 Bean 的条件判断
 *
 * @author 姜辞旧
 * @date 2023/1/29 10:27
 */
@Component
@ConfigurationProperties(prefix = "auto-comment.sqlserver.comment")
public class SqlServerCommentConfig {

    private boolean autoAdd = false;

    private boolean showSql = false;

    private List<String> entityPackages = new ArrayList<>();

    public boolean isAutoAdd() {
        return autoAdd;
    }

    public void setAutoAdd(boolean autoAdd) {
        this.autoAdd = autoAdd;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public List<String> getEntityPackages() {
        return entityPackages;
    }

    public void setEntityPackages(List<String> entityPackages) {
        this.entityPackages = entityPackages;
    }
}
