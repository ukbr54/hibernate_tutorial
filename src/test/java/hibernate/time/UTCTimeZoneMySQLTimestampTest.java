package hibernate.time;

import org.hibernate.cfg.AvailableSettings;
import util.provider.DataSourceProvider;
import util.provider.MySQLDataSourceProvider;

import java.util.Properties;

public class UTCTimeZoneMySQLTimestampTest extends DefaultMySQLTimetampTest{

    @Override
    protected Properties properties() {
        Properties properties = super.properties();
        properties.setProperty(AvailableSettings.JDBC_TIME_ZONE,"UTC");
        return properties;
    }

    @Override
    protected DataSourceProvider dataSourceProvider() {
        MySQLDataSourceProvider provider = (MySQLDataSourceProvider)super.dataSourceProvider();
        provider.setUseLegacyDatetimeCode(false);
        return provider;
    }

    @Override
    protected String expectedServerTimestamp() {
        return "2016-08-25 11:23:46";
    }
}
