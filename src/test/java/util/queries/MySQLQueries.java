package util.queries;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */
public class MySQLQueries implements Queries{

    public static final Queries INSTANCE = new MySQLQueries();

    @Override
    public String transactionId() {
        return "SELECT trx_id FROM information_schema.innodb_trx";
    }
}
