package backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.pojo.ProblemLink;

public interface ProblemLinkDAO extends JpaRepository<ProblemLink, Integer> {
    List<ProblemLink> findProblemLinkByUserid(int userid);
    ProblemLink findProblemLinkByUseridAndProblemid(int userid, int problemid);
}
