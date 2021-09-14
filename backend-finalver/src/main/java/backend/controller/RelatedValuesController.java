package backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backend.pojo.UriEntity;
import backend.service.NetworkService;
import backend.service.UriLinkService;
import backend.service.UriService;
import backend.service.UserService;

@RestController
public class RelatedValuesController extends BaseRelyController {
    private UriLinkService uriLinkService;
    private UriService uriService;

    @Autowired
    public RelatedValuesController(NetworkService networkService, UserService userService, UriLinkService uriLinkService, UriService uriService) {
        super(networkService, userService);
        this.uriLinkService = uriLinkService;
        this.uriService = uriService;
    }

    @PostMapping("/api/relatedValue")
    public String relatedValue(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        int userid;
        try {
            userid = getUserID(request);
        } catch (Exception e) {
            return errorString(e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        String uri, course, name;
        try {
            JsonNode jsonNode = mapper.readTree(body);
            if (jsonNode.has("uri")) {
                uri = jsonNode.get("uri").asText();
                UriEntity uriEntity = uriService.findEntityInDatabaseByUri(uri);
                if (uriEntity == null) {
                    response.setStatus(400);
                    return errorString("实体不存在于数据集中");
                }
                course = uriEntity.getCourse();
                name = uriEntity.getEntityName();
            } else {
                name = jsonNode.get("name").asText();
                course = jsonNode.get("course").asText();
                List<UriEntity> uriEntities = uriService.findUriEititiesByCourseAndName(course, name);
                if (uriEntities.isEmpty()) {
                    response.setStatus(400);
                    return errorString("未搜索到相关实体");
                }
                uri = uriEntities.get(0).getUri();
            }
        } catch (Exception e) {
            response.setStatus(400);
            return errorString("请求格式错误");
        }

        try {
            Map<String, Object> ret = new HashMap<>();
            ret.put("data", uriLinkService.getRelatedInformation(uri, course, name, userid));
            ret.put("code", "0");
            ret.put("msg", "成功");
            return mapper.writeValueAsString(ret);
        } catch (Exception e) {
            response.setStatus(500);
            return errorString(String.format("服务器网络请求错误: %s", e.getMessage()));
        }
    }
}
