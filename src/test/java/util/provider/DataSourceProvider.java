package util.provider;

import util.queries.Queries;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */
public interface DataSourceProvider {

    enum IdentifierStrategy {
        IDENTITY,SEQUENCE;
    }

    String hibernateDialect();

    DataSource dataSource();

    Class<? extends DataSource> dataSourceClassName();

    Properties dataSourceProperties();

    String url();

    String username();

    String password();

    Database database();

    Queries queries();
}
