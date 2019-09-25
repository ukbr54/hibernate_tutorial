package hibernate.query;

import javafx.geometry.Pos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

/**
 * REFER THIS ARTICLE FOR THEORY: https://vladmihalcea.com/improve-statement-caching-efficiency-in-clause-parameter-padding/
 */
public class InQueryTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class
        };
    }

    @Override
    protected void additionalProperties(Properties properties) {
        properties.put("hibernate.jdbc.batch_size", "50");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.query.in_clause_parameter_padding", "true");
    }

    @Test
    public void testPadding(){
        doInJPA(entityManager -> {
            for(int i=0; i<=15; i++){
                Post post = new Post();
                post.setId(i);
                post.setTitle(String.format("Post no. %d",i));

                entityManager.persist(post);
            }
        });



        doInJPA(entityManager -> {
            Assert.assertEquals(3,getPOstByIds(entityManager,1,2,3).size());
            Assert.assertEquals(4,getPOstByIds(entityManager,1,2,3,4).size());
            Assert.assertEquals(5,getPOstByIds(entityManager,1,2,3,4,5).size());
            Assert.assertEquals(6,getPOstByIds(entityManager,1,2,3,4,5,6).size());
        });
    }

    List<Post> getPOstByIds(EntityManager entityManager,Integer ... ids){
        return entityManager.createQuery(
                "SELECT p FROM Post p " +
                   "WHERE p.id IN :ids", Post.class
        ).setParameter("ids", Arrays.asList(ids))
         .getResultList();
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post{

        @Id
        private Integer id;

        private String title;

        public Post(String title) {
            this.title = title;
        }
    }
}
