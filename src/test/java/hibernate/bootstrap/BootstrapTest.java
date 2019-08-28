package hibernate.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.transaction.JPATransactionVoidFunction;

import javax.persistence.*;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */
public class BootstrapTest extends AbstractJPAProgrammaticBootstrapTest{

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
           Post.class
        };
    }

    @Setter
    @Getter
    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post{

        @Id
        private Long id;

        private String title;
    }

    @Test
    public void test(){
        doInJPA(entityManager -> {
            for (long id = 1; id <= 3; id++) {
                Post post = new Post();
                post.setId(id);
                post.setTitle(String.format("High-Performance Java Persistence, Part %d", id));
                entityManager.persist(post);
            }
        });
    }

    protected void doInJPA(JPATransactionVoidFunction function) {
        EntityManager entityManager = null;
        EntityTransaction txn = null;
        try {
            entityManager = entityManagerFactory().createEntityManager();
            function.beforeTransactionCompletion();
            txn = entityManager.getTransaction();
            txn.begin();
            function.accept(entityManager);
            if ( !txn.getRollbackOnly() ) {
                txn.commit();
            }
            else {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    LOGGER.error( "Rollback failure", e );
                }
            }
        } catch (Throwable t) {
            if ( txn != null && txn.isActive() ) {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    LOGGER.error( "Rollback failure", e );
                }
            }
            throw t;
        } finally {
            function.afterTransactionCompletion();
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }
}
