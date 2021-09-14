package backend.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "usertable")
@Data //自动编写get、set方法
public class User {
    @Id //声明注解下面的字段“id”为数据库表的主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) //标注主键的生成策略，通过strategy属性指定
    private int id; //主键id

    @Column(length = 128)
    private String username;

    @Column(length = 128)
    private String passkeys;
}
