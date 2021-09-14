package backend.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backend.service.UserService;

@RestController
public class LoginController {
    private UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    private String errorString(String msg) {
        return String.format("{\"code\" : \"-1\", \"msg\" : \"%s\"}", msg);
    }

    private String loginSucceedString(String token) {
        return String.format("{\"code\" : \"0\",\"msg\" : \"succeed\",\"token\" : %s}", token);
    }

    private String succeedString() {
        return "{\"code\" : \"0\", \"msg\" : \"succeed\"}";
    }

    @PostMapping("/api/login")
    public String login(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        String username, password;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            username = jsonNode.get("username").asText();
            password = jsonNode.get("password").asText();
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            String token = userService.getUserToken(username, password);
            return loginSucceedString(token);
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }
    }

    @PostMapping("/api/changePassword")
    public String changePassword(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        String token;
        try {
            token = request.getHeader("Authorization");
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求头验证信息错误");
        }
        String oldPassword, newPassword;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            oldPassword = jsonNode.get("oldpassword").asText();
            newPassword = jsonNode.get("newpassword").asText();
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }
        try {
            userService.changePassword(token, oldPassword, newPassword);
            return succeedString();
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }
    }

    @PostMapping("/api/register")
    public String register(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        String username, password;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            username = jsonNode.get("username").asText();
            password = jsonNode.get("password").asText();
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            userService.tryRegister(username, password);
            return succeedString();
        } catch (Exception e) {
            response.setStatus(401);
            return errorString(e.getMessage());
        }
    }
}
