package backend.service;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.config.Config;
import lombok.extern.slf4j.Slf4j;

class NetworkException extends Exception {
    NetworkException(String msg) {
        super(msg);
    }
}

@Slf4j
@Service
public class NetworkService {
    private Config config;
    private String defaultID;

    private final String getKnowlegeCardUrl = "http://open.edukg.cn/opedukg/api/typeOpen/open/getKnowledgeCard";

    @Autowired
    public NetworkService(Config config) {
        this.config = config;
        this.defaultID = "0";
    }

    public String getUserID(String phone, String password) throws NetworkException {
        String url = "http://open.edukg.cn/opedukg/api/typeAuth/user/login";
        HttpResponse<String> response;
        try {
            response = Unirest.post(url)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .field("phone", phone)
            .field("password", password)
            .asString();
        } catch (Exception e) {
            throw new NetworkException(e.getMessage());
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());
            String code = jsonNode.get("code").asText();
            String msg = jsonNode.get("msg").asText();
            if (!code.equals("0")) {
                throw new NetworkException(msg);
            }
            return jsonNode.get("id").asText();
        } catch (Exception e) {
            throw new NetworkException(e.getMessage());
        }
    }

    public String getDefaultID() throws NetworkException {
        return getUserID(config.getDefaultPhoneString(), config.getDefaultPassword());
    }

    public String tryPost(String url, Map<String, Object> parameters) throws NetworkException {
        HttpResponse<String> response;
        try {
            response = Unirest.post(url)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .fields(parameters)
            .asString();
            return response.getBody();
        } catch (Exception e) {
            throw new NetworkException(e.getMessage());
        }
    }

    public String tryGet(String url, Map<String, Object> parameters) throws NetworkException {
        HttpResponse<String> response;
        try {
            response = Unirest.get(url)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .queryString(parameters)
            .asString();
            return response.getBody();
        } catch (Exception e) {
            throw new NetworkException(e.getMessage());
        }
    }

    public String postResponse(String url, String phone, String password, Map<String, Object> parameters) throws NetworkException {
        parameters.put("id", getUserID(phone, password));
        return tryPost(url, parameters);
    }

    public String getResponse(String url, String phone, String password, Map<String, Object> parameters) throws NetworkException {
        parameters.put("id", getUserID(phone, password));
        return tryGet(url, parameters);
    }

    public String defaultPostResponse(String url, Map<String, Object> parameters) throws NetworkException {
        parameters.put("id", defaultID);
        String result;
        ObjectMapper mapper = new ObjectMapper();
        try {
            result = tryPost(url, parameters);
            JsonNode jsonNode = mapper.readTree(result);
            if (!"0".equals(jsonNode.get("code").asText())) {
                throw new NetworkException(jsonNode.get("msg").asText());
            }
            return result;
        } catch (Exception e) {
            defaultID = getDefaultID();
            parameters.put("id", defaultID);
            log.info("login id expired?");
        }
        try {
            result = tryPost(url, parameters);
            JsonNode jsonNode = mapper.readTree(result);
            if (!"0".equals(jsonNode.get("code").asText())) {
                throw new NetworkException(jsonNode.get("msg").asText());
            }
            return result;
        } catch (Exception e) {
            log.warn("request failed...");
            throw new NetworkException(e.getMessage());
        }
    }

    public String defaultGetResponse(String url, Map<String, Object> parameters) throws NetworkException {
        parameters.put("id", defaultID);
        String result;
        ObjectMapper mapper = new ObjectMapper();
        try {
            result = tryGet(url, parameters);
            JsonNode jsonNode = mapper.readTree(result);
            if (!"0".equals(jsonNode.get("code").asText())) {
                throw new NetworkException(jsonNode.get("msg").asText());
            }
            return result;
        } catch (Exception e) {
            defaultID = getDefaultID();
            parameters.put("id", defaultID);
            log.info("login id expired?");
        }
        try {
            result = tryGet(url, parameters);
            JsonNode jsonNode = mapper.readTree(result);
            if (!"0".equals(jsonNode.get("code").asText())) {
                throw new NetworkException(jsonNode.get("msg").asText());
            }
            return result;
        } catch (Exception e) {
            log.warn("request failed...");
            throw new NetworkException(e.getMessage());
        }
    }

    public String getNameOfUri(String course, String uri) throws NetworkException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("course", course);
        parameters.put("uri", uri);
        String result = defaultPostResponse(getKnowlegeCardUrl, parameters);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(result);
            return jsonNode.get("data").get("entity_name").asText();
        } catch (Exception e) {
            throw new NetworkException("response json error: " + e.getMessage());
        }
    }
}
