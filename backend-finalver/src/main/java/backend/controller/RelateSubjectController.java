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

import backend.service.NetworkService;
import backend.service.UserService;

@RestController
public class RelateSubjectController extends BaseRelyController {
    private final String courseToken = "course";
    private final String[] parameters = {"subjectName", courseToken};
    private final String[] optionals = {};
    private final String[] resultKeys = {"all", "fsanswer", "subject", "message", "tamplateContent", "fs", "filterStr", "subjectUri", "predicate", "score", "answerflag", "attention", "fsscore", "value"};
    private final String url = "http://open.edukg.cn/opedukg/api/typeOpen/open/relatedsubject";
    @Autowired
    public RelateSubjectController(NetworkService networkService, UserService userService) {
        super(networkService, userService);
    }

    List<Map<String, Object>> parseDataAndDecorate(JsonNode jsonNode, String course) {
        JsonNode jsonList = jsonNode.get("data");
        List<Map<String, Object>> dataList = new LinkedList<>();
        if (!jsonList.isArray()) {
            return dataList;
        }
        for (JsonNode dataNode : jsonList) {
            Map<String, Object> map = loadKeys(dataNode, resultKeys);
            map.put("course", course);
            dataList.add(map);
        }
        return dataList;
    }

    String warpResult(List<Map<String, Object>> dataList) throws JsonProcessingException {
        Map<String, Object> ret = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        ret.put("data", dataList);
        ret.put("code", "0");
        ret.put("msg", "成功");
        return mapper.writeValueAsString(ret);
    }

    @PostMapping("/api/relatedsubject")
    public String relatedSubject(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        try {
            getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        List<Map<String, Object>> maps;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(body);
            maps = readRequestJsonWithCourses(jsonNode, parameters, optionals, courseToken);
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            List<Map<String, Object>> rets = new LinkedList<>();
            for (Map<String, Object> map : maps) {
                String result = getNetworkService().defaultPostResponse(url, map);
                jsonNode = mapper.readTree(result);
                rets.addAll(parseDataAndDecorate(jsonNode, map.get(courseToken).toString()));
            }
            return warpResult(rets);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }

}
