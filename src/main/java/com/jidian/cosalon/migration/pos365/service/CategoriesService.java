package com.jidian.cosalon.migration.pos365.service;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.dto.LoginRequest;
import com.jidian.cosalon.migration.pos365.dto.LoginResponse;
import com.jidian.cosalon.migration.pos365.repository.CategoriesRepository;
import com.jidian.cosalon.migration.pos365.retrofitservice.Pos365RetrofitService;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.annotation.PostConstruct;

@Service
public class CategoriesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoriesService.class);

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private CategoriesRepository repository;

    @Autowired
    private Retrofit retrofit;

    private Pos365RetrofitService pos365RetrofitService;

    @PostConstruct
    private void init() {
        pos365RetrofitService = retrofit.create(Pos365RetrofitService.class);
    }

    @Autowired
    @Qualifier("categoriesThread")
    private MyThread categoryThread;

    public Boolean createFetchingCategory() throws Exception {
        if (Utils.SESSION_ID.isEmpty()) {
            Response<LoginResponse> response = pos365RetrofitService.login(new LoginRequest("admin", "Cosalon@2019")).execute();
            if (response.headers() != null && response.headers().values("Set-Cookie") != null) {
                response.headers().values("Set-Cookie").forEach(cookie -> {
                    if (cookie.contains("ss-pid=")) {
                        for (String s: cookie.split(";")) {
                            if (s.contains("ss-pid=")) {
                                Utils.PID = s.trim();
                                break;
                            }
                        }
                    }
                });
            }
            LoginResponse loginResponse = response.body();
            if (loginResponse != null) {
                Utils.SESSION_ID = loginResponse.getSessionId();
            }
            LOGGER.info("loginResponse: {}, Utils.SESSION_ID={}, Utils.PID={}", loginResponse != null ? loginResponse.toString() : null,
                    Utils.SESSION_ID, Utils.PID);
        }

        taskExecutor.execute(categoryThread);
        return true;
    }
}
