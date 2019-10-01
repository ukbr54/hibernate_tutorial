package util;

import util.provider.Database;

public class AbstractMySQLIntegrationTest extends AbstractTest{

    @Override
    protected Database database() {
        return Database.MYSQL;
    }
}
