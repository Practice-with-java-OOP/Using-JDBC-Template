package com.jidian.cosalon.migration.pos365.thread;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.repository.TaskRepository;
import com.jidian.cosalon.migration.pos365.retrofitservice.Pos365RetrofitService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//@Component
//@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public abstract class MyThread implements Runnable {

    @Autowired
    protected Retrofit retrofit;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Getter
    protected MyThreadStatus status = MyThreadStatus.IDLE;

    protected Pos365RetrofitService pos365RetrofitService;

    @PostConstruct
    public void init() {
        status = MyThreadStatus.IDLE;
        pos365RetrofitService = retrofit.create(Pos365RetrofitService.class);
        taskRepository.updateThread(getName(), status);
    }

    @PreDestroy
    public void destroy() {
        status = MyThreadStatus.DESTROYED;
    }

    public abstract String getName();

    public Map<String, String> getMapHeaders2() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.put("Cookie", "ss-id=" + Utils.SESSION_ID + ";ss-pid=EHRpxOv1STmpoZkmWLF4");
        return headers;
    }

    @Override
    public void run() {
        try {
            status = MyThreadStatus.RUNNING;
            taskRepository.updateThread(getName(), status);
            doRun();
            status = MyThreadStatus.IDLE;
            taskRepository.updateThread(getName(), status);
        } catch (Exception e) {
            e.printStackTrace();
            status = MyThreadStatus.IDLE;
            taskRepository.updateThread(getName(), status);
        }
    }

    public abstract void doRun();
}
