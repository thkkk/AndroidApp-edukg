package backend.controller;

import java.util.Collections;
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
import backend.service.UserService;

@RestController
public class MarkController {
    private UserService userService;
    private MarkService markService;

    @Autowired
    public MarkController(UserService userService, MarkService markService) {
        this.userService = userService;
        this.markService = markService;
    }

    public int getUserID(HttpServletRequest request) throws Exception {
        return userService.getUserID(request.getHeader("Authorization"));
    }

    public String errorString(String msg) {
        return String.format("{\"code\" : \"-1\", \"msg\" : \"%s\"}", msg);
    }

    public String successString() {
        return String.format("{\"code\" : \"0\", \"msg\" : \"成功\"}");
    }

    public String warpResult(List<Map<String, Object>> dataList) throws JsonProcessingException {
        Map<String, Object> ret = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        ret.put("data", dataList);
        ret.put("code", "0");
        ret.put("msg", "成功");
        return mapper.writeValueAsString(ret);
    }

    @PostMapping("/api/markUri")
    public String markUri(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        String uri, course;
        try {
            JsonNode jsonNode = mapper.readTree(body);
            uri = jsonNode.get("uri").asText();
            course = jsonNode.get("course").asText();
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            markService.markUri(userid, uri, course);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
        return successString();
    }


    @PostMapping("/api/unmarkUri")
    public String unmarkUri(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        String uri, course;
        try {
            JsonNode jsonNode = mapper.readTree(body);
            uri = jsonNode.get("uri").asText();
            course = jsonNode.get("course").asText();
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            markService.unmarkUri(userid, uri, course);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
        return successString();
    }

    @PostMapping("/api/getMarkList")
    public String getMarkList(HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        try {
            List<Map<String, Object>> results = new LinkedList<>();
            List<UriEntity> uriEntities = markService.findMarkedUriEntites(userid);
            for (UriEntity uriEntity : uriEntities) {
                Map<String, Object> result = new HashMap<>();
                result.put("name", uriEntity.getEntityName());
                result.put("uri", uriEntity.getUri());
                result.put("course", uriEntity.getCourse());
                results.add(result);
            }
            return warpResult(results);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }

    @PostMapping("/api/getHistoryList")
    public String getHistoryList(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        List<String> courses = new LinkedList<>();
        int maxNum = 1000;
        try {
            JsonNode jsonNode = mapper.readTree(body);
            JsonNode courseNode = jsonNode.get("course");
            if (courseNode.isArray()) {
                for (JsonNode singleNode : courseNode) {
                    courses.add(singleNode.asText());
                }
            } else {
                courses.add(courseNode.asText());
            }
            if (jsonNode.has("maxNum")) {
                maxNum = jsonNode.get("maxNum").asInt();
            }
            if (maxNum < 0 || maxNum > 1000) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            List<Map<String, Object>> results = new LinkedList<>();
            List<UriEntity> uriEntities = new LinkedList<>();
            for (String course : courses) {
                uriEntities.addAll(markService.findVisitedEntities(userid, course));
            }
            Collections.shuffle(uriEntities);
            for (UriEntity uriEntity : uriEntities) {
                maxNum--;
                if (maxNum < 0) {
                    break;
                }
                Map<String, Object> result = new HashMap<>();
                result.put("name", uriEntity.getEntityName());
                result.put("uri", uriEntity.getUri());
                result.put("course", uriEntity.getCourse());
                result.put("marked", markService.isMarked(userid, uriEntity.getUri()));
                results.add(result);
            }
            return warpResult(results);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }

}
