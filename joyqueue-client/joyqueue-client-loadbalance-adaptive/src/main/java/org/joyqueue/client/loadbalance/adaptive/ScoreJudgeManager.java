package org.joyqueue.client.loadbalance.adaptive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * ScoreJudgeManager
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class ScoreJudgeManager {

    private static Map<String, ScoreJudge> judgeMap = new HashMap<>();

    static {
        Map<String, ScoreJudge> judges = loadJudges();
        for (Map.Entry<String, ScoreJudge> entry : judges.entrySet()) {
            judgeMap.put(entry.getKey(), entry.getValue());
        }
    }

    public static ScoreJudge getJudge(String type) {
        return judgeMap.get(type);
    }

    public static List<ScoreJudge> getJudges() {
        return new ArrayList<>(judgeMap.values());
    }

    public static List<ScoreJudge> getJudges(String[] types) {
        if (types == null) {
            return getJudges();
        }
        List<ScoreJudge> result = new ArrayList<>();
        for (String type : types) {
            ScoreJudge judge = judgeMap.get(type);
            if (judge != null) {
                result.add(judge);
            }
        }
        return result;
    }

    protected static Map<String, ScoreJudge> loadJudges() {
        Map<String, ScoreJudge> result = new HashMap<>();
        Iterator<ScoreJudge> iterator = ServiceLoader.load(ScoreJudge.class).iterator();
        while (iterator.hasNext()) {
            ScoreJudge judge = iterator.next();
            result.put(judge.type(), judge);
        }
        return result;
    }
}