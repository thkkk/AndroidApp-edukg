package backend.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;

import backend.service.NetworkService;
import backend.service.UserService;

public class BaseRelyController {
    private NetworkService networkService;
    private UserService userService;

    @Autowired
    public BaseRelyController(NetworkService networkService, UserService userService) {
        this.networkService = networkService;
        this.userService = userService;
    }

    protected NetworkService getNetworkService() {
        return networkService;
    }

    protected int getUserID(HttpServletRequest request) throws Exception {
        return userService.getUserID(request.getHeader("Authorization"));
    }

    protected UserService getUserService() {
        return userService;
    }

    public String errorString(String msg) {
        return String.format("{\"code\" : \"-1\", \"msg\" : \"%s\"}", msg);
    }

    public Map<String, Object> readRequestJson(String body, String[] parameters, String[] optionals) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(body);
        for (String param : parameters) {
            ret.put(param, jsonNode.get(param).asText());
        }
        for (String option : optionals) {
            if (jsonNode.hasNonNull(option)) {
                ret.put(option, jsonNode.get(option).asText());
            }
        }
        return ret;
    }

    public Map<String, Object> readRequestJson(JsonNode jsonNode, String[] parameters, String[] optionals) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        for (String param : parameters) {
            ret.put(param, jsonNode.get(param).asText());
        }
        for (String option : optionals) {
            if (jsonNode.hasNonNull(option)) {
                ret.put(option, jsonNode.get(option).asText());
            }
        }
        return ret;
    }

    public Map<String, Object> readRequestJsonExcepts(JsonNode jsonNode, String[] parameters, String[] optionals, String excepts) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        for (String param : parameters) {
            if (!param.equals(excepts)) {
                ret.put(param, jsonNode.get(param).asText());
            }
        }
        for (String option : optionals) {
            if (jsonNode.hasNonNull(option) && !option.equals(excepts)) {
                ret.put(option, jsonNode.get(option).asText());
            }
        }
        return ret;
    }


    public List<Map<String, Object>> readRequestJsonWithCourses(JsonNode jsonNode, String[] parameters, String[] optionals, String courseToken) throws Exception {
        List<Map<String, Object>> retList = new LinkedList<>();
        JsonNode courseJson = jsonNode.get(courseToken);
        if (courseJson.isArray()) {
            for (JsonNode courseName : courseJson) {
                Map<String, Object> ret = readRequestJsonExcepts(jsonNode, parameters, optionals, courseToken);
                ret.put(courseToken, courseName.asText());
                retList.add(ret);
            }
        } else {
            retList.add(readRequestJson(jsonNode, parameters, optionals));
        }
        return retList;
    }

    Map<String, Object> loadKeys(JsonNode dataNode, String[] resultKeys) {
        Map<String, Object> ret = new HashMap<>();
        for (String key : resultKeys) {
            ret.put(key, dataNode.get(key));
        }
        return ret;
    }
}
