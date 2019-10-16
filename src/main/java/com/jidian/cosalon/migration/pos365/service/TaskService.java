package com.jidian.cosalon.migration.pos365.service;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.dto.LoginRequest;
import com.jidian.cosalon.migration.pos365.dto.LoginResponse;
import com.jidian.cosalon.migration.pos365.repository.BranchJpaRepository;
import com.jidian.cosalon.migration.pos365.repository.TaskRepository;
import com.jidian.cosalon.migration.pos365.retrofitservice.Pos365RetrofitService;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import com.jidian.cosalon.migration.pos365.thread.MyThreadStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.Future;

@Service
public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BranchJpaRepository branchJpaRepository;

    @Autowired
    private Retrofit retrofit;

    private Pos365RetrofitService pos365RetrofitService;

    @PostConstruct
    private void init() {
        pos365RetrofitService = retrofit.create(Pos365RetrofitService.class);
    }

    @Autowired
    @Qualifier("branchThread")
    private MyThread branchThread;

    @Autowired
    @Qualifier("productThread")
    private MyThread productThread;

    @Autowired
    @Qualifier("userThread")
    private MyThread userThread;

    @Autowired
    @Qualifier("productHistoryThread")
    private MyThread productHistoryThread;

    @Autowired
    @Qualifier("transferThread")
    private MyThread transferThread;

    @Autowired
    @Qualifier("orderThread")
    private MyThread orderThread;

    @Autowired
    @Qualifier("categoriesThread")
    private MyThread categoryThread;

    @Autowired
    @Qualifier("itemsThread")
    private MyThread itemsThread;

    @Autowired
    @Qualifier("orderStockThread")
    private MyThread orderStockThread;

    @Autowired
    @Qualifier("partnerThread")
    private MyThread partnerThread;

    @Autowired
    @Qualifier("productOnHandByBranchThread")
    private MyThread productOnHandByBranchThread;

    @Autowired
    @Qualifier("returnThread")
    private MyThread returnThread;

    @Autowired
    @Qualifier("orderStockDetailThread")
    private MyThread orderStockDetailThread;

    public Boolean createFetchingTask() throws Exception {
        if (Utils.SESSION_ID.isEmpty()) {
            Response<LoginResponse> response = pos365RetrofitService
                    .login(new LoginRequest("admin", "Cosalon@2019")).execute();
            if (response.headers() != null && response.headers().values("Set-Cookie") != null) {
                response.headers().values("Set-Cookie").forEach(cookie -> {
                    if (cookie.contains("ss-pid=")) {
                        for (String s : cookie.split(";")) {
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
            LOGGER.debug("loginResponse: {}, Utils.SESSION_ID={}, Utils.PID={}",
                    loginResponse != null ? loginResponse.toString() : null,
                    Utils.SESSION_ID, Utils.PID);
        }

        taskExecutor.execute(userThread);
        taskExecutor.execute(categoryThread);
        taskExecutor.execute(itemsThread);
        taskExecutor.execute(orderStockThread);
        taskExecutor.execute(transferThread);
        taskExecutor.execute(orderThread);
        taskExecutor.execute(partnerThread);
        taskExecutor.execute(productOnHandByBranchThread);
        taskExecutor.execute(returnThread);

        taskExecutor.execute(() -> {
            try {
                final Future futureBranch = taskExecutor.submit(branchThread);
                final Future futureProduct = taskExecutor.submit(productThread);
                futureBranch.get();
                futureProduct.get();

                taskExecutor.execute(productHistoryThread);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

        taskExecutor.execute(() -> {
            try {
                final Future futureOrderStock = taskExecutor.submit(orderStockThread);
                futureOrderStock.get();
                taskExecutor.execute(orderStockDetailThread);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

        return true;
    }

    public Map<String, MyThreadStatus> findAll() {
        return taskRepository.findAll();
    }
}
