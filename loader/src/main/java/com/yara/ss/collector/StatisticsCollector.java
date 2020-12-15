package com.yara.ss.collector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsCollector {

    private final Map<String, Integer> map = new HashMap<>();

    public void collectStatistics(List<?> list) {
        String collectionGenericType = list.get(0).getClass().getSimpleName();
        int collectionSize = list.size();
        map.put(collectionGenericType, collectionSize);
    }

    public Map<String, Integer> getStatistics() {
        return map;
    }
}
