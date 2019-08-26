package jpabootstrap.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */

@Setter
@Getter
@ToString
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String email;

    public User(){}

    public User(String name,String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
