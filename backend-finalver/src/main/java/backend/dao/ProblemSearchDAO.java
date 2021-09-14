package backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.pojo.ProblemSearch;

public interface ProblemSearchDAO extends JpaRepository<ProblemSearch, Integer> {
    ProblemSearch findProblemSearchByKeyString(String keyString);
}
