package backend.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backend.pojo.UriEntity;
import backend.service.MarkService;
import backend.service.NetworkService;
import backend.service.UriService;
import backend.service.UserService;

@RestController
public class InfoController extends BaseRelyController {
    private MarkService markService;
    private UriService uriService;

    private final String courseToken = "course";
    private final String[] parameters = {courseToken, "name"};
    private final String[] optionals = {};
    private final String[] resultKeys = {"label", "content"};
    private final String url = "http://open.edukg.cn/opedukg/api/typeOpen/open/infoByInstanceName";
    //private final String imagePattern = "http://kb\\.cs\\.tsinghua\\.edu\\.cn.*";
    private final String uriPattern = "http://.*";

    @Autowired
    public InfoController(NetworkService networkService, UserService userService, MarkService markService, UriService uriService) {
        super(networkService, userService);
        this.markService = markService;
        this.uriService = uriService;
    }

    List<Map<String, Object>> parseProperty(JsonNode propertyNode) {
        List<Map<String, Object>> properties = new LinkedList<>();
        if (!propertyNode.isArray()) {
            return properties;
        }
        Map<String, List<String>> checkMap = new HashMap<>();
        for (JsonNode singleNode : propertyNode) {
            String predicate = singleNode.get("predicateLabel").asText();
            String value = singleNode.get("object").asText();
            if (Pattern.matches(uriPattern, value)) {
                UriEntity uriEntity = uriService.findEntityInDatabaseByUri(value);
                if (uriEntity == null) {
                    continue;
                }
                value = uriEntity.getEntityName();
            }
            List<String> valueList;
            if (checkMap.containsKey(predicate)) {
                valueList = checkMap.get(predicate);
            } else {
                valueList = new LinkedList<>();
            }
            valueList.add(value);
            checkMap.put(predicate, valueList);
        }
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : checkMap.entrySet()) {
            String value = "";
            for (String singleValue : entry.getValue()) {
                value = value + singleValue + "\n";
            }
            Map<String, Object> ret = new HashMap<>();
            ret.put("predicateLabel", entry.getKey());
            ret.put("object", value);
            result.add(ret);
        }
        return result;
    }

    Map<String, Object> parseDataAndDecorate(JsonNode jsonNode, String uri, int userid, String course, String name) throws Exception {
        Map<String, Object> data = loadKeys(jsonNode.get("data"), resultKeys);
        data.put("property", parseProperty(jsonNode.get("data").get("property")));
        data.put("marked", markService.isMarked(userid, uri));
        markService.visitUri(userid, uri, course);

        Map<String, Object> ret = new HashMap<>();
        ret.put("data", data);
        ret.put("msg", "成功");
        ret.put("code", "0");
        return ret;
    }

    @PostMapping("/api/infoByInstanceName")
    public String infoByInstanceName(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserService().getUserID(request.getHeader("Authorization"));
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        Map<String, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        String uri, course, name;
        try {
            JsonNode jsonNode = mapper.readTree(body);
            uri = jsonNode.get("uri").asText();
            name = jsonNode.get("name").asText();
            course = jsonNode.get(courseToken).asText();
            map = readRequestJson(jsonNode, parameters, optionals);
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            String result = getNetworkService().defaultGetResponse(url, map);
            JsonNode jsonNode = mapper.readTree(result);
            return mapper.writeValueAsString(parseDataAndDecorate(jsonNode, uri, userid, course, name));
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器内部错误: %s", e.getMessage()));
        }
    }
}

