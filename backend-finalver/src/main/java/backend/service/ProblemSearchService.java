package backend.service;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.dao.ProblemSearchDAO;
import backend.pojo.ProblemEntity;
import backend.pojo.ProblemSearch;
import lombok.extern.slf4j.Slf4j;

class SearchKeyNotFoundException extends Exception {
    SearchKeyNotFoundException(String msg) {
        super(msg);
    }
}

@Service
@Slf4j
public class ProblemSearchService {
    private ProblemSearchDAO problemSearchDAO;

    @Autowired
    public ProblemSearchService(ProblemSearchDAO problemSearchDAO) {
        this.problemSearchDAO = problemSearchDAO;
    }

    public void saveProblemEntities(String searchKey, List<ProblemEntity> problemEntities) {
        List<Integer> idList = new LinkedList<>();
        for (ProblemEntity problemEntity : problemEntities) {
            idList.add(problemEntity.getId());
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String result = mapper.writeValueAsString(idList);
            ProblemSearch problemSearch = new ProblemSearch();
            problemSearch.setKeyString(searchKey);
            problemSearch.setResultString(result);
            problemSearchDAO.save(problemSearch);
        } catch (Exception e) {
            log.error("saving problemEntities meets unknown error");
        }
    }

    public List<Integer> getProblemIds(String searchKey) throws SearchKeyNotFoundException {
        ProblemSearch problemSearch = problemSearchDAO.findProblemSearchByKeyString(searchKey);
        if (problemSearch == null) {
            throw new SearchKeyNotFoundException(String.format("%s not found", searchKey));
        }
        List<Integer> idList = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(problemSearch.getResultString());
            for (JsonNode singleJson : jsonNode) {
                idList.add(singleJson.asInt());
            }
            return idList;
        } catch (JsonProcessingException e) {
            problemSearchDAO.delete(problemSearch);
            log.warn(String.format("search information of %s deleted.", searchKey));
            throw new SearchKeyNotFoundException("JsonError");
        } catch (Exception e) {
            throw new SearchKeyNotFoundException(e.getMessage());
        }
    }
}
