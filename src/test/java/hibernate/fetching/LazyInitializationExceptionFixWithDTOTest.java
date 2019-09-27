package hibernate.fetching;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class LazyInitializationExceptionFixWithDTOTest extends AbstractTest {

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
            for (long i = 1; i < 4; i++) {
                Post post = new Post();
                post.setId(i);
                post.setTitle(String.format("Post nr. %d", i));
                entityManager.persist(post);

                PostComment comment = new PostComment();
                comment.setId(i);
                comment.setPost(post);
                comment.setReview(review);
                entityManager.persist(comment);
            }
        });

        List<PostCommentDTO> comments = doInJPA(entityManager -> {
            return entityManager.createQuery(
                    "SELECT new hibernate.fetching.PostCommentDTO(pc.id, pc.review, p.title) " +
                       "FROM PostComment pc JOIN pc.post p WHERE pc.review = :review",PostCommentDTO.class
            ).setParameter("review",review)
             .getResultList();
        });

        for(PostCommentDTO comment : comments){
            LOGGER.info("The post title is '{}'", comment.getTitle());
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
