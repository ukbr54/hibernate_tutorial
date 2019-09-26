package hibernate.forum.dto;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */

public class PostDTO {

    private Long id;

    private String title;

    public PostDTO() { }

    public PostDTO(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
