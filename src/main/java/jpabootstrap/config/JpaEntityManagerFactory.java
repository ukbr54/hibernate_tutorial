package jpabootstrap.config;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */
public class JpaEntityManagerFactory {

    private final String DB_URL = "jdbc:mysql://localhost:3306/test_db";
    private final String DB_USER_NAME = "root";
    private final  String DB_PASSWORD = "root";
    private final Class[] entityClasses;

    public JpaEntityManagerFactory(Class[] entityClasses){
        this.entityClasses = entityClasses;
    }

    public EntityManager getEntityManager(){
        return getEntityManagerFactory().createEntityManager();
    }

    protected EntityManagerFactory getEntityManagerFactory(){
        PersistenceUnitInfo persistenceUnitInfo = getPersistenceUnitInfo(getClass().getSimpleName());
        Map<String,Object> configuration = new HashMap<>();
        return new EntityManagerFactoryBuilderImpl(new PersistenceUnitInfoDescriptor(persistenceUnitInfo),configuration).build();
    }

    protected HibernatePersistenceUnitInfo getPersistenceUnitInfo(String name){
        return new HibernatePersistenceUnitInfo(name,getEntityClassNames(),getProperties());
    }

    protected Properties getProperties(){
        Properties properties = new Properties();
        properties.put("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.id.new_generator_mappings", false);
        properties.put("hibernate.hbm2ddl.auto","update");
        properties.put("hibernate.connection.datasource", getMysqlDataSource());
        return properties;
    }

    protected DataSource getMysqlDataSource(){
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setURL(DB_URL);
        mysqlDataSource.setUser(DB_USER_NAME);
        mysqlDataSource.setPassword(DB_PASSWORD);
        return mysqlDataSource;
    }

    protected List<String> getEntityClassNames(){
        return Arrays.asList(getEntities()).stream().map(Class::getName).collect(Collectors.toList());
    }

    protected Class[] getEntities(){
        return entityClasses;
    }
}
