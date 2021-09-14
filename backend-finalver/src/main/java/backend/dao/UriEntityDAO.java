package backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import backend.pojo.UriEntity;

public interface UriEntityDAO extends JpaRepository<UriEntity, Integer> {
    UriEntity findUriEntityByUri(String uri);

    @Query(value = "SELECT * FROM uriEntity WHERE course = ?1 ORDER BY outDegree DESC LIMIT 10;", nativeQuery = true)
    List<UriEntity> findByCourseByOrderByOutDegreeDESC(String course);

    List<UriEntity> findUriEntityByCourse(String course);

    List<UriEntity> findUriEntityByCourseAndEntityName(String course, String entityName);
}
