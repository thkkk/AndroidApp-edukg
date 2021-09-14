package backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import backend.dao.PredicateDAO;
import backend.dao.UriLinkDAO;
import backend.pojo.Predicate;
import backend.pojo.UriEntity;
import backend.pojo.UriLink;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UriLinkService {
    private NetworkService networkService;
    private UriLinkDAO uriLinkDAO;
    private PredicateDAO predicateDAO;
    private UriService uriService;
    private MarkService markService;
    private final String uriPattern = "http://.*";
    private final String relatedSubjectUrl = "http://open.edukg.cn/opedukg/api/typeOpen/open/relatedsubject";

    @Autowired
    UriLinkService(NetworkService networkService, UriLinkDAO uriLinkDAO, PredicateDAO predicateDAO, UriService uriService, MarkService markService) {
        this.networkService = networkService;
        this.uriLinkDAO = uriLinkDAO;
        this.predicateDAO = predicateDAO;
        this.uriService = uriService;
        this.markService = markService;
    }

    public void clearLinks() {
        uriLinkDAO.deleteAll();
    }

    public void clearPredicate() {
        predicateDAO.deleteAll();
    }

    private void saveSinglePredicate(String uri, String description) {
        Predicate predicate = new Predicate();
        predicate.setUri(uri);
        predicate.setDescription(description);
        predicateDAO.save(predicate);
    }

    public void saveUnKnownPredicate(String uri, String course) {
        Predicate predicate = predicateDAO.findPredicateByUri(uri);
        if (predicate != null) {
            return;
        }
        String description;
        try {
            description = networkService.getNameOfUri(course, uri);
            if (description.isEmpty()) {
                return;
            }
        } catch (Exception e) {
            log.warn("get name of uri({}) failed", uri);
            return;
        }
        saveSinglePredicate(uri, description);
    }

    public Predicate getUnknownPredicate(String uri, String course) {
        Predicate predicate = predicateDAO.findPredicateByUri(uri);
        if (predicate != null) {
            return predicate;
        }
        saveUnKnownPredicate(uri, course);
        return predicateDAO.findPredicateByUri(uri);
    }

    public void saveKnownPredicate(String uri, String description) {
        if (predicateDAO.findPredicateByUri(uri) == null) {
            saveSinglePredicate(uri, description);
        }
    }

    public void saveUriLinks(List<UriLink> uriLinks) throws DataAccessException {
        log.info("start inserting...");
        uriLinkDAO.saveAll(uriLinks);
    }

    public void saveSingleUriLink(String subject, String predicate, String object) {
        UriLink uriLink = new UriLink();
        uriLink.setSubjectUri(subject);
        uriLink.setPredicateUri(predicate);
        uriLink.setObjectValue(object);
        uriLinkDAO.save(uriLink);
    }

    public boolean isUri(String objectValue) {
        return Pattern.matches(uriPattern, objectValue);
    }

    private void addToMapList(Map<String, List<String>> map, UriLink uriLink) {
        if (uriLink.getObjectValue().isEmpty()) {
            return;
        }
        List<String> values;
        if (!map.containsKey(uriLink.getPredicateUri())) {
            values = new LinkedList<>();
        } else {
            values = map.get(uriLink.getPredicateUri());
        }
        values.add(uriLink.getObjectValue());
        map.put(uriLink.getPredicateUri(), values);
    }

    protected List<Map<String, Object>> parsePropertyMapToList(Map<String, List<String>> propertyMap, String course) {
        List<Map<String, Object>> propertyList = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : propertyMap.entrySet()) {
            Predicate predicate = getUnknownPredicate(entry.getKey(), course);
            if (predicate == null) {
                continue;
            }
            if (predicate.getDescription().isEmpty()) {
                continue;
            }
            Map<String, Object> ret = new HashMap<>();
            ret.put("predicate", predicate.getDescription());
            ret.put("object", entry.getValue());
            propertyList.add(ret);
        }
        return propertyList;
    }

    protected List<Map<String, Object>> parseRelatedMapToList(Map<String, List<String>> relatedMap, String course, int userid) {
        List<Map<String, Object>> relatedList = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : relatedMap.entrySet()) {
            Predicate predicate = getUnknownPredicate(entry.getKey(), course);
            if (predicate == null) {
                continue;
            }
            if (predicate.getDescription().isEmpty()) {
                continue;
            }
            List<Map<String, Object>> uriList = new LinkedList<>();
            for (String uri : entry.getValue()) {
                UriEntity uriEntity = uriService.findEntityInDatabaseByUri(uri);
                if (uriEntity != null) {
                    Map<String, Object> uriEntityMap = new HashMap<>();
                    uriEntityMap.put("uri", uriEntity.getUri());
                    uriEntityMap.put("name", uriEntity.getEntityName());
                    uriEntityMap.put("course", uriEntity.getCourse());
                    uriEntityMap.put("visited", markService.isVisited(userid, uri));
                    uriEntityMap.put("marked", markService.isMarked(userid, uri));
                    uriList.add(uriEntityMap);
                }
            }
            if (!uriList.isEmpty()) {
                Map<String, Object> ret = new HashMap<>();
                ret.put("predicate", predicate.getDescription());
                ret.put("object", uriList);
                relatedList.add(ret);
            }
        }
        return relatedList;
    }

    protected List<Map<String, Object>> parseRelatedMapAndPropertyToList(Map<String, List<String>> relatedMap, String course, int userid, Map<String, List<String>> propertyMap) {
        List<Map<String, Object>> relatedList = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : relatedMap.entrySet()) {
            Predicate predicate = getUnknownPredicate(entry.getKey(), course);
            if (predicate == null) {
                continue;
            }
            if (predicate.getDescription().isEmpty()) {
                continue;
            }
            List<Map<String, Object>> uriList = new LinkedList<>();
            Set<String> visitedName = new HashSet<>();
            for (String uri : entry.getValue()) {
                UriEntity uriEntity = uriService.findEntityInDatabaseByUri(uri);
                if (uriEntity != null) {
                    Map<String, Object> uriEntityMap = new HashMap<>();
                    uriEntityMap.put("uri", uriEntity.getUri());
                    uriEntityMap.put("name", uriEntity.getEntityName());
                    uriEntityMap.put("course", uriEntity.getCourse());
                    uriEntityMap.put("visited", markService.isVisited(userid, uri));
                    uriEntityMap.put("marked", markService.isMarked(userid, uri));
                    visitedName.add(uriEntity.getEntityName());
                    uriList.add(uriEntityMap);
                }
            }
            if (propertyMap.containsKey(predicate.getDescription())) {
                List<String> valueList = propertyMap.remove(predicate.getDescription());
                for (String value : valueList) {
                    if (visitedName.contains(value)) {
                        continue;
                    }
                    Map<String, Object> fakeMap = new HashMap<>();
                    fakeMap.put("uri", "");
                    fakeMap.put("name", value);
                    fakeMap.put("course", course);
                    fakeMap.put("visited", false);
                    fakeMap.put("visited", false);
                    uriList.add(fakeMap);
                }
            }
            if (!uriList.isEmpty()) {
                Map<String, Object> ret = new HashMap<>();
                ret.put("predicate", predicate.getDescription());
                ret.put("object", uriList);
                relatedList.add(ret);
            }
        }
        for (Map.Entry<String, List<String>> entry : propertyMap.entrySet()) {
            List<Map<String, Object>> uriList = new LinkedList<>();
            for (String value : entry.getValue()) {
                Map<String, Object> fakeMap = new HashMap<>();
                fakeMap.put("uri", "");
                fakeMap.put("name", value);
                fakeMap.put("course", course);
                fakeMap.put("visited", false);
                fakeMap.put("visited", false);
                uriList.add(fakeMap);
            }
            if (!uriList.isEmpty()) {
                Map<String, Object> ret = new HashMap<>();
                ret.put("predicate", entry.getKey());
                ret.put("object", uriList);
                relatedList.add(ret);
            }
        }
        return relatedList;
    }

    private Map<String, List<String>> addRelatedSubjectToProperty(String name, String course) {
        Map<String, List<String>> map = new HashMap<>();
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("subjectName", name);
        queryMap.put("course", course);
        String relatedSubjectString;
        try {
            relatedSubjectString = networkService.defaultPostResponse(relatedSubjectUrl, queryMap);
        } catch (Exception e) {
            log.warn("query of {} failed", name);
            return map;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(relatedSubjectString).get("data");
            for (JsonNode singleNode : jsonNode) {
                if (name.equals(singleNode.get("subject").asText())) {
                    try {
                        String value = singleNode.get("value").asText();
                        String predicate = singleNode.get("predicate").asText();
                        List<String> valueList = new ArrayList<>();
                        if (map.containsKey(predicate)) {
                            valueList = map.get(predicate);
                        }
                        value = value.replace("，", ",");
                        value = value.replace("<br><br>", "，");
                        value = value.replace("<br>", "");
                        String[] valueStrings = value.split("，", 100);
                        valueList.addAll(Arrays.asList(valueStrings));
                        if (valueList.isEmpty()) {
                            continue;
                        }
                        if (predicate.isEmpty()) {
                            continue;
                        }
                        map.put(predicate, valueList);
                    } catch (Exception e) {
                        log.warn("parsing process meets exception when query {}", name);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("parsing process meets json missing exception when query {}", name);
            return new HashMap<>();
        }
        return map;
    }

    public Map<String, List<Map<String, Object>>> getRelatedInformation(String uri, String course, String name, int userid) {
        List<UriLink> uriLinks = uriLinkDAO.findUriLinkBySubjectUri(uri);

        Map<String, List<String>> propertyMap = new HashMap<>();
        Map<String, List<String>> relatedMap = new HashMap<>();
        for (UriLink uriLink : uriLinks) {
            if (isUri(uriLink.getObjectValue())) {
                addToMapList(relatedMap, uriLink);
            } else {
                addToMapList(propertyMap, uriLink);
            }
        }

        Map<String, List<String>> netPropertyMap = addRelatedSubjectToProperty(name, course);

        List<Map<String, Object>> propertyList = parsePropertyMapToList(propertyMap, course);
        //List<Map<String, Object>> relatedList = parseRelatedMapToList(relatedMap, course, userid);
        List<Map<String, Object>> relatedList = parseRelatedMapAndPropertyToList(relatedMap, course, userid, netPropertyMap);

        Map<String, List<Map<String, Object>>> ret = new HashMap<>();
        ret.put("property", propertyList);
        ret.put("related", relatedList);
        return ret;
    }
}
