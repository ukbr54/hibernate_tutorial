package hibernate.fetching;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class LazyInitializationExceptionFixTestUsingFetchJoin extends AbstractTest {

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
            for(long i=0; i<4; i++){
                Post post = new Post();
                post.setId(i);
                post.setTitle(String.format("Post nr. %d",i));
                entityManager.persist(post);

                PostComment comment = new PostComment();
                comment.setId(i);
                comment.setReview(review);
                comment.setPost(post);

                entityManager.persist(comment);
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
                    "SELECT pc FROM PostComment pc JOIN FETCH pc.post "+
                       "WHERE pc.review = :review",PostComment.class
            ).setParameter("review",review)
             .getResultList();

            transaction.commit();
        }catch (Throwable e){
            if(Objects.nonNull(transaction) && transaction.isActive()){
                transaction.rollback();
            }
            throw  e;
        }finally {
            if(Objects.nonNull(entityManager)) entityManager.close();
        }

        for(PostComment comment : comments) {
            LOGGER.info("The post title is '{}'", comment.getPost().getTitle());
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
