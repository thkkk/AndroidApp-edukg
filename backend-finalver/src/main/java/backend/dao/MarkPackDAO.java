package backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.pojo.MarkPack;

public interface MarkPackDAO extends JpaRepository<MarkPack, Integer> {
    List<MarkPack> findMarkPacksByUserid(int userid);
    MarkPack findMarkPackByUseridAndUri(int userid, String uri);
    List<MarkPack> findByUseridAndCourse(int userid, String course);
}
