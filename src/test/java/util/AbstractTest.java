package util;

import hibernate.bootstrap.HibernatePersistenceUnitInfo;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.provider.DataSourceProvider;
import util.provider.Database;
import util.transaction.JPATransactionFunction;
import util.transaction.JPATransactionVoidFunction;
import util.transaction.VoidCallable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */
public abstract class AbstractTest {

//    static {
//        Thread.currentThread().setName("Alice");
//    }

    protected final ExecutorService executorService =
            Executors.newSingleThreadExecutor(r -> {
                Thread bob = new Thread(r);
                bob.setName("Bob");
                return bob;
            }
    );

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private EntityManagerFactory emf;

    private SessionFactory sf;

    private List<Closeable> closeables = new ArrayList<>();

    @Before
    public void init(){
        if(nativeHibernateSessionFactoryBootstrap()){
            //sf = newSessionFactory();
        }else{
            emf = newEntityManagerFactory();
        }
        afterInit();
    }

    protected void afterInit(){}

    @After
    public void destroy() {
        if(nativeHibernateSessionFactoryBootstrap()) {
            if (sf != null) {  sf.close(); }
        } else {
            if (emf != null) {  emf.close(); }
        }
        for(Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOGGER.error("Failure", e);
            }
        }
        closeables.clear();
    }

    protected EntityManagerFactory newEntityManagerFactory(){
        PersistenceUnitInfo persistenceUnitInfo = persistenceUnitInfo(getClass().getSimpleName());
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(AvailableSettings.INTERCEPTOR, interceptor());
        Integrator integrator = integrator();
        if (integrator != null) {
            configuration.put("hibernate.integrator_provider", (IntegratorProvider) () -> Collections.singletonList(integrator));
        }
        EntityManagerFactoryBuilderImpl entityManagerFactoryBuilder =
                new EntityManagerFactoryBuilderImpl(new PersistenceUnitInfoDescriptor(persistenceUnitInfo), configuration);
        return entityManagerFactoryBuilder.build();

    }

    protected HibernatePersistenceUnitInfo persistenceUnitInfo(String name){
        HibernatePersistenceUnitInfo persistenceUnitInfo = new HibernatePersistenceUnitInfo(name,entityClassNames(),properties());
        String[] resources = resources();
        if(resources != null){
            persistenceUnitInfo.getMappingFileNames().addAll(Arrays.asList(resources));
        }
        return persistenceUnitInfo;
    }

    protected Properties properties(){
        Properties properties = new Properties();
        properties.put("hibernate.dialect", dataSourceProvider().hibernateDialect());

        //log settings
        properties.put("hibernate.hbm2ddl.auto", "update");

        //data source settings
        DataSource dataSource = newDataSource();
        if (dataSource != null) {
            properties.put("hibernate.connection.datasource", dataSource);
        }

        properties.put("hibernate.generate_statistics", Boolean.TRUE.toString());

        //properties.put("net.sf.ehcache.configurationResourceName", Thread.currentThread().getContextClassLoader().getResource("ehcache.xml").toString());
        //properties.put("hibernate.ejb.metamodel.population", "disabled");
        additionalProperties(properties);
        return properties;
    }

    protected DataSourceProxyType dataSourceProxyType() {
        return DataSourceProxyType.DATA_SOURCE_PROXY;
    }

    protected DataSource newDataSource(){
        DataSource dataSource =
                proxyDataSource()
                        ? dataSourceProxyType().dataSource(dataSourceProvider().dataSource())
                        : dataSourceProvider().dataSource();
        return dataSource;
//        if(connectionPooling()) {
//            HikariDataSource poolingDataSource = connectionPoolDataSource(dataSource);
//            closeables.add(poolingDataSource::close);
//            return poolingDataSource;
//        } else {
//            return dataSource;
//        }
    }

    protected boolean proxyDataSource() {  return true; }

    protected DataSourceProvider dataSourceProvider() { return database().dataSourceProvider(); }

    protected Database database() { return Database.MYSQL; }

    protected void additionalProperties(Properties properties) { }

    protected Class<?>[] entities() { return new Class[]{}; }

    protected List<String> entityClassNames() {
        return Arrays.asList(entities()).stream().map(Class::getName).collect(Collectors.toList());
    }

    protected String[] packages() { return null; }

    protected String[] resources() { return null; }

    protected Integrator integrator() { return null; }

    protected Interceptor interceptor() { return null; }

    public EntityManagerFactory entityManagerFactory() {
        return nativeHibernateSessionFactoryBootstrap() ? sf : emf;
    }

    protected boolean nativeHibernateSessionFactoryBootstrap() { return false; }

    protected void doInJPA(JPATransactionVoidFunction function){
        EntityManager entityManager = null;
        EntityTransaction txn = null;
        try{
            entityManager = entityManagerFactory().createEntityManager();
            function.beforeTransactionCompletion();
            txn = entityManager.getTransaction();
            txn.begin();
            function.accept(entityManager);
            if ( !txn.getRollbackOnly() ) {
                txn.commit();
            }
            else {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    LOGGER.error( "Rollback failure", e );
                }
            }
        } catch (Throwable t) {
            if ( txn != null && txn.isActive() ) {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    LOGGER.error( "Rollback failure", e );
                }
            }
            throw t;
        } finally {
            function.afterTransactionCompletion();
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    protected <T> T doInJPA(JPATransactionFunction<T> function) {
        T result = null;
        EntityManager entityManager = null;
        EntityTransaction txn = null;
        try {
            entityManager = entityManagerFactory().createEntityManager();
            function.beforeTransactionCompletion();
            txn = entityManager.getTransaction();
            txn.begin();
            result = function.apply(entityManager);
            if ( !txn.getRollbackOnly() ) {
                txn.commit();
            }
            else {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    LOGGER.error( "Rollback failure", e );
                }
            }
        } catch (Throwable t) {
            if ( txn != null && txn.isActive() ) {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    LOGGER.error( "Rollback failure", e );
                }
            }
            throw t;
        } finally {
            function.afterTransactionCompletion();
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return result;
    }

    protected void executeSync(VoidCallable callable) {
        executeSync(Collections.singleton(callable));
    }

    protected void executeSync(Collection<VoidCallable> callables) {
        try {
            List<Future<Void>> futures = executorService.invokeAll(callables);
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
