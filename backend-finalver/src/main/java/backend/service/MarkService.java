package backend.service;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.dao.MarkPackDAO;
import backend.pojo.MarkPack;
import backend.pojo.UriEntity;

@Service
public class MarkService {
    private MarkPackDAO markPackDAO;
    private UriService uriService;

    @Autowired
    public MarkService(MarkPackDAO markPackDAO, UriService uriService) {
        this.markPackDAO = markPackDAO;
        this.uriService = uriService;
    }

    public boolean isVisited(int userid, String uri) {
        MarkPack mark = markPackDAO.findMarkPackByUseridAndUri(userid, uri);
        if (mark != null) {
            return mark.isVisited();
        }
        return false;
    }

    public boolean isMarked(int userid, String uri) {
        MarkPack mark = markPackDAO.findMarkPackByUseridAndUri(userid, uri);
        if (mark != null) {
            return mark.isMarked();
        }
        return false;
    }

    public List<String> findMarkedUris(int userid) {
        List<String> uris = new LinkedList<>();
        List<MarkPack> marks = markPackDAO.findMarkPacksByUserid(userid);
        for (MarkPack markPack : marks) {
            if (markPack.isMarked()) {
                uris.add(markPack.getUri());
            }
        }
        return uris;
    }

    public List<UriEntity> findMarkedUriEntites(int userid) {
        List<MarkPack> markPacks = markPackDAO.findMarkPacksByUserid(userid);
        List<UriEntity> uriEntities = new LinkedList<>();
        for (MarkPack markPack : markPacks) {
            if (markPack.isMarked()) {
                uriEntities.add(uriService.findUriEntityByUri(markPack.getUri(), markPack.getCourse()));
            }
        }
        return uriEntities;
    }

    public List<String> findVisitedUris(int userid) {
        List<String> uris = new LinkedList<>();
        List<MarkPack> marks = markPackDAO.findMarkPacksByUserid(userid);
        for (MarkPack markPack : marks) {
            if (markPack.isVisited()) {
                uris.add(markPack.getUri());
            }
        }
        return uris;
    }

    public List<UriEntity> findVisitedEntities(int userid, String course) {
        List<UriEntity> uriEntities = new LinkedList<>();
        List<MarkPack> markPacks = markPackDAO.findByUseridAndCourse(userid, course);
        for (MarkPack markPack : markPacks) {
            if (markPack.isVisited()) {
                uriEntities.add(uriService.findUriEntityByUri(markPack.getUri(), markPack.getCourse()));
            }
        }
        return uriEntities;
    }

    public void visitUri(int userid, String uri, String course) {
        MarkPack mark = markPackDAO.findMarkPackByUseridAndUri(userid, uri);
        if (mark == null) {
            mark = new MarkPack();
            mark.setUserid(userid);
            mark.setUri(uri);
            mark.setCourse(course);
        }
        mark.setVisited(true);
        markPackDAO.save(mark);
    }

    public void markUri(int userid, String uri, String course) {
        MarkPack mark = markPackDAO.findMarkPackByUseridAndUri(userid, uri);
        if (mark == null) {
            mark = new MarkPack();
            mark.setUserid(userid);
            mark.setUri(uri);
            mark.setCourse(course);
        }
        mark.setMarked(true);
        markPackDAO.save(mark);
    }

    public void unmarkUri(int userid, String uri, String course) {
        MarkPack mark = markPackDAO.findMarkPackByUseridAndUri(userid, uri);
        if (mark == null) {
            mark = new MarkPack();
            mark.setUserid(userid);
            mark.setUri(uri);
            mark.setCourse(course);
        }
        mark.setMarked(false);
        markPackDAO.save(mark);
    }
}
