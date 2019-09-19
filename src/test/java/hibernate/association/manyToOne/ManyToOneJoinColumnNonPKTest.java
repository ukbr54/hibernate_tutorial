package hibernate.association.manyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

/**
 *  Natural Id Fetching: Just like with the JPA @Id annotation, the @NaturalId allows you to fetch the entity if you know the associated
 *  natural key.
 *
 *  However, when using a non-Primary Key association, the referencedColumnName should be used to instruct Hibernate which column should be
 *  used on the parent side to establish the many-to-one database relationship.
 */
public class ManyToOneJoinColumnNonPKTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Book.class,
                Publication.class
        };
    }

    @Test
    public void test(){
        doInJPA(entityManager -> {
            Book book = new Book();
            book.setTitle( "High-Performance Java Persistence" );
            book.setAuthor( "Vlad Mihalcea" );
            book.setIsbn( "978-9730228236" );
            entityManager.persist(book);

            Publication amazonUs = new Publication();
            amazonUs.setPublisher( "amazon.com" );
            amazonUs.setBook( book );
            amazonUs.setPriceCents( 4599 );
            amazonUs.setCurrency( "$" );
            entityManager.persist( amazonUs );

            Publication amazonUk = new Publication();
            amazonUk.setPublisher( "amazon.co.uk" );
            amazonUk.setBook( book );
            amazonUk.setPriceCents( 3545 );
            amazonUk.setCurrency( "&" );
            entityManager.persist( amazonUk );
        });

        doInJPA(entityManager -> {
            Publication publication = entityManager.createQuery(
                    "SELECT p " +
                    "FROM Publication p " +
                    "JOIN FETCH p.book b " +
                    "WHERE " +
                    " b.isbn = :isbn AND " +
                    " p.currency = :currency",Publication.class
            )
             .setParameter("isbn","978-9730228236")
             .setParameter("currency", "&")
             .getSingleResult();

            Assert.assertEquals("amazon.co.uk",publication.getPublisher());

            Assert.assertEquals("High-Performance Java Persistence",publication.getBook().getTitle());
        });
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Table(name = "books")
    @Entity(name = "Book")
    public static class Book implements Serializable {

        @Id
        @GeneratedValue
        private Long id;

        private String title;

        private String author;

        @NaturalId
        private String isbn;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Table(name = "publications")
    @Entity(name = "Publication")
    public static class Publication{

        @Id
        @GeneratedValue
        private Long id;

        private String publisher;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "isbn",referencedColumnName = "isbn")
        private Book book;

        @Column(name = "price_in_cents",nullable = false)
        private Integer priceCents;

        private String currency;
    }
}
