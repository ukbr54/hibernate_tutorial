package hibernate.association;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

/**
 * In Bidirectional OneToMany - No extra query are executed.
 *
 *
 *    MappedBy -  To make this association bidirectional, all we'll have to do is to define the referencing side. The inverse or the
 *    referencing side simply maps to the owning side.
 *    
 *    @JoinColumn - the owing side is usually defined on the "many" side of the relationship.It's usually the side which owns the
 *    foreign key.
 *
 */
public class BidirectionalOneToManyTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Post.class,
                PostComment.class
        };
    }

    @Test
    public void testLifeCycle(){
        doInJPA(entityManager -> {
            Post post = new Post("My First Post");

            post.addComment(new PostComment("My First review"));
            post.addComment(new PostComment("My second review"));
            post.addComment(new PostComment("My third review"));

            entityManager.persist(post);
        });

        doInJPA(entityManager -> {
            Post post = new Post();
            post.setTitle("High-Performance Java Persistence");

            PostComment comment = new PostComment();
            comment.setReview("JPA and Hibernate");
            post.addComment(comment);

            entityManager.persist(post);
        });

        doInJPA(entityManager -> {
            Post post = new Post("My Second Post");
            entityManager.persist(post);

            PostComment comment1 = new PostComment("My first review");
            post.addComment(comment1);
            PostComment comment2 = new PostComment("My second review");
            post.addComment(comment2);

            entityManager.persist(post);
            entityManager.flush();

            post.removeComment(comment1);
            entityManager.flush();
        });
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "post")
    @Table(name = "post")
    public static  class Post{

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
    @Entity(name = "PostComment")
    @Table(name = "post_comment")
    public static class PostComment{

        @Id
        @GeneratedValue
        private Long id;

        private String review;

        //The PostComment entity have a foreign key
        @ManyToOne(fetch = FetchType.LAZY)
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
            return Objects.equals(id, that.id) ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
