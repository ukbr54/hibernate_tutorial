package hibernate.mapping.calculated;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Now, these properties are going to be calculated on every method call, and that might not be very efficient if you need to call a
 * given method multiple times.
 *
 * To overcome this issue, you can simply calculate these values upon loading the entity from the database (assuming the cents and the
 * interestRate are not going to be modified since, otherwise, the saving Account will be deactivated).
 */

public class JpaCalculatedTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Account.class,
                User.class
        };
    }

    @Test
    public void test(){
        doInJPA(entityManager -> {
            User user = new User();
            user.setId(1L);
            user.setFirstName("John");
            user.setLastName("Doe");

            entityManager.persist(user);

            Account account =
              new Account(1L,user,"ABC123",12345L,6.7,Timestamp.valueOf(LocalDateTime.now().minusMonths(3)));

            entityManager.persist(account);
        });

        doInJPA(entityManager -> {
            final Account account = entityManager.find(Account.class, 1L);

            LOGGER.info(String.format("Get Dollar: %1$,.2f",account.getDollars()));
            LOGGER.info(String.format("Get Interest Cents: %d",account.getInterestCents()));
            LOGGER.info(String.format("Get Interest Dollar: %1$,.2f",account.getInterestDollars()));

            Assert.assertEquals(123.45D, account.getDollars(), 0.001);
            Assert.assertEquals(207L, account.getInterestCents());
            Assert.assertEquals(2.07D, account.getInterestDollars(), 0.001);
        });
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "Account")
    @Table(name = "account")
    public static class Account{

        @Id
        private Long id;

        @ManyToOne
        private User owner;

        private String iban;

        private long cents;

        private double interestRate;

        private Timestamp createdOn;

        public Account(Long id, User owner, String iban, long cents, double interestRate, Timestamp createdOn) {
            this.id = id;
            this.owner = owner;
            this.iban = iban;
            this.cents = cents;
            this.interestRate = interestRate;
            this.createdOn = createdOn;
        }

        @Transient
        public double getDollars(){
            return cents / 100D;
        }

        @Transient
        public long getInterestCents(){
            long months = createdOn.toLocalDateTime().until(LocalDateTime.now(), ChronoUnit.MONTHS);
            double interestUnrounded = ((interestRate / 100D) * cents * months) / 12;
            return BigDecimal.valueOf(interestUnrounded).setScale(0,BigDecimal.ROUND_HALF_EVEN).longValue();
        }

        @Transient
        public double getInterestDollars(){
            return getInterestCents() / 100D;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "User")
    @Table(name = "user")
    public static class User{

        @Id
        private Long id;

        private String firstName;

        private String lastName;


    }
}
