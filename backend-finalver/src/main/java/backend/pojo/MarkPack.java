package backend.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "marktable")
@Data
public class MarkPack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userid;

    @Column(length = 128)
    private String uri;

    @Column(length = 32)
    private String course;

    private boolean marked = false;
    private boolean visited = false;
}
