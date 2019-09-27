package hibernate.mapping.embeddable;

import hibernate.mapping.LoggedUserTest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class EmbeddableEntityListenerTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
                Post.class,
                Tag.class
        };
    }

    @Test
    public void test(){
        LoggedUser.logIn("Alice");

        doInJPA(entityManager -> {
            Tag jdbc = new Tag();
            jdbc.setName("JDBC");
            entityManager.persist(jdbc);

            Tag hibernate = new Tag();
            hibernate.setName("Hibernate");
            entityManager.persist(hibernate);

            Tag jooq = new Tag();
            jooq.setName("JOOQ");
            entityManager.persist(jooq);
        });

        doInJPA(entityManager -> {
            Post post = new Post();
            post.setId(1L);
            post.setTitle("High-Performance Java Persistence, 1st Edition");

            post.getTags().add(entityManager.find(Tag.class,"JDBC"));
            post.getTags().add(entityManager.find(Tag.class,"Hibernate"));
            post.getTags().add(entityManager.find(Tag.class,"JOOQ"));

            entityManager.persist(post);
        });

        doInJPA(entityManager -> {
            Post post = entityManager.find(Post.class, 1L);

            LOGGER.info(String.format("Data: %s",post.toString()));

            post.setTitle("High-Performance Java Persistence, 2nd Edition");

            entityManager.flush();
            
            LOGGER.info(String.format("Data: %s",post.toString()));
        });

        LoggedUser.logOut();
    }

    public static class LoggedUser{
        private static final ThreadLocal<String> userHolder = new ThreadLocal<>();

        public static void logIn(String user){
            userHolder.set(user);
        }

        public static void logOut(){
            userHolder.remove();
        }

        public static String get(){
            return userHolder.get();
        }
    }

    @Setter
    @Getter
    @ToString
    @Embeddable
    public static class Audit{

        @Column(name = "created_on")
        private LocalDateTime createdOn;

        @Column(name = "created_by")
        private String createdBy;

        @Column(name = "updated_on")
        private LocalDateTime updatedOn;

        @Column(name = "updated_by")
        private String updatedBy;

        @PrePersist
        public void prePersist(){
            createdOn = LocalDateTime.now();
            createdBy = LoggedUser.get();
        }

        @PreUpdate
        public void preUpdate(){
            updatedOn = LocalDateTime.now();
            updatedBy = LoggedUser.get();
        }
    }

    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post{

        @Id
        private Long id;

        private String title;

        @Embedded
        private Audit audit = new Audit();

        @ManyToMany
        @JoinTable(
                name = "post_tag",
                joinColumns = @JoinColumn(name = "post_id"),
                inverseJoinColumns = @JoinColumn(name = "tag_id")
        )
        private List<Tag> tags = new ArrayList<>();
    }

    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @Entity(name = "Tag")
    @Table(name = "tag")
    public static class Tag{

        @Id
        private String name;

        @Embedded
        private Audit audit = new Audit();
    }
}
