package hibernate.association.manytomany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class BidirectionalManyToManyLinkEntityTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
            Post.class,
            PostTag.class,
            Tag.class
        };
    }

    @Test
    public void testLifeCycle(){
        doInJPA(entityManager -> {
            Post post1 = new Post("JPA with Hibernate");
            Post post2 = new Post("Native Hibernate");

            Tag tag1 = new Tag("Java");
            Tag tag2 = new Tag("Hibernate");

            entityManager.persist(post1);
            entityManager.persist(post2);

            entityManager.persist(tag1);
            entityManager.persist(tag2);

            post1.addTag(tag1);
            post1.addTag(tag2);

            post2.addTag(tag1);

            entityManager.flush();

            LOGGER.info("Remove");
            //1 DELETE Query from post_tag
            post1.removeTag(tag1);
        });
    }

    @Test
    public void testShuffle(){
        final Long postId = doInJPA(entityManager -> {
            Post post1 = new Post("JPA with Hibernate");
            Post post2 = new Post("Native Hibernate");

            Tag tag1 = new Tag("Java");
            Tag tag2 = new Tag("Hibernate");

            entityManager.persist(post1);
            entityManager.persist(post2);

            entityManager.persist(tag1);
            entityManager.persist(tag2);

            post1.addTag(tag1);
            post1.addTag(tag2);

            post2.addTag(tag1);

            entityManager.flush();

            return post1.getId();
        });
        doInJPA(entityManager -> {
            LOGGER.info("Shuffle");
            //1 SELECT Query
            Post post1 = entityManager.find(Post.class, postId);
            //1 SELECT QUERY with inner join tag & post_tag
            post1.getTags().sort((postTag1, postTag2) ->
                    postTag2.getId().getTagId().compareTo(postTag1.getId().getTagId())
            );
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
        private List<PostTag> tags = new ArrayList<>();

        public Post(String title) {
            this.title = title;
        }

        public void addTag(Tag tag){
            PostTag postTag = new PostTag(this,tag);
            tags.add(postTag);
            tag.getPosts().add(postTag);
        }

        public void removeTag(Tag tag){
            for(Iterator<PostTag> iterator = tags.iterator(); iterator.hasNext();){
                PostTag postTag = iterator.next();
                if(postTag.getPost().equals(this) && postTag.getTag().equals(tag)){
                    iterator.remove();
                    postTag.getTag().getPosts().remove(postTag);
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
    @NoArgsConstructor
    @Embeddable
    public static class PostTagId implements Serializable{

        private Long postId;

        private Long tagId;

        public PostTagId(Long postId, Long tagId) {
            this.postId = postId;
            this.tagId = tagId;
        }

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
    @Table(name = "post_tag")
    @Entity(name = "PostTag")
    public static class PostTag{

        @EmbeddedId
        private PostTagId id;

        @ManyToOne
        @MapsId("postId")
        private Post post;

        @ManyToOne
        @MapsId("tagId")
        private Tag tag;

        public PostTag(Post post, Tag tag) {
            this.post = post;
            this.tag = tag;
            this.id = new PostTagId(post.getId(),tag.getId());
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
    @Table(name = "tag")
    @Entity(name = "Tag")
    public static class Tag{

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @OneToMany(mappedBy = "tag",cascade = CascadeType.ALL,orphanRemoval = true)
        private List<PostTag> posts = new ArrayList<>();

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
