package backend.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.dao.UriEntityDAO;
import backend.pojo.UriEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UriService {
    private UriEntityDAO uriEntityDAO;
    private NetworkService networkService;

    @Autowired
    public UriService(UriEntityDAO uriEntityDAO, NetworkService networkService) {
        this.uriEntityDAO = uriEntityDAO;
        this.networkService = networkService;
    }

    public String stripName(String originName) {
        if (originName == null) {
            log.warn("empty name...");
            return "";
        } else {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(originName);
            return m.replaceAll("");
        }
    }

    public void clearAll() {
        uriEntityDAO.deleteAll();
    }

    private UriEntity addAndGetUriEntity(String uri, String course) {
        UriEntity uriEntity = new UriEntity();
        uriEntity.setCourse(course);
        uriEntity.setUri(uri);
        try {
            uriEntity.setEntityName(networkService.getNameOfUri(course, uri));
            uriEntityDAO.save(uriEntity);
        } catch (Exception e) {
            log.warn(String.format("getting name of %s meets error with error:%s", uri, e.getMessage()));
            uriEntity.setEntityName("");
        }
        return uriEntity;
    }

    public UriEntity findUriEntityByUri(String uri, String course) {
        UriEntity uriEntity = uriEntityDAO.findUriEntityByUri(uri);
        if (uriEntity == null) {
            uriEntity = addAndGetUriEntity(uri, course);
        }
        return uriEntity;
    }

    public UriEntity findEntityInDatabaseByUri(String uri) {
        return uriEntityDAO.findUriEntityByUri(uri);
    }

    public List<UriEntity> findTop10ByOutDegreeDESC(String course) {
        return uriEntityDAO.findByCourseByOrderByOutDegreeDESC(course);
    }

    public List<UriEntity> findUriEntitiesByCourse(String course) {
        return uriEntityDAO.findUriEntityByCourse(course);
    }

    public List<UriEntity> findUriEititiesByCourseAndName(String course, String name) {
        return uriEntityDAO.findUriEntityByCourseAndEntityName(course, name);
    }

    public void addSingleUriEntity(String uri, String course, String name, int outDegree) {
        UriEntity uriEntity = uriEntityDAO.findUriEntityByUri(uri);
        if (uriEntity != null) {
            log.info(String.format("uri %s has been added.", uri));
            return;
        }
        uriEntity = new UriEntity();
        try {
            uriEntity.setCourse(course);
            uriEntity.setEntityName(name);
            uriEntity.setUri(uri);
            uriEntity.setOutDegree(outDegree);
            uriEntityDAO.save(uriEntity);
        } catch (Exception e) {
            log.warn(String.format("getting name of %s meets error with error:%s", uri, e.getMessage()));
        }
    }

    public void addMultiUriEntities(List<UriEntity> uriEntities) {
        uriEntityDAO.saveAll(uriEntities);
    }
}
