package com.andy.retry.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.andy.retry.core.model.DelayDetail;

import lombok.extern.slf4j.Slf4j;

/**
 * 延迟队列相关的时间计算工具（支持任意时间长度的延迟策略定制，最小时间精确到秒）
 * @author andy
 *
 */
@Slf4j
public class DelayLevelTool {
    // 延迟级别换算表（key-延迟多少秒；value-延迟级别）
    public static final Map<String, Integer> delayLevelTable = new HashMap<>();
    // 延迟级别对应的seconds有序列表
    // 默认配置是messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
    public static final List<Integer> delayLevelList = Arrays.asList(7200,3600,1800,1200,600,540,480,420,360,300,240,180,120,60,30,10,5,1);
    // 时间单位换算表（key-时间单位；value-对应的秒数）
    public static final HashMap<String, Long> timeUnitTable = new HashMap<String, Long>();

    static {
        timeUnitTable.put("s", 1L);
        timeUnitTable.put("m", 60L);
        timeUnitTable.put("h", 60 * 60L);
        timeUnitTable.put("d", 60 * 60 * 24L);
        delayLevelTable.put("0", 0);
        delayLevelTable.put("1", 1);
        delayLevelTable.put("5", 2);
        delayLevelTable.put("10", 3);
        delayLevelTable.put("30", 4);
        delayLevelTable.put("60", 5);
        delayLevelTable.put("120", 6);
        delayLevelTable.put("180", 7);
        delayLevelTable.put("240", 8);
        delayLevelTable.put("300", 9);
        delayLevelTable.put("360", 10);
        delayLevelTable.put("420", 11);
        delayLevelTable.put("480", 12);
        delayLevelTable.put("540", 13);
        delayLevelTable.put("600", 14);
        delayLevelTable.put("1200", 15);
        delayLevelTable.put("1800", 16);
        delayLevelTable.put("3600", 17);
        delayLevelTable.put("7200", 18);
    }
    
    /**
     * 匹配到最合适的delayLevel，减去该level的对应延迟时间，得出到期后的剩余时间
     * @param seconds
     * @return
     */
    public static DelayDetail getDelayLevel(Long seconds) {
        DelayDetail delayDetail = new DelayDetail();
        Integer suitedLevel = delayLevelTable.get(String.valueOf(seconds));
        if(suitedLevel!=null) {
            delayDetail.setDelayLevel(suitedLevel);
            delayDetail.setRestSeconds(0L);
        } else {
            Integer suitedLevelTime = delayLevelList.stream().filter(e -> e<seconds).findFirst().get();
            delayDetail.setDelayLevel(delayLevelTable.get(suitedLevelTime.toString()));
            delayDetail.setRestSeconds(seconds-suitedLevelTime);
        }
        return delayDetail;
    }
    /**
     * 将带有s、m、h、d的时间字符转换成整数秒
     * @param timeString
     * @return
     */
    public static Long getTimeSeconds(String timeString) {
        try {
            timeString = timeString.toLowerCase();
            Long tu = timeUnitTable.get(timeString.substring(timeString.length() - 1));
            return Long.parseLong(timeString.substring(0, timeString.length() - 1)) * tu;
        } catch (Exception e) {
            log.error("####### convert time error", e);
        }
        return 0L;
    }
    
}
