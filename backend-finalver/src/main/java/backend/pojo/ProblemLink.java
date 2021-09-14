package backend.pojo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;


@Entity
@Table(name = "problemlink")
@Data
public class ProblemLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userid;

    private int problemid;

    private boolean marked = false;

    private int passNum = 0;

    private int failedNum = 0;

    public void addPass() {
        passNum = passNum + 1;
    }

    public void addFauled() {
        failedNum = failedNum + 1;
    }
}
