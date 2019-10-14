package com.jidian.cosalon.migration.pos365.service;

import com.jidian.cosalon.migration.pos365.thread.MyThread;
import com.jidian.cosalon.migration.pos365.thread.MyThreadStatus;
import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.dto.BranchResponse;
import com.jidian.cosalon.migration.pos365.dto.LoginRequest;
import com.jidian.cosalon.migration.pos365.dto.LoginResponse;
import com.jidian.cosalon.migration.pos365.repository.BranchJpaRepository;
import com.jidian.cosalon.migration.pos365.repository.TaskRepository;
import com.jidian.cosalon.migration.pos365.retrofitservice.Pos365RetrofitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Retrofit;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BranchJpaRepository branchJpaRepository;

    @Autowired
    private Retrofit retrofit;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Pos365RetrofitService pos365RetrofitService;

    @PostConstruct
    private void init() {
        pos365RetrofitService = retrofit.create(Pos365RetrofitService.class);
    }

//    public HttpHeaders getHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.add("Cookie", "ss-id=" + Utils.SESSION_ID);
//        return headers;
//    }
//
//    public MultiValueMap<String, String> getMapHeaders() {
//        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
//        headers.add("ss-id", Utils.SESSION_ID);
//        return headers;
//    }

    public Map<String, String> getMapHeaders2() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.put("Cookie", "ss-id=" + Utils.SESSION_ID + ";ss-pid=EHRpxOv1STmpoZkmWLF4");
        return headers;
    }

//    public Boolean createFetchingTask() {
//        taskExecutor.execute(new MyThread() {
//            @Override
//            public void run() {
//                if (Utils.SESSION_ID.isEmpty()) {
//                    LoginResponse response = restTemplate.postForObject("https://cosalon.pos365.vn/api/auth/credentials?format=json",
//                            new LoginRequest("admin", "Cosalon@2019"),
//                            LoginResponse.class);
//                    if (response != null) {
//                        Utils.SESSION_ID = response.getSessionId();
//                    }
//                    LOGGER.info(response != null ? response.toString() : null);
//                }
//
////                BaseResponse<BranchResponse> response = restTemplate.getForObject(String.format("https://cosalon.pos365.vn/api/branchs?format=json"),
////                        new BaseResponse<BranchResponse>() {}.getClass());
////                LOGGER.debug("Response: {}", response);
//
//                ResponseEntity<? extends BaseResponse<BranchResponse>> responseEntity = restTemplate.exchange(String.format("https://cosalon.pos365.vn/api/branchs?format=json"),
//                        HttpMethod.GET, new HttpEntity<>("parameters", getHeaders()),
//                        new BaseResponse<BranchResponse>(){}.getClass());
//                LOGGER.info("Response: {}", responseEntity);
//
////                branchJpaRepository.saveAll(responseEntity.getBody().getResults());
//
////                try {
////                    Thread.sleep(5000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//            }
//        });
//        return null;
//    }

    @Autowired
    @Qualifier("branchThread")
    private MyThread branchThread;

    public Boolean createFetchingTask() throws Exception {
        if (Utils.SESSION_ID.isEmpty()) {
            LoginResponse loginResponse = pos365RetrofitService.login(new LoginRequest("admin", "Cosalon@2019")).execute().body();
            if (loginResponse != null) {
                Utils.SESSION_ID = loginResponse.getSessionId();
            }
            LOGGER.info("loginResponse: {}", loginResponse != null ? loginResponse.toString() : null);
        }

        taskExecutor.execute(branchThread);
        return true;
    }

    public Map<String, MyThreadStatus> findAll() {
        return taskRepository.findAll();
    }
}
