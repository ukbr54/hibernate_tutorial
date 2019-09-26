package hibernate.query.dto.projection;

import hibernate.forum.dto.PostDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */


@NamedNativeQuery(
        name = "PostDTOQuery",
        query = "SELECT p.id AS id, p.title AS title FROM post p WHERE p.created_on > :timestamp",
        resultSetMapping = "ProjectionPostDTO"
)
@SqlResultSetMapping(
        name = "ProjectionPostDTO",
        classes = @ConstructorResult(
                targetClass = PostDTO.class,
                columns = {
                        @ColumnResult(name = "id",type = Long.class),
                        @ColumnResult(name = "title",type = String.class)
                }
        )
)
@Setter
@Getter
@NoArgsConstructor
@Entity(name = "Post")
@Table(name = "post")
public class Post {

    @Id
    private Long id;

    private String title;

    @Column(name = "created_on")
    private Timestamp createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_on")
    private Timestamp updatedOn;

    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    private Integer version;
}
