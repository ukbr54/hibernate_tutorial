package hibernate.query.dto.projection.jpa;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

import hibernate.forum.dto.ClassImportIntegrator;
import hibernate.forum.dto.PostDTO;
import hibernate.query.dto.projection.Post;
import org.hibernate.integrator.spi.Integrator;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

/**
 * FOR REFERENCE: https://vladmihalcea.com/dto-projection-jpa-query/
 */
public class JPADTOProjectionImportTest extends AbstractTest {
    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class
        };
    }

    @Override
    protected Integrator integrator() {
        return new ClassImportIntegrator(Arrays.asList(PostDTO.class));
    }

    @Override
    protected void afterInit() {
        doInJPA(entityManager -> {
            Post post = new Post();
            post.setId(1L);
            post.setTitle("High-Performance Java Persistence");
            post.setCreatedBy("Vlad Mihalcea");
            post.setCreatedOn(Timestamp.from(
                    LocalDateTime.of(2020, 11, 2, 12, 0, 0).toInstant(ZoneOffset.UTC)
            ));
            post.setUpdatedBy("Vlad Mihalcea");
            post.setUpdatedOn(Timestamp.from(
                    LocalDateTime.now().toInstant(ZoneOffset.UTC)
            ));

            entityManager.persist(post);
        });
    }


    @Test
    public void testConstructorExpression(){
        doInJPA(entityManager -> {
            List<PostDTO> postDTOs = entityManager.createQuery(
                    "SELECT new PostDTO(p.id, p.title) FROM Post p " +
                       "WHERE p.createdOn > :fromTimestamp",PostDTO.class
            ).setParameter("fromTimestamp",Timestamp.from(
                    LocalDate.of(2020,1,1).atStartOfDay().toInstant(ZoneOffset.UTC)
            )).getResultList();

            Assert.assertEquals(1,postDTOs.size());
        });
    }
}
