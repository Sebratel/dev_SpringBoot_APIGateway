package br.com.sebratel.bff.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "br.com.sebratel.bff.repository.afetados.dho",
        entityManagerFactoryRef = "dhoEntityManagerFactory",
        transactionManagerRef = "dhoTransactionManager"
)
public class DhoDbConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.dho")
    public DataSourceProperties dhoDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dhoDataSource")
    public DataSource dhoDataSource() {
        return dhoDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "dhoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dhoEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("dhoDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        return builder
                .dataSource(dataSource)
                .packages("br.com.sebratel.bff.model.entity.dho")
                .persistenceUnit("dho")
                .properties(properties)
                .build();
    }

    @Bean(name = "dhoTransactionManager")
    public PlatformTransactionManager dhoTransactionManager(
            @Qualifier("dhoEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
