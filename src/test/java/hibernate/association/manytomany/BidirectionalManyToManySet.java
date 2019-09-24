package hibernate.association.manytomany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.annotations.NaturalId;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class BidirectionalManyToManySet extends AbstractTest {

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
            //1 INSERT QUERY IN post
            Post post1 = new Post("JPA with Hibernate");
            //1 INSERT QUERY IN post
            Post post2 = new Post("Native Hibernate");

            //1 INSERT QUERY IN tag
            Tag tag1 = new Tag("Java");
            //1 INSERT QUERY IN tag
            Tag tag2 = new Tag("Hibernate");

            //1 INSERT QUERY IN post_tag
            post1.addTag(tag1);
            //1 INSERT QUERY IN post_tag
            post1.addTag(tag2);

            //1 INSERT QUERY IN post_tag
            post2.addTag(tag1);

            entityManager.persist(post1);
            entityManager.persist(post2);

            entityManager.flush();

            //1 Delete query in post_tag
            post1.removeTag(tag1);
        });
    }

    @Test
    public void testRemove() {
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
            LOGGER.info("Remove");
            // 1 SELECT QUERY from post
            Post post1 = entityManager.find(Post.class, postId);

            //delete query from post_tag
            //delete query from post
            entityManager.remove(post1);
        });
    }

    @Test
    public void testShuffle() {
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

            //1 SELECT QUERY from post
            Post post1 = entityManager
                    .createQuery(
                            "select p " +
                                    "from Post p " +
                                    "join fetch p.tags " +
                                    "where p.id = :id", Post.class)
                    .setParameter( "id", postId )
                    .getSingleResult();

            //1 SELECT QUERY with inner join post_tag and post
            Tag javaTag = entityManager.unwrap(Session.class)
                    .bySimpleNaturalId(Tag.class)
                    .getReference("Java");

            //1 Delete query from post_tag
            post1.removeTag(javaTag);
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

        @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
        @JoinTable(
                name = "post_tag",
                joinColumns = @JoinColumn(name = "post_id"),
                inverseJoinColumns = @JoinColumn(name = "tag_id")
        )
        private Set<Tag> tags = new HashSet<>();

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
            if (!(o instanceof Post)) return false;
            Post other = (Post) o;
            return id != null && id.equals(other.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "Tag")
    @Table(name = "tags")
    public static class Tag{

        @Id
        @GeneratedValue
        private Long id;

        @NaturalId
        private String name;

        @ManyToMany(mappedBy = "tags")
        private Set<Post> posts = new HashSet<>();

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
