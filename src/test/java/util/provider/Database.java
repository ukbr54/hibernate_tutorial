package util.provider;

import util.ReflectionUtils;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */
public enum Database {
    MYSQL(MySQLDataSourceProvider.class);

    private Class<? extends DataSourceProvider> dataSourceProviderClass;

    Database(Class<? extends DataSourceProvider> dataSourceProviderClass) {
        this.dataSourceProviderClass = dataSourceProviderClass;
    }

    public DataSourceProvider dataSourceProvider(){
        return ReflectionUtils.newInstance(dataSourceProviderClass.getName());
    }
}
