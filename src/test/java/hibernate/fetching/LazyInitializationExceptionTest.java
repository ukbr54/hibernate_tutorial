package hibernate.fetching;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.LazyInitializationException;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class LazyInitializationExceptionTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class,
                PostComment.class
        };
    }

    @Test
    public void testNPlusOne(){
        String review = "Excellent!";

        doInJPA(entityManager -> {
            for(long i=1; i<4; i++){
                Post post = new Post();
                post.setId(i);
                post.setTitle(String.format("Post nr. %d",i));
                entityManager.persist(post);

                PostComment postComment = new PostComment();
                postComment.setId(i);
                postComment.setReview(review);
                postComment.setPost(post);
                entityManager.persist(postComment);
            }
        });

        List<PostComment> comments = null;
        EntityManager entityManager = null;
        EntityTransaction transaction = null;

        try{
            entityManager = entityManagerFactory().createEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();

            comments = entityManager.createQuery(
                    "SELECT pc FROM PostComment pc " +
                       "WHERE pc.review = :review",PostComment.class
            ).setParameter("review",review)
             .getResultList();

            transaction.commit();
        }catch (Throwable e){
            if(transaction != null && transaction.isActive()){
                transaction.rollback();
            }
            throw e;
        }finally {
            if(entityManager != null) entityManager.close();
        }

        try{
            for(PostComment comment : comments){
                LOGGER.info("The Post title is '{}'",comment.getPost().getTitle());
            }
        }catch (LazyInitializationException expected){
            LOGGER.error("Lazy Initialization Exception Demo");
            Assert.assertTrue(expected.getMessage().contains("could not initialize proxy"));
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post{

        @Id
        private Long id;

        private String title;

        public Post(String title) {
            this.title = title;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "PostComment")
    @Table(name = "post_comment")
    public static class PostComment{

        @Id
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        private Post post;

        private String review;

        public PostComment(String review) {
            this.review = review;
        }
    }
}
