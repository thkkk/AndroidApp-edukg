package backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.pojo.UriLink;

public interface UriLinkDAO extends JpaRepository<UriLink, Integer> {
    List<UriLink> findUriLinkBySubjectUri(String subjectUri);
    List<UriLink> findUriLinkByObjectValue(String objectValue);
    UriLink findUriLinkBySubjectUriAndPredicateUriAndObjectValue(String subjectUri, String predicateUri, String objectValue);
}
