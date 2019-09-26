package hibernate.query.dto.projection.jpa;

import hibernate.forum.dto.PostDTO;
import hibernate.query.dto.projection.Post;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.Tuple;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
@Slf4j
public class JPADTOProjectionTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class
        };
    }

    @Override
    protected void afterInit() {
        doInJPA(entityManager -> {
            Post post = new Post();
            post.setId(1L);
            post.setTitle("High-Performance Java Persistence");
            post.setCreatedBy("Vlad Mihalcea");
            post.setCreatedOn(Timestamp.from(
                    LocalDateTime.of(2020,11,2,12,0,0).toInstant(ZoneOffset.UTC)
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
                   "SELECT new hibernate.forum.dto.PostDTO(" +
                   " p.id, p.title ) FROM Post p WHERE p.createdOn > :fromTimestamp", PostDTO.class
           ).setParameter("fromTimestamp",Timestamp.from(
                   LocalDate.of(2020,1,1).atStartOfDay().toInstant(ZoneOffset.UTC)
           )).getResultList();

           log.debug("Fetching the data from SQL: "+postDTOs.get(0).getTitle());

           Assert.assertEquals(1,postDTOs.size());
        });
    }

    @Test
    public void testTuple(){
        doInJPA(entityManager -> {
            List<Tuple> postDTOs = entityManager.createQuery(
                    "SELECT p.id as id, p.title as title FROM Post p " +
                       "WHERE p.createdOn > :fromTimestamp", Tuple.class
            ).setParameter("fromTimestamp", Timestamp.from(
                            LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
            )).getResultList();

            Assert.assertFalse(postDTOs.isEmpty());

            Tuple postDTO = postDTOs.get(0);
            Assert.assertEquals(1L, postDTO.get("id"));
            Assert.assertEquals("High-Performance Java Persistence", postDTO.get("title"));
        });
    }

    @Test
    public void testTupleNativeQuery(){
        doInJPA(entityManager -> {
            List<Tuple> postDTOs = entityManager.createNativeQuery(
                    "SELECT p.id AS id, p.title AS title FROM post p " +
                       "WHERE p.created_on > :fromTimestamp",Tuple.class
            ).setParameter("fromTimestamp",Timestamp.from(
                    LocalDate.of(2020,1,1).atStartOfDay().toInstant(ZoneOffset.UTC)
            )).getResultList();

            Assert.assertFalse(postDTOs.isEmpty());

            Tuple postDTO = postDTOs.get(0);
            Assert.assertEquals(1L, ((Number) postDTO.get("id")).longValue());
            Assert.assertEquals("High-Performance Java Persistence", postDTO.get("title"));
        });
    }

    @Test
    public void testConstructorResultNativeQuery(){
        doInJPA(entityManager -> {
            List<PostDTO> postDTOs = entityManager.createNamedQuery(
                    "PostDTOQuery"
            ).setParameter("timestamp",Timestamp.from(
                            LocalDate.of(2020,1,1).atStartOfDay().toInstant(ZoneOffset.UTC)
            )).getResultList();

            Assert.assertEquals(1, postDTOs.size());
        });
    }
}
