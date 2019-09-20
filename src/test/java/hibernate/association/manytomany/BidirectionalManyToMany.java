package hibernate.association.manytomany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.*;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

/**
 * The mappedBy attribute of the posts association in the Tag entity marks that, in this bidirectional relationship, the Post entity owns
 * the association. This is needed since only one side can own a relationship, and changes are only propagated to the database from
 * this particular side.
 */
public class BidirectionalManyToMany extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class,
                Tag.class
        };
    }

    @Test
    public void testLifeCycle(){
        doInJPA(entityManager -> {
            //1 Query INSERT
            Post post1 = new Post("JPA with Hibernate");
            //1 Query INSERT
            Post post2 = new Post("Native Hibernate");

            //1 Query INSERT
            Tag tag1 = new Tag("Java");
            //1 Query INSERT
            Tag tag2 = new Tag("Hibernate");

            //1 Query INSERT
            post1.addTag(tag1);
            //1 Query INSERT
            post1.addTag(tag2);

            //1 Query INSERT
            post2.addTag(tag1);

            entityManager.persist(post1);
            entityManager.persist(post2);

            entityManager.flush();

            //1 Delete Query
            //1 INSERT QUERY
            //So, instead of deleting just one post_tag entry, Hibernate removes all post_tag rows associated to the given post_id and
            // reinserts the remaining ones back afterward.
            post1.removeTag(tag1);
        });
    }

    @Test
    public void testRemove(){
       Long postId =  doInJPA(entityManager -> {
            Post post1 = new Post("JPA With Hibernate");
            Post post2 = new Post("Native Hibernate");

            Tag tag1 = new Tag("Java");
            Tag tag2 = new Tag("Hibernate");

            post1.addTag(tag1);
            post1.addTag(tag2);

            post2.addTag(tag2);

            entityManager.persist(post1);
            entityManager.persist(post2);

            return post1.id;
        });

       doInJPA(entityManager -> {
           LOGGER.info("Remove");
           //1 SELECT Statement AND 2 DELETE Query - one for removing the post entity and one for removing relation from post_tag
           Post post1 = entityManager.find(Post.class,postId);
           entityManager.remove(post1);
       });
    }

    @Test
    public void testShuffle(){
        Long postId = doInJPA(entityManager -> {
            Post post1 = new Post("JPA With Hibernate");
            Post post2 = new Post("Native Hibernate");

            Tag tag1 = new Tag("Java");
            Tag tag2 = new Tag("Hibernate");

            post1.addTag(tag1);
            post1.addTag(tag2);

            post2.addTag(tag2);

            entityManager.persist(post1);
            entityManager.persist(post2);

            return post1.id;
        });

        doInJPA(entityManager -> {
            LOGGER.info("Shuffle");
            //1 SELECT QUERY
            Post post = entityManager.find(Post.class, postId);

            //If we are using child entity then only hibernate generating the SELECT Query with inner join for child entity
            //1 Delete Query in post_tag remove post
            // 2 Insert Query in post_tag for that post.
            post.getTags().sort(Collections.reverseOrder(Comparator.comparing(Tag::getId)));
        });
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "Post")
    @Table(name = "posts")
    public static class Post{

        @Id
        @GeneratedValue
        private Long id;

        private String title;

        @ManyToMany(cascade = {CascadeType.MERGE,CascadeType.PERSIST})
        @JoinTable(
                name = "post_tag",
                joinColumns = @JoinColumn(name = "post_id"),
                inverseJoinColumns = @JoinColumn(name = "tag_id")
        )
        private List<Tag> tags = new ArrayList<>();

        public Post(String title) {
            this.title = title;
        }

        public void addTag(Tag tag){
            tags.add(tag);
            tag.getPosts().add(this);
        }

        public void removeTag(Tag tag){
            tags.remove(tag);
            tag.getPosts().remove(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Post post = (Post) o;
            return title.equals(post.title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title);
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Table(name = "tags")
    @Entity(name = "tags")
    public static class Tag{

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @ManyToMany(mappedBy = "tags")
        private List<Post> posts = new ArrayList<>();

        public Tag(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tag tag = (Tag) o;
            return name.equals(tag.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
