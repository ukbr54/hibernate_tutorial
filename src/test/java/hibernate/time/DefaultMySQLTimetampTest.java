package hibernate.time;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractMySQLIntegrationTest;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class DefaultMySQLTimetampTest extends AbstractMySQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Book.class
        };
    }

    @Test
    public void test(){
        TimeZone defaultTimeZone = TimeZone.getDefault();
        try{
            TimeZone.setDefault(TimeZone.getTimeZone("US/Hawaii"));
            doInJPA(entityManager -> {
                Book book = new Book();

                book.setId(1L);
                book.setTitle("High-Performance Java Persistence");
                book.setCreatedBy("Vlad Mihalcea");
                book.setCreatedOn(new Timestamp(ZonedDateTime.of(
                        2016,8,25,11,23,46,0, ZoneId.of("UTC")).toInstant().toEpochMilli())
                );
                Assert.assertEquals(1472124226000L, book.getCreatedOn().getTime());
                entityManager.persist(book);
            });

            doInJPA(entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                session.doWork(connection ->{
                    try(Statement st = connection.createStatement()){
                        try(ResultSet rs = st.executeQuery(
                                "SELECT DATE_FORMAT(created_on, '%Y-%m-%d %H:%i:%s') " +
                                     "FROM Book"
                        )){
                            while(rs.next()){
                                String timestamp = rs.getString(1);
                                LOGGER.error("Expected {}, but got {}", expectedServerTimestamp(), timestamp);
                                if(!expectedServerTimestamp().equals(timestamp))  {
                                    LOGGER.error("Expected {}, but got {}", expectedServerTimestamp(), timestamp);
                                }
                            }
                        }
                    }
                });
            });

            doInJPA(entityManager -> {
                Book book = entityManager.find(Book.class, 1L);
                Assert.assertEquals(1472124226000L, book.getCreatedOn().getTime());
            });


        }finally {
            TimeZone.setDefault(defaultTimeZone);
        }
    }

    protected String expectedServerTimestamp() {
        return "2016-08-25 11:23:46";
    }
}
