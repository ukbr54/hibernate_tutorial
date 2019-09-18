package hibernate.association.onetomany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

/**
 * The helper method for the child entity removal relies on the underlying child object equality for
 * matching the collection entry that needs to be removed.
 * If the application developer doesn’t choose to override the default equals and hashCode methods,
 * the java.lang.Object identity-based equality is going to be used. The problem with this approach
 * is that the application developer must supply a child entity object reference that’s contained in the
 * current child collection.
 *
 * Sometimes child entities are loaded in one web request and saved in a HttpSession or a Stateful
 * Enterprise Java Bean. Once the Persistence Context, which loaded the child entity is closed, the
 * entity becomes detached. If the child entity is sent for removal into a new web request, the child
 * entity must be reattached or merged into the current Persistence Context. This way, if the parent
 * entity is loaded along with its child entities, the removal operation will work properly since the
 * removing child entity is already managed and contained in the children collection.
 * If the entity hasn’t changed, reattaching this child entity is redundant and so the equals and the
 * hashCode methods must be overridden to express equality in terms of a unique business key.
 */
public class BidirectionalOneToManyEqualityBasedEntityRemoval extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class,
                PostComment.class
        };
    }

    @Test
    public void testLifeCycle(){
        final PostComment comment = doInJPA(entityManager -> {
            Post post = new Post("First Post");
            entityManager.persist(post);

            PostComment comment1 = new PostComment("First Review");
            comment1.setCreatedBy("Test");
            post.addComment(comment1);

            PostComment comment2 = new PostComment("Second Review");
            comment2.setCreatedBy("Test");
            post.addComment(comment2);

            entityManager.persist(post);
            entityManager.flush();

            return comment1;
        });

        doInJPA(entityManager -> {
            Post post = entityManager.find(Post.class, 1L);

            post.removeComment(comment);
            Assert.assertEquals(1, post.getComments().size());
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

        @OneToMany(mappedBy = "post",cascade = CascadeType.ALL,orphanRemoval = true)
        private List<PostComment> comments = new ArrayList<>();

        public Post(String title) {
            this.title = title;
        }

        public void addComment(PostComment comment){
            comments.add(comment);
            comment.setPost(this);
        }

        public void removeComment(PostComment comment){
            comments.remove(comment);
            comment.setPost(null);
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

        private String createdBy;

        @Temporal(TemporalType.TIMESTAMP)
        private Date createdOn = new Date();

        @ManyToOne
        @JoinColumn(name = "post_id")
        private Post post;

        public PostComment(String review) {
            this.review = review;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PostComment that = (PostComment) o;
            return createdBy.equals(that.createdBy) &&
                    createdOn.equals(that.createdOn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(createdBy, createdOn);
        }
    }
}
