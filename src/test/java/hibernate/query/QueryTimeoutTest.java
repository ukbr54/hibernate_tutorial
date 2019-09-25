package hibernate.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class QueryTimeoutTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class
        };
    }

    @Test
    public void testJPQLTimeoutHint(){
        doInJPA(entityManager -> {
            for (int i = 0; i < 5; i++) {
                entityManager.persist(
                        new Post().setTitle(String.format("Hibernate User Guide, Chapter %d", i + 1))
                );
            }

            for (int i = 0; i < 5; i++) {
                entityManager.persist(
                        new Post().setTitle(String.format("%d Hibernate Tips", (i + 1) * 5))
                );
            }

            for (int i = 0; i < 5; i++) {
                entityManager.persist(
                        new Post().setTitle(String.format("%d Tips to master Hibernate", (i + 1) * 10))
                );
            }
        });

        doInJPA(entityManager -> {
            List<Post> posts = entityManager.createQuery(
                     "SELECT p FROM Post p " +
                     "WHERE lower(p.title) LIKE lower(:titlePattern)",Post.class
            ).setParameter("titlePattern","%Hibernate%")
             .setHint("javax.persistent.query.timeout",50)
             .getResultList();

            Assert.assertEquals(15,posts.size());
        });

        doInJPA(entityManager -> {
            List<Post> posts = entityManager
                    .createQuery("select p " +
                                    "from Post p " +
                                    "where lower(p.title) like lower(:titlePattern)", Post.class)
                    .setParameter("titlePattern", "%Hibernate%")
                    .setHint("org.hibernate.timeout", 1)
                    .getResultList();

            Assert.assertEquals(15, posts.size());
        });

        doInJPA(entityManager -> {
            List<Post> posts = entityManager
                    .createQuery("select p " +
                                    "from Post p " +
                                    "where lower(p.title) like lower(:titlePattern)", Post.class)
                    .setParameter("titlePattern", "%Hibernate%")
                    .unwrap(org.hibernate.query.Query.class)
                    .setTimeout(1)
                    .getResultList();

            Assert.assertEquals(15, posts.size());
        });
    }

    @Test
    public void testJPATimeout(){
        doInJPA(entityManager -> {
            try{
                List<Tuple> result = entityManager.createNativeQuery(
                      "SELECT 1 FROM pg_sleep(2)",Tuple.class
                ).setHint("javax.persistence.query.timeout",(int) TimeUnit.SECONDS.toMillis(1))
                 .getResultList();

                Assert.fail("Timeout failure Timeout");
            }catch (Exception e){

            }
        });
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post{

        @Id
        @GeneratedValue
        private Integer id;

        private String title;

        public Post setTitle(String title){
            this.title = title;
            return this;
        }
    }
}
