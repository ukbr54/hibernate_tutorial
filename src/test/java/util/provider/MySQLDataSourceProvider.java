package util.provider;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.hibernate.dialect.MySQL8Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ReflectionUtils;
import util.queries.MySQLQueries;
import util.queries.Queries;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */
public class MySQLDataSourceProvider implements DataSourceProvider{

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private boolean rewriteBatchedStatements = true;

    private boolean cachePrepStmts = false;

    private boolean useServerPrepStmts = false;

    private boolean useTimezone = false;

    private boolean useJDBCCompliantTimezoneShift = false;

    private boolean useLegacyDatetimeCode = true;

    private boolean useCursorFetch = false;

    public boolean isRewriteBatchedStatements() {
        return rewriteBatchedStatements;
    }

    public void setRewriteBatchedStatements(boolean rewriteBatchedStatements) {
        this.rewriteBatchedStatements = rewriteBatchedStatements;
    }

    public boolean isCachePrepStmts() {
        return cachePrepStmts;
    }

    public void setCachePrepStmts(boolean cachePrepStmts) {
        this.cachePrepStmts = cachePrepStmts;
    }

    public boolean isUseServerPrepStmts() {
        return useServerPrepStmts;
    }

    public void setUseServerPrepStmts(boolean useServerPrepStmts) {
        this.useServerPrepStmts = useServerPrepStmts;
    }

    public boolean isUseTimezone() {
        return useTimezone;
    }

    public void setUseTimezone(boolean useTimezone) {
        this.useTimezone = useTimezone;
    }

    public boolean isUseJDBCCompliantTimezoneShift() {
        return useJDBCCompliantTimezoneShift;
    }

    public void setUseJDBCCompliantTimezoneShift(boolean useJDBCCompliantTimezoneShift) {
        this.useJDBCCompliantTimezoneShift = useJDBCCompliantTimezoneShift;
    }

    public boolean isUseLegacyDatetimeCode() {
        return useLegacyDatetimeCode;
    }

    public void setUseLegacyDatetimeCode(boolean useLegacyDatetimeCode) {
        this.useLegacyDatetimeCode = useLegacyDatetimeCode;
    }

    public boolean isUseCursorFetch() {
        return useCursorFetch;
    }

    public void setUseCursorFetch(boolean useCursorFetch) {
        this.useCursorFetch = useCursorFetch;
    }

    @Override
    public String hibernateDialect() {
        return "org.hibernate.dialect.MySQL8Dialect";
    }

    @Override
    public DataSource dataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        String url = "jdbc:mysql://localhost/test_db?" + "rewriteBatchedStatements=" + rewriteBatchedStatements +
                     "&cachePrepStmts=" + cachePrepStmts + "&useServerPrepStmts=" + useServerPrepStmts;

        if(useCursorFetch) {
            url += "&useCursorFetch=true";
        }

        if(MySQL8Dialect.class.isAssignableFrom(ReflectionUtils.getClass(hibernateDialect()))){
            url += "&useTimezone=" + useTimezone +
                    "&useJDBCCompliantTimezoneShift=" + useJDBCCompliantTimezoneShift +
                    "&useLegacyDatetimeCode=" + useLegacyDatetimeCode;
        }
        LOGGER.info("Url: "+url);
        dataSource.setURL(url);
        dataSource.setUser(username());
        dataSource.setPassword(password());
        return dataSource;
    }

    @Override
    public Class<? extends DataSource> dataSourceClassName() {
        return MysqlDataSource.class;
    }

    @Override
    public Properties dataSourceProperties() {
        Properties properties = new Properties();
        properties.setProperty("url",url());
        return properties;
    }

    @Override
    public String url() {
        return "jdbc:mysql://localhost:3306/test_db?user=root&password=root";
    }

    @Override
    public String username() {
        return "root";
    }

    @Override
    public String password() {
        return "test";
    }

    @Override
    public Database database() {
        return Database.MYSQL;
    }

    @Override
    public Queries queries() {
        return MySQLQueries.INSTANCE;
    }


    @Override
    public String toString() {
        return "MySQLDataSourceProvider{" +
                "rewriteBatchedStatements=" + rewriteBatchedStatements +
                ", cachePrepStmts=" + cachePrepStmts +
                ", useServerPrepStmts=" + useServerPrepStmts +
                ", useTimezone=" + useTimezone +
                ", useJDBCCompliantTimezoneShift=" + useJDBCCompliantTimezoneShift +
                ", useLegacyDatetimeCode=" + useLegacyDatetimeCode +
                ", useCursorFetch=" + useCursorFetch +
                '}';
    }
}
