package backend.pojo;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "problementity")
@Data
public class ProblemEntity {
    @Id
    private int id;

    @Column(length = 512)
    private String context;

    @Column(length = 128)
    private String answerA;

    @Column(length = 128)
    private String answerB;

    @Column(length = 128)
    private String answerC;

    @Column(length = 128)
    private String answerD;

    private char qanswer;

    public Map<String, Object> toMap() {
        Map<String, Object> map = toMapWithoutAnswer();
        map.put("qAnswer", qanswer);
        return map;
    }

    public Map<String, Object> toMapWithoutAnswer() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("qBody", context);
        map.put("answerA", answerA);
        map.put("answerB", answerB);
        map.put("answerC", answerC);
        map.put("answerD", answerD);
        return map;
    }

}
