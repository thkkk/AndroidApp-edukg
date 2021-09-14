package backend.controller;

import java.util.ArrayList;
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

import backend.pojo.ProblemEntity;
import backend.pojo.ProblemResult;
import backend.service.NetworkService;
import backend.service.ProblemService;
import backend.service.UserService;


@RestController
public class ProblemController extends BaseRelyController {
    private ProblemService problemService;

    @Autowired
    public ProblemController(NetworkService networkService, UserService userService, ProblemService problemService) {
        super(networkService, userService);
        this.problemService = problemService;
    }

    public String successString() {
        return "{\"code\" : \"0\", \"msg\" : \"成功\"}";
    }

    @PostMapping("/api/markProblem")
    public String markProblem(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        int problemid;
        try {
            jsonNode = mapper.readTree(body);
            problemid = jsonNode.get("problemid").asInt();
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            problemService.markProblem(userid, problemid);
            return successString();
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }

    @PostMapping("/api/unmarkProblem")
    public String unmarkProblem(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        int problemid;
        try {
            jsonNode = mapper.readTree(body);
            problemid = jsonNode.get("problemid").asInt();
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            problemService.unmarkProblem(userid, problemid);
            return successString();
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }


    private String wrapResult(List<Map<String, Object>> data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("code", "0");
        result.put("msg", "成功");
        return mapper.writeValueAsString(result);
    }


    private String wrapResult(Map<String, Object> result, List<Map<String, Object>> data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        result.put("data", data);
        result.put("code", "0");
        result.put("msg", "成功");
        return mapper.writeValueAsString(result);
    }

    @PostMapping("/api/getMarkedProblems")
    public String getMarkedProblems(HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        try {
            List<ProblemEntity> problemEntities = problemService.getMarkedProblems(userid);
            List<Map<String, Object>> results = new LinkedList<>();
            for (ProblemEntity problemEntity : problemEntities) {
                results.add(problemEntity.toMap());
            }
            return wrapResult(results);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }

    @PostMapping("/api/getProblemSet")
    public String getProblemSet(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        List<String> names = new LinkedList<>();
        int maxNum = 1000;
        try {
            jsonNode = mapper.readTree(body);
            if (jsonNode.has("maxNum")) {
                maxNum = jsonNode.get("maxNum").asInt();
            }
            if (jsonNode.get("name").isArray()) {
                for (JsonNode singleNode : jsonNode.get("name")) {
                    names.add(singleNode.asText());
                }
            } else {
                names.add(jsonNode.get("name").asText());
            }
            if (maxNum > 1000) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            ArrayList<ProblemEntity> arrayList = new ArrayList<>();
            for (String name : names) {
                arrayList.addAll(problemService.getProblemListByName(userid, name));
            }
            Collections.shuffle(arrayList);
            List<Map<String, Object>> results = new LinkedList<>();
            for (ProblemEntity problemEntity : arrayList) {
                maxNum--;
                if (maxNum < 0) {
                    break;
                }
                results.add(problemEntity.toMapWithoutAnswer());
            }
            return wrapResult(results);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }

    @PostMapping("/api/checkProblems")
    public String checkProblems(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            List<Map<String, Object>> results = new LinkedList<>();
            jsonNode = mapper.readTree(body).get("data");
            int cnt1 = 0, cnt2 = 0;
            for (JsonNode singleNode : jsonNode) {
                int problemid = singleNode.get("problemid").asInt();
                char yourAnswer = singleNode.get("answer").asText().charAt(0);
                ProblemResult problemResult = problemService.checkResult(userid, problemid, yourAnswer);
                results.add(problemResult.toMap());
                cnt1 = cnt1 + 1;
                if (problemResult.isPassed()) {
                    cnt2 = cnt2 + 1;
                }
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("tot", cnt1);
            resultMap.put("passed", cnt2);
            return wrapResult(resultMap, results);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("检查时遇到问题：%s", e.getMessage()));
        }
    }


    @PostMapping("/api/getRecommendProblem")
    public String getRecommendProblem(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid = 0;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        int maxNum = 1000;
        try {
            jsonNode = mapper.readTree(body);
            if (jsonNode.has("maxNum")) {
                maxNum = jsonNode.get("maxNum").asInt();
            }
            if (maxNum > 1000) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            List<ProblemEntity> arrayList = problemService.getRecommendProblems(userid, maxNum);
            List<Map<String, Object>> results = new LinkedList<>();
            for (ProblemEntity problemEntity : arrayList) {
                results.add(problemEntity.toMapWithoutAnswer());
            }
            return wrapResult(results);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }
}
