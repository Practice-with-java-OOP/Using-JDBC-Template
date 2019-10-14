package com.jidian.cosalon.migration.pos365.repository;

import com.jidian.cosalon.migration.pos365.thread.MyThreadStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TaskRepository {

    @Getter
    private ConcurrentMap<String, MyThreadStatus> threadMap;

    @Getter
    @Setter
    private String sessionId;

    @PostConstruct
    public void init() {
        threadMap = new ConcurrentHashMap<>();
    }


    public Map<String, MyThreadStatus> findAll() {
        return threadMap;
    }

    public void updateThread(String name, MyThreadStatus status) {
        threadMap.put(name, status);
    }
}
