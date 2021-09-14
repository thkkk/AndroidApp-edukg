package backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.pojo.Predicate;

public interface PredicateDAO extends JpaRepository<Predicate, Integer> {
    Predicate findPredicateByUri(String uri);
}
