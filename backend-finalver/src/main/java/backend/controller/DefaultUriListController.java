package backend.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backend.pojo.UriEntity;
import backend.service.MarkService;
import backend.service.NetworkService;
import backend.service.RecommendUriService;
import backend.service.UserService;

@RestController
public class DefaultUriListController extends BaseRelyController {
    private RecommendUriService recommendUriService;
    private MarkService markService;

    @Autowired
    public DefaultUriListController(NetworkService networkService, UserService userService, RecommendUriService recommendUriService, MarkService markService) {
        super(networkService, userService);
        this.recommendUriService = recommendUriService;
        this.markService = markService;
    }

    String wrapResult(List<UriEntity> uriEntities, int userid) throws JsonProcessingException {
        Map<String, Object> ret = new HashMap<>();
        List<Map<String, Object>> dataList = new LinkedList<>();
        for (UriEntity uriEntity : uriEntities) {
            Map<String, Object> singleEntity = new HashMap<>();
            String uri = uriEntity.getUri();
            singleEntity.put("uri", uri);
            singleEntity.put("name", uriEntity.getEntityName());
            singleEntity.put("course", uriEntity.getCourse());
            singleEntity.put("visited", markService.isVisited(userid, uri));
            singleEntity.put("marked", markService.isMarked(userid, uri));
            dataList.add(singleEntity);
        }
        ret.put("data", dataList);
        ret.put("code", "0");
        ret.put("msg", "成功");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(ret);
    }

    @PostMapping("/api/defaultUriList")
    String defaultUriList(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        List<String> courses = new LinkedList<>();
        try {
            JsonNode jsonNode = mapper.readTree(body);
            if (jsonNode.get("course").isArray()) {
                for (JsonNode course : jsonNode.get("course")) {
                    courses.add(course.asText());
                }
            } else {
                courses.add(jsonNode.get("course").asText());
            }
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            List<UriEntity> uriList = new LinkedList<>();
            for (String course : courses) {
                uriList.addAll(recommendUriService.getDefaultList(course));
            }
            return wrapResult(uriList, userid);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器内部错误: %s", e.getMessage()));
        }
    }
}
