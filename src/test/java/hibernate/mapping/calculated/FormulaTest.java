package hibernate.mapping.calculated;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * CREATE DEFINER=`root`@`localhost` FUNCTION `month_diff`( dateColA date, dateColB date ) RETURNS int(11)
 * BEGIN
 *    DECLARE diff INT;
 *    SET diff =  TIMESTAMPDIFF(MONTH, dateColA, dateColB);
 *    RETURN diff;
 * END
 *
 *
 * TO CALL FUNCTION: SELECT month_diff('2015-05-05','2015-06-15')
 */

public class FormulaTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Account.class,
                User.class
        };
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            User user = new User();
            user.setId(1L);
            user.setFirstName("John");
            user.setFirstName("Doe");

            entityManager.persist(user);

            Account account = new Account(
                    1L,
                    user,
                    "ABC123",
                    12345L,
                    6.7,
                    Timestamp.valueOf(
                            LocalDateTime.now().minusMonths(3)
                    )
            );
            entityManager.persist(account);
        });
        doInJPA(entityManager -> {
            Account account = entityManager.find(Account.class, 1L);

            Assert.assertEquals(123.45D, account.getDollars(), 0.001);
            Assert.assertEquals(207L, account.getInterestCents());
            Assert.assertEquals(2.07D, account.getInterestDollars(), 0.001);
        });
    }

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

        @Formula("cents / 100")
        private double dollars;

        @Formula(
                "round((interestRate / 100) * cents * " +
                "month_diff(createdOn,now()) / 12)"
        )
        private long interestCents;

        @Formula(
                "round((interestRate / 100) * cents * " +
                "month_diff(createdOn,now()) / 12) / 100"
        )
        private double interestDollars;

        public Account(Long id, User owner, String iban, long cents, double interestRate, Timestamp createdOn) {
            this.id = id;
            this.owner = owner;
            this.iban = iban;
            this.cents = cents;
            this.interestRate = interestRate;
            this.createdOn = createdOn;
        }

        @Transient
        public double getDollars() {
            return dollars;
        }

        @Transient
        public long getInterestCents() {
            return interestCents;
        }

        @Transient
        public double getInterestDollars() {
            return interestDollars;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public User getOwner() {
            return owner;
        }

        public void setOwner(User owner) {
            this.owner = owner;
        }

        public String getIban() {
            return iban;
        }

        public void setIban(String iban) {
            this.iban = iban;
        }

        public long getCents() {
            return cents;
        }

        public void setCents(long cents) {
            this.cents = cents;
        }

        public double getInterestRate() {
            return interestRate;
        }

        public void setInterestRate(double interestRate) {
            this.interestRate = interestRate;
        }

        public Timestamp getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(Timestamp createdOn) {
            this.createdOn = createdOn;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "User")
    @Table(name = "user")
    public static class User {

        @Id
        private Long id;

        private String firstName;

        private String lastName;

    }
}
