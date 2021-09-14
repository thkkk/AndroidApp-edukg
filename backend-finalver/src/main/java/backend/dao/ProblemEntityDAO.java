package backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.pojo.ProblemEntity;

public interface ProblemEntityDAO extends JpaRepository<ProblemEntity, Integer> {
    ProblemEntity findProblemEntityById(int id);
}
