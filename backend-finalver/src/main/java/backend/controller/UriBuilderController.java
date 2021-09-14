package backend.controller;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backend.pojo.UriEntity;
import backend.pojo.UriLink;
import backend.service.RecommendUriService;
import backend.service.UriLinkService;
import backend.service.UriService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class UriBuilderController {
    private UriService uriService;
    private UriLinkService uriLinkService;
    private RecommendUriService recommendUriService;

    public UriBuilderController(UriService uriService, UriLinkService uriLinkService, RecommendUriService recommendUriService) {
        this.uriService = uriService;
        this.uriLinkService = uriLinkService;
        this.recommendUriService = recommendUriService;
    }

    @PostMapping("/manage/clearUriEntity")
    String clearUriEntity(HttpServletRequest request, HttpServletResponse response) {
        try {
            uriService.clearAll();
        } catch (Exception e) {
            response.setStatus(500);
            return e.getMessage();
        }
        return "database cleared.";
    }

    @PostMapping("/manage/clearRecommend")
    String clearRecommend(HttpServletRequest request, HttpServletResponse response) {
        try {
            recommendUriService.clearRecommed();
        } catch (Exception e) {
            response.setStatus(500);
            return e.getMessage();
        }
        return "database cleared.";
    }

    @PostMapping("/manage/clearUriLink")
    String clearUriLink(HttpServletRequest request, HttpServletResponse response) {
        try {
            uriLinkService.clearLinks();
        } catch (Exception e) {
            response.setStatus(500);
            return e.getMessage();
        }
        return "database cleared.";
    }

    @PostMapping("/manage/clearPredicate")
    String clearPredicate(HttpServletRequest request, HttpServletResponse response) {
        try {
            uriLinkService.clearPredicate();
        } catch (Exception e) {
            response.setStatus(500);
            return e.getMessage();
        }
        return "database cleared.";
    }

    @PostMapping("/manage/addUriLink")
    String addUriLink(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body).get("data");
            List<UriLink> uriLinks = new LinkedList<>();
            log.info("request received.");
            for (JsonNode singleNode : jsonNode) {
                String subject = singleNode.get("subject").asText();
                String predicate = singleNode.get("predicate").asText();
                String object = singleNode.get("object").asText();
                int id = singleNode.get("id").asInt();
                if (object.length() <= 500) {
                    UriLink uriLink = new UriLink();
                    uriLink.setId(id);
                    uriLink.setSubjectUri(subject);
                    uriLink.setPredicateUri(predicate);
                    uriLink.setObjectValue(object);
                    uriLinks.add(uriLink);
                }
            }
            log.info("start...");
            uriLinkService.saveUriLinks(uriLinks);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            return e.getMessage();
        }
        return "add operation done.";
    }

    @PostMapping("/manage/addUriEntities")
    String addUriEntities(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            String course = jsonNode.get("course").asText();
            JsonNode dataList = jsonNode.get("data");
            List<UriEntity> uriList = new LinkedList<>();
            for (JsonNode singleJson : dataList) {
                if (singleJson.get("name").asText().isEmpty()) {
                    continue;
                }
                UriEntity uriEntity = new UriEntity();
                uriEntity.setCourse(course);
                uriEntity.setEntityName(uriService.stripName(singleJson.get("name").asText()));
                uriEntity.setUri(singleJson.get("uri").asText());
                uriEntity.setOutDegree(singleJson.get("outDegree").asInt());
                uriList.add(uriEntity);
            }
            uriService.addMultiUriEntities(uriList);
        } catch (Exception e) {
            response.setStatus(500);
            return e.getMessage();
        }
        return "add operation done.";
    }


    @PostMapping("/manage/addSingleRecommend")
    String addSingleRecommend(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            String course = jsonNode.get("course").asText();
            String uri = jsonNode.get("uri").asText();
            recommendUriService.addRecommend(uri, course);
        } catch (Exception e) {
            response.setStatus(500);
            return e.getMessage();
        }
        return "add operation done.";
    }
}
