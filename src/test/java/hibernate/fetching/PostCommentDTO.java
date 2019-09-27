package hibernate.fetching;

import lombok.Getter;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

@Getter
public class PostCommentDTO {

    private final Long id;
    private final String review;
    private final String title;

    public PostCommentDTO(Long id, String review, String title) {
        this.id = id;
        this.review = review;
        this.title = title;
    }
}
