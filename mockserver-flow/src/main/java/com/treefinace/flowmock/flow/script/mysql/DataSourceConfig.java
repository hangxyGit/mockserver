package com.treefinace.flowmock.flow.script.mysql;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


//数据库配置统一在config/mysql/db.properties中
@Configuration
@PropertySource(value = "classpath:config/mysql/db.properties")
public class DataSourceConfig {
    private String typeAliasesPackage = "com.main.example.bean.**.*";

    /*
     * 动态数据源
     * dbMap中存放数据源名称与数据源实例，数据源名称存于DataEnum.DbSource中
     * setDefaultTargetDataSource方法设置默认数据源
     */
    @Bean(name = "dynamicDataSource")
    public DataSource dynamicDataSource() {
        DataSource dataSource = DataSourceBuilder.create().build();
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        //配置多数据源
        Map<Object, Object> dbMap = new HashMap();
        dbMap.put("", dataSource);
        dynamicDataSource.setTargetDataSources(dbMap);

        // 设置默认数据源
        dynamicDataSource.setDefaultTargetDataSource(dataSource);
        return dynamicDataSource;
    }

    /*
     * 数据库连接会话工厂
     * 将动态数据源赋给工厂
     * mapper存于resources/mapper目录下
     * 默认bean存于com.main.example.bean包或子包下，也可直接在mapper中指定
     */
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dynamicDataSource());
        sqlSessionFactory.setTypeAliasesPackage(typeAliasesPackage); //扫描bean
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactory.setMapperLocations(resolver.getResources("classpath*:mapper/*.xml"));    // 扫描映射文件
        return sqlSessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        // 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
        return new DataSourceTransactionManager(dynamicDataSource());
    }
}
