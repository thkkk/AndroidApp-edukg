package backend.service;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.dao.RecommendUriDAO;
import backend.pojo.RecommendUri;
import backend.pojo.UriEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RecommendUriService {
    private RecommendUriDAO recommendUriDAO;
    private UriService uriService;

    @Autowired
    public RecommendUriService(RecommendUriDAO recommendUriDAO, UriService uriService) {
        this.recommendUriDAO = recommendUriDAO;
        this.uriService = uriService;
    }

    public void clearRecommed() {
        recommendUriDAO.deleteAll();
    }

    public void addRecommend(String uri, String course) {
        RecommendUri recommedUri = recommendUriDAO.findRecommendUriByuri(uri);
        if (recommedUri != null) {
            log.warn("adding duplicate entity...");
            return;
        }
        recommedUri = new RecommendUri();
        recommedUri.setCourse(course);
        recommedUri.setUri(uri);
        recommendUriDAO.save(recommedUri);
    }

    public List<UriEntity> getDefaultList(String course) {
        List<RecommendUri> uris = recommendUriDAO.findRecommedUriByCourse(course);
        List<UriEntity> entities = new LinkedList<>();
        for (RecommendUri recommendUri : uris) {
            entities.add(uriService.findUriEntityByUri(recommendUri.getUri(), course));
        }
        return entities;
    }
}
