package hibernate.association.manytomany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.junit.Ignore;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class BidirectionalManyToManyListTest extends AbstractTest {

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
            //1 INSERT QUERY in post
            Post post1 = new Post("JPA with Hibernate");
            //1 INSERT QUERY in post
            Post post2 = new Post("Native Hibernate");

            //1 INSERT QUERY in tag
            Tag tag1 = new Tag("Java");
            //1 INSERT QUERY in tag
            Tag tag2 = new Tag("Hibernate");

            //1 INSERT QUERY in post_tag
            post1.addTag(tag1);
            //1 INSERT QUERY in post_tag
            post1.addTag(tag2);

            ////1 INSERT QUERY in post_tag
            post2.addTag(tag1);

            entityManager.persist(post1);
            entityManager.persist(post2);

            entityManager.flush();

            //1 DELETE QUERY post
            //1 INSERT QUERY post_tag
            post1.removeTag(tag1);
        });
    }

    @Test
    public void testRemovePost(){
        final Long postId = doInJPA(entityManager -> {
            Post post1 = new Post("JPA with Hibernate");
            Post post2 = new Post("Native Hibernate");

            Tag tag1 = new Tag("Java");
            Tag tag2 = new Tag("Hibernate");

            post1.addTag(tag1);
            post1.addTag(tag2);

            post2.addTag(tag1);

            entityManager.persist(post1);
            entityManager.persist(post2);

            return post1.id;
        });

        doInJPA(entityManager -> {
            LOGGER.info("Remove Post");
            //1 SELECT QUERY FROM post
            Post post = entityManager.find(Post.class, postId);

            //1 DELETE query from post_tag
            //1 Delete query from post
            entityManager.remove(post);
        });
    }

    @Ignore
    @Test
    public void testRemoveTag(){
        final Long tagId = doInJPA(entityManager -> {
            Post post1 = new Post("JPA with Hibernate");
            Post post2 = new Post("Native Hibernate");

            Tag tag1 = new Tag("Java");
            Tag tag2 = new Tag("Hibernate");

            post1.addTag(tag1);
            post1.addTag(tag2);

            post2.addTag(tag1);

            entityManager.persist(post1);
            entityManager.persist(post2);

            return tag1.id;
        });
        doInJPA(entityManager -> {
            LOGGER.info("Remove Tag");
            Tag tag1 = entityManager.find(Tag.class, tagId);

            entityManager.remove(tag1);
        });
    }

    @Test
    public void testShuffle(){
        final Long postId = doInJPA(entityManager -> {
            Post post1 = new Post("JPA with Hibernate");
            Post post2 = new Post("Native Hibernate");

            Tag tag1 = new Tag("Java");
            Tag tag2 = new Tag("Hibernate");

            post1.addTag(tag1);
            post1.addTag(tag2);

            post2.addTag(tag1);

            entityManager.persist(post1);
            entityManager.persist(post2);

            return post1.id;
        });

        doInJPA(entityManager -> {
            LOGGER.info("Shuffle");
            Tag tag1 = new Tag("Java");

            //1 SELECT Query
            Post post1 = entityManager.createQuery(
                    "SELECT p FROM Post p" +
                    " JOIN FETCH p.tags " +
                    " WHERE p.id = :id",Post.class
            ).setParameter("id",postId)
             .getSingleResult();

            //Delete from post_tag
            //insert into post_tag
            post1.removeTag(tag1);
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
            return id.equals(post.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Table(name = "tag")
    @Entity(name = "Tag")
    public static class Tag{

        @Id
        @GeneratedValue
        private Long id;

        @NaturalId
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
