package backend.pojo;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ProblemResult {
    private int problemid;
    private char yourAnswer;
    private char stdAnswer;
    public boolean isPassed() {
        return stdAnswer == yourAnswer;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", problemid);
        map.put("youranswer", yourAnswer);
        map.put("stdanswer", stdAnswer);
        return map;
    }
}
