package backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.dao.ProblemEntityDAO;
import backend.dao.ProblemLinkDAO;
import backend.pojo.ProblemEntity;
import backend.pojo.ProblemLink;
import backend.pojo.ProblemResult;
import lombok.extern.slf4j.Slf4j;

class ProblemNotFoundException extends RuntimeException {
    ProblemNotFoundException(String msg) {
        super(msg);
    }
}

class ProblemIndex {
    private int problemid;
    private int score;
    ProblemIndex(ProblemLink problemLink, Random random) {
        this.score = problemLink.getFailedNum() * 3 - problemLink.getPassNum() * 2;
        if (problemLink.isMarked()) {
            this.score += 5;
        }
        this.score += random.nextInt(4);
        this.problemid = problemLink.getProblemid();
    }

    int getProblemid() {
        return problemid;
    }

    int getScore() {
        return score;
    }
}

@Slf4j
@Service
public class ProblemService {
    private ProblemEntityDAO problemEntityDAO;
    private ProblemLinkDAO problemLinkDAO;
    private ProblemSearchService problemSearchService;
    private NetworkService networkService;
    private Random random;

    private final String url = "http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName";

    @Autowired
    public ProblemService(ProblemEntityDAO problemEntityDAO, ProblemLinkDAO problemLinkDAO, NetworkService networkService, ProblemSearchService problemSearchService) {
        this.problemEntityDAO = problemEntityDAO;
        this.problemLinkDAO = problemLinkDAO;
        this.networkService = networkService;
        this.problemSearchService = problemSearchService;
        this.random = new Random();
    }

    public boolean isMarked(int userid, int problemid) {
        ProblemLink problemLink = problemLinkDAO.findProblemLinkByUseridAndProblemid(userid, problemid);
        if (problemLink == null) {
            return false;
        }
        return problemLink.isMarked();
    }

    public void markProblem(int userid, int problemid) {
        ProblemLink problemLink = problemLinkDAO.findProblemLinkByUseridAndProblemid(userid, problemid);
        if (problemLink == null) {
            problemLink = new ProblemLink();
            problemLink.setProblemid(problemid);
            problemLink.setUserid(userid);
        }
        problemLink.setMarked(true);
        problemLinkDAO.save(problemLink);
    }

    public void unmarkProblem(int userid, int problemid) {
        ProblemLink problemLink = problemLinkDAO.findProblemLinkByUseridAndProblemid(userid, problemid);
        if (problemLink == null) {
            problemLink = new ProblemLink();
            problemLink.setProblemid(problemid);
            problemLink.setUserid(userid);
        }
        problemLink.setMarked(false);
        problemLinkDAO.save(problemLink);
    }

    public ProblemEntity getProblemEntity(int problemid) {
        return problemEntityDAO.findProblemEntityById(problemid);
    }

    public ProblemEntity parseProblemString(int problemid, String fullContext, char qanswer) {
        fullContext = fullContext.replace('．', '.');
        ProblemEntity problemEntity = problemEntityDAO.findProblemEntityById(problemid);
        if (problemEntity != null) {
            return problemEntity;
        }
        problemEntity = new ProblemEntity();
        problemEntity.setId(problemid);
        problemEntity.setQanswer(qanswer);
        Pattern r = Pattern.compile("(.*)(A\\..*)(B\\..*)(C\\..*)(D\\..*)");
        Matcher m = r.matcher(fullContext);
        if (m.find()) {
            problemEntity.setContext(m.group(1));
            problemEntity.setAnswerA(m.group(2));
            problemEntity.setAnswerB(m.group(3));
            problemEntity.setAnswerC(m.group(4));
            problemEntity.setAnswerD(m.group(5));
            problemEntityDAO.save(problemEntity);
            return problemEntity;
        } else {
            return null;
        }
    }

    public void visitProblem(int userid, int problemid) {
        ProblemLink problemLink = problemLinkDAO.findProblemLinkByUseridAndProblemid(userid, problemid);
        if (problemLink == null) {
            problemLink = new ProblemLink();
            problemLink.setUserid(userid);
            problemLink.setProblemid(problemid);
            problemLinkDAO.save(problemLink);
        }
    }

    public void visitProblemEntities(int userid, List<ProblemEntity> problemList) {
        for (ProblemEntity problemEntity : problemList) {
            visitProblem(userid, problemEntity.getId());
        }
    }

