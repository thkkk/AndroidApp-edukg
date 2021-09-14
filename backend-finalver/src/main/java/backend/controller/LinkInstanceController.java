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

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backend.service.MarkService;
import backend.service.NetworkService;
import backend.service.UserService;

@RestController
public class LinkInstanceController extends BaseRelyController {
    private MarkService markService;

    private final String courseToken = "course";
    private final String[] parameters = {courseToken, "context"};
    private final String[] optionals = {};
    private final String[] resultKeys = {"entity_type", "entity_url", "start_index", "end_index", "entity"};
    private final String url = "http://open.edukg.cn/opedukg/api/typeOpen/open/linkInstance";

    public LinkInstanceController(NetworkService networkService, UserService userService, MarkService markService) {
        super(networkService, userService);
        this.markService = markService;
    }

    List<Map<String, Object>> parseDataAndDecorate(JsonNode jsonNode, String course, int userid) {
        List<Map<String, Object>> data = new LinkedList<>();
        JsonNode jsonList = jsonNode.findPath("data").findPath("results");
        if (!jsonList.isArray()) {
            return data;
        }
        for (JsonNode dataNode : jsonList) {
            String uri = dataNode.get("entity_url").asText();
            Map<String, Object> resultMap = loadKeys(dataNode, resultKeys);
            resultMap.put("visited", markService.isVisited(userid, uri));
            resultMap.put("marked", markService.isMarked(userid, uri));
            data.add(resultMap);
        }
        return data;
    }

    String wrapResult(List<Map<String, Object>> result) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = new HashMap<>();
        data.put("results", result);
        Map<String, Object> ret = new HashMap<>();
        ret.put("data", data);
        ret.put("code", "0");
        ret.put("msg", "成功");
        return mapper.writeValueAsString(ret);
    }

    @PostMapping("/api/linkInstance")
    public String linkInstance(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserService().getUserID(request.getHeader("Authorization"));
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        List<Map<String, Object>> maps;
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(body);
            maps = readRequestJsonWithCourses(jsonNode, parameters, optionals, courseToken);
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            String result;
            List<Map<String, Object>> rets = new LinkedList<>();
            for (Map<String, Object> map : maps) {
                result = getNetworkService().defaultPostResponse(url, map);
                JsonNode jsonNode = mapper.readTree(result);
                rets.addAll(parseDataAndDecorate(jsonNode, map.get(courseToken).toString(), userid));
            }
            return wrapResult(rets);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器内部错误: %s", e.getMessage()));
        }
    }
}
