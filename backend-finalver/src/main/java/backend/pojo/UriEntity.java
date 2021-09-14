package backend.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "uriEntity")
@Data
public class UriEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 256)
    private String uri;

    @Column(length = 256)
    private String entityName;

    @Column(length = 32)
    private String course;

    private int outDegree = 0;          //作为主语的边的条数
    private int entityDegree = 0;       //与之相关的实体数量
}
