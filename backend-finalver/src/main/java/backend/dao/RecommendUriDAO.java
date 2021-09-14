package backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.pojo.RecommendUri;

public interface RecommendUriDAO extends JpaRepository<RecommendUri, Integer> {
    List<RecommendUri> findRecommedUriByCourse(String course);
    RecommendUri findRecommendUriByuri(String uri);
}
