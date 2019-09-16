package hibernate.association;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

/**
 * @JoinColumn  annotation helps Hibernate to figure out that there is a post_id Foreign Key column in the post_comment table that defines
 * this association.
 *
 *
 */

public class UnidirectionalOneToManyTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
           Post.class,
           PostComment.class
        };
    }

    @Test
    public void testLifeCycle(){
        doInJPA(entityManager -> {
            Post post = new Post("First Post");

            post.getComments().add(new PostComment("My first review"));
            post.getComments().add(new PostComment("My second review"));
            post.getComments().add(new PostComment("My third review"));

            entityManager.persist(post);
            entityManager.flush();

            LOGGER.info("Remove tail");
            post.getComments().remove(2);
            entityManager.flush();
            LOGGER.info("Remove head");
            post.getComments().remove(0);
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
        private Long id;

        private String title;

        @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
        @JoinColumn(name = "post_id")
        private List<PostComment> comments = new ArrayList<>();

        public Post(String title) {
            this.title = title;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "PostComment")
    @Table(name = "post_comment")
    public class PostComment{
        @Id
        @GeneratedValue
        private Long id;

        private String review;

        public PostComment(String review) {
            this.review = review;
        }
    }
}
