package br.com.sebratel.bff.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "br.com.sebratel.bff.repository.radius",
        entityManagerFactoryRef = "radiusEntityManagerFactory",
        transactionManagerRef = "radiusTransactionManager"
)
public class RadiusDbConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.radius")
    public DataSourceProperties radiusDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "radiusDataSource")
    public DataSource radiusDataSource() {
        // Como o properties usa .jdbc-url, o builder do Hikari fará o bind correto
        return radiusDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "radiusEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean radiusEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("radiusDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("br.com.sebratel.bff.model")
                .persistenceUnit("radius")
                .build();
    }

    @Bean(name = "radiusTransactionManager")
    public PlatformTransactionManager radiusTransactionManager(
            @Qualifier("radiusEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}