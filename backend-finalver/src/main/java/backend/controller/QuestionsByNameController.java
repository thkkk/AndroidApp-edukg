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

import backend.pojo.ProblemEntity;
import backend.service.NetworkService;
import backend.service.ProblemService;
import backend.service.UserService;

@RestController
public class QuestionsByNameController extends BaseRelyController {
    private ProblemService problemService;

    @Autowired
    public QuestionsByNameController(NetworkService networkService, UserService userService, ProblemService problemService) {
        super(networkService, userService);
        this.problemService = problemService;
    }

    public String warpResult(List<Map<String, Object>> data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> ret = new HashMap<>();
        ret.put("data", data);
        ret.put("code", "0");
        ret.put("msg", "成功");
        return mapper.writeValueAsString(ret);
    }

    @PostMapping("/api/questionListByUriName")
    public String questionListByUriName(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        String name;
        try {
            JsonNode jsonNode = mapper.readTree(body);
            name = jsonNode.get("uriName").asText();
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            List<ProblemEntity> problemEntities = problemService.getProblemListByName(userid, name);
            List<Map<String, Object>> dataList = new LinkedList<>();
            for (ProblemEntity problemEntity : problemEntities) {
                Map<String, Object> map = problemEntity.toMap();
                map.put("marked", problemService.isMarked(userid, problemEntity.getId()));
                dataList.add(map);
            }
            return warpResult(dataList);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器内部错误: %s", e.getMessage()));
        }
    }
}
