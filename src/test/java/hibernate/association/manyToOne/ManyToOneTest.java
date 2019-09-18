package hibernate.association.manyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class ManyToOneTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class,
                PostComment.class
        };
    }

    @Test
    public void testLifeCycle(){
        doInJPA(entityManager -> {
            //One Insert Query
            Post post = new Post("First Post");
            entityManager.persist(post);
        });

        doInJPA(entityManager -> {
            //One Select Query
            Post post = entityManager.find(Post.class,1L);

            //One INSERT Query
            PostComment comment = new PostComment("My Review");
            comment.setPost(post);

            entityManager.persist(comment);
        });

        doInJPA(entityManager -> {
            //One SELECT Query
            PostComment comment = entityManager.find(PostComment.class, 2L);

            //One Update Query
            comment.setPost(null);
        });

        doInJPA(entityManager -> {
            //One SELECT Query
            PostComment comment = entityManager.getReference(PostComment.class, 2L);

            //One Delete Query
            entityManager.remove(comment);
        });
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Table(name = "post")
    @Entity(name = "Post")
    public static class Post{

        @Id
        @GeneratedValue
        private Long id;

        private String title;

        public Post(String title) {
            this.title = title;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Table(name = "post_comment")
    @Entity(name = "PostComment")
    public static class PostComment{

        @Id
        @GeneratedValue
        private Long id;

        private String review;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id")
        private Post post;

        public PostComment(String review) {
            this.review = review;
        }
    }
}
