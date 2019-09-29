package hibernate.mapping.calculated;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.Date;

public class FormulaCurrentDateTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Event.class,
        };
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            Event event = new Event();
            event.setId(1L);

            entityManager.persist(event);
            entityManager.flush();

            entityManager.refresh(event);

            Assert.assertNotNull(event.getCreatedOn());
        });
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "Event")
    @Table(name = "event")
    public static class Event{

        @Id
        private Long id;

        @Formula("SELECT CURRENT_DATE")
        @Temporal(TemporalType.TIMESTAMP)
        private Date createdOn;
    }
}
