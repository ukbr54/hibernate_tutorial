package hibernate.association.manytomany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.annotations.NaturalId;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Ujjwal Gupta on Oct,2019
 */

/**
 * When mapping the intermediary join table, it’s better to map only one side as a bidirectional @OneToMany association since
 * otherwise a second SELECT statement will be issued while removing the intermediary join entity.
 */
public class UnidirectionalManyAsOneToManyExtraColumnsTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                Tag.class,
                PostTag.class
        };
    }

    @Test
    public void testLifecycle() {

        doInJPA(entityManager -> {
            Tag misc = new Tag("Misc");
            Tag jdbc = new Tag("JDBC");
            Tag hibernate = new Tag("Hibernate");
            Tag jooq = new Tag("jOOQ");

            entityManager.persist( misc );
            entityManager.persist( jdbc );
            entityManager.persist( hibernate );
            entityManager.persist( jooq );
        });

        doInJPA(entityManager -> {
            Session session = entityManager.unwrap( Session.class );

            Tag misc = session.bySimpleNaturalId(Tag.class).load( "Misc" );
            Tag jdbc = session.bySimpleNaturalId(Tag.class).load( "JDBC" );
            Tag hibernate = session.bySimpleNaturalId(Tag.class).load( "Hibernate" );
            Tag jooq = session.bySimpleNaturalId(Tag.class).load( "jOOQ" );

            Post hpjp1 = new Post("High-Performance Java Persistence 1st edition");
            hpjp1.setId(1L);

            hpjp1.addTag(jdbc);
            hpjp1.addTag(hibernate);
            hpjp1.addTag(jooq);
            hpjp1.addTag(misc);

            entityManager.persist(hpjp1);

            Post hpjp2 = new Post("High-Performance Java Persistence 2nd edition");
            hpjp2.setId(2L);

            hpjp2.addTag(jdbc);
            hpjp2.addTag(hibernate);
            hpjp2.addTag(jooq);

            entityManager.persist(hpjp2);
        });

        doInJPA(entityManager -> {
            Tag misc = entityManager.unwrap( Session.class )
                    .bySimpleNaturalId(Tag.class)
                    .load( "Misc" );

            Post post = entityManager.createQuery(
                    "select p " +
                            "from Post p " +
                            "join fetch p.tags pt " +
                            "join fetch pt.tag " +
                            "where p.id = :postId", Post.class)
                    .setParameter( "postId", 1L )
                    .getSingleResult();

            post.removeTag( misc );
        });
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

        @OneToMany(mappedBy = "post",cascade = CascadeType.ALL,orphanRemoval = true)
        private List<PostTag> tags = new ArrayList<>();

        public Post(String title) {
            this.title = title;
        }

        public void addTag(Tag tag){
            PostTag postTag = new PostTag(this,tag);
            tags.add(postTag);
        }

        public void removeTag(Tag tag){
            for(Iterator<PostTag> iterator = tags.iterator(); iterator.hasNext();){
                PostTag postTag = iterator.next();
                if(postTag.getPost().equals(this) && postTag.getTag().equals(tag)){
                    iterator.remove();
                    postTag.setPost(null);
                    postTag.setTag(null);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Post post = (Post) o;
            return Objects.equals(title, post.title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title);
        }
    }

    @Setter
    @Getter
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostTagId implements Serializable {

        @Column(name = "post_id")
        private Long postId;

        @Column(name = "tag_id")
        private Long tagId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PostTagId postTagId = (PostTagId) o;
            return Objects.equals(postId, postTagId.postId) &&
                    Objects.equals(tagId, postTagId.tagId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(postId, tagId);
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "PostTag")
    @Table(name = "post_tag")
    public static class PostTag{

        @EmbeddedId
        private PostTagId id;

        @MapsId("postId")
        @ManyToOne(fetch = FetchType.LAZY)
        private Post post;

        @MapsId("tagId")
        @ManyToOne(fetch = FetchType.LAZY)
        private Tag tag;

        @Column(name = "created_on")
        private Date createdOn = new Date();

        public PostTag(Post post, Tag tag) {
            this.post = post;
            this.tag = tag;
            this.id = new PostTagId(post.getId(), tag.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PostTag postTag = (PostTag) o;
            return Objects.equals(post, postTag.post) &&
                    Objects.equals(tag, postTag.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(post, tag);
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @Entity(name = "Tag")
    @Table(name = "tag")
    public static class Tag{

        @Id
        @GeneratedValue
        private Long id;

        @NaturalId
        private String name;

        public Tag(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tag tag = (Tag) o;
            return Objects.equals(name, tag.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