    List<ProblemEntity> searchProblemListFromInterNet(String name) throws NetworkException, JsonProcessingException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uriName", name);
        String result = networkService.defaultGetResponse(url, parameters);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(result).get("data");
        List<ProblemEntity> problemEntities = new LinkedList<>();
        for (JsonNode singleJson : jsonNode) {
            try {
                int problemid = singleJson.get("id").asInt();
                char qanswer = 'A';
                String answerBody = singleJson.get("qAnswer").asText();
                if (answerBody.contains("A")) {
                    qanswer = 'A';
                } else if (answerBody.contains("B")) {
                    qanswer = 'B';
                } else if (answerBody.contains("C")) {
                    qanswer = 'C';
                } else {
                    qanswer = 'D';
                }
                String fullContext;
                if (singleJson.has("qbody")) {
                    fullContext = singleJson.get("qbody").asText();
                } else {
                    fullContext = singleJson.get("qBody").asText();
                }
                ProblemEntity problemEntity = parseProblemString(problemid, fullContext, qanswer);
                if (problemEntity != null) {
                    problemEntities.add(problemEntity);
                }
            } catch (Exception e) {
                log.warn(String.format("parse problem meeting error:%s", singleJson.asText()));
            }
        }
        return problemEntities;
    }

    public List<ProblemEntity> getProblemListByName(int userid, String name) throws NetworkException, JsonProcessingException {
        List<ProblemEntity> problemEntities = new LinkedList<>();
        try {
            List<Integer> idList = problemSearchService.getProblemIds(name);
            for (Integer id : idList) {
                ProblemEntity problemEntity = problemEntityDAO.findProblemEntityById(id);
                if (problemEntity != null) {
                    problemEntities.add(problemEntity);
                } else {
                    log.warn("unknown problem when searching {}", name);
                }
            }
        } catch (SearchKeyNotFoundException e) {
            log.info("search keyword {} from internet", name);
            problemEntities = searchProblemListFromInterNet(name);
            problemSearchService.saveProblemEntities(name, problemEntities);
        }

        visitProblemEntities(userid, problemEntities);
        Collections.shuffle(problemEntities);
        return problemEntities;
    }

    public List<ProblemEntity> getMarkedProblems(int userid) {
        List<ProblemLink> problemLinks = problemLinkDAO.findProblemLinkByUserid(userid);
        List<ProblemEntity> problemEntities = new LinkedList<>();
        for (ProblemLink problemLink : problemLinks) {
            if (problemLink.isMarked()) {
                ProblemEntity problemEntity = problemEntityDAO.findProblemEntityById(problemLink.getProblemid());
                problemEntities.add(problemEntity);
            }
        }
        return problemEntities;
    }

    public List<ProblemEntity> getRecommendProblems(int userid, int maxNum) {
        List<ProblemLink> problemLinks = problemLinkDAO.findProblemLinkByUserid(userid);
        ArrayList<ProblemIndex> problemIndexs = new ArrayList<>();
        for (ProblemLink problemLink : problemLinks) {
            ProblemIndex problemIndex = new ProblemIndex(problemLink, random);
            problemIndexs.add(problemIndex);
        }
        problemIndexs.sort(Comparator.comparing(ProblemIndex::getScore));

        List<ProblemEntity> problemEntities = new ArrayList<>();
        for (ProblemIndex problemIndex : problemIndexs) {
            maxNum--;
            if (maxNum < 0) {
                break;
            }
            ProblemEntity problemEntity = problemEntityDAO.findProblemEntityById(problemIndex.getProblemid());
            problemEntities.add(problemEntity);
        }
        return problemEntities;
    }

    public ProblemResult checkResult(int userid, int problemid, char yourAnswer) {
        ProblemEntity problemEntity = problemEntityDAO.findProblemEntityById(problemid);
        if (problemEntity == null) {
            throw new ProblemNotFoundException(String.format("题目%d不存在", problemid));
        }
        ProblemResult problemResult = new ProblemResult();
        problemResult.setProblemid(problemid);
        problemResult.setYourAnswer(yourAnswer);
        problemResult.setStdAnswer(problemEntity.getQanswer());
        ProblemLink problemLink = problemLinkDAO.findProblemLinkByUseridAndProblemid(userid, problemid);
        if (problemLink == null) {
            problemLink = new ProblemLink();
            problemLink.setProblemid(problemid);
            problemLink.setUserid(userid);
        }
        if (problemResult.isPassed()) {
            problemLink.addPass();
        } else {
            problemLink.addFauled();
        }
        return problemResult;
    }
}
