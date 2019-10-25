package com.jidian.cosalon.migration.pos365.service;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.dto.LoginRequest;
import com.jidian.cosalon.migration.pos365.dto.LoginResponse;
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

//    @Autowired
//    private BranchJpaRepository branchJpaRepository;

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

    @Autowired
    @Qualifier("transfersDetailThread")
    private MyThread transfersDetailThread;

    @Autowired
    @Qualifier("returnDetailThread")
    private MyThread returnDetailThread;

    @Autowired
    @Qualifier("orderDetailThread")
    private MyThread orderDetailThread;

    @Autowired
    @Qualifier("imsWarehouseThread")
    private MyThread imsWarehouseThread;

    @Autowired
    @Qualifier("imsChemicalThread")
    private MyThread imsChemicalThread;

    @Autowired
    @Qualifier("imsSupplierThread")
    private MyThread imsSupplierThread;

    @Autowired
    @Qualifier("imsWarehouseChemicalThread")
    private MyThread imsWarehouseChemicalThread;

    @Autowired
    @Qualifier("imsWarehouseChemicalV2Thread")
    private MyThread imsWarehouseChemicalV2Thread;

    @Autowired
    @Qualifier("imsCustomerSuggestionThread")
    private MyThread imsCustomerSuggestionThread;

    @Autowired
    @Qualifier("imsImportGoodsReceiptThread")
    private MyThread imsImportGoodsReceiptThread;

    @Autowired
    @Qualifier("imsTransferGoodsReceiptThread")
    private MyThread imsTransferGoodsReceiptThread;

    @Autowired
    @Qualifier("imsReturnGoodsReceiptThread")
    private MyThread imsReturnGoodsReceiptThread;

    @Autowired
    @Qualifier("upmsUserThread")
    private MyThread upmsUserThread;

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
        taskExecutor.execute(partnerThread);
        taskExecutor.execute(productOnHandByBranchThread);

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
                LOGGER.info("Executing Order Stock Thread");
                final Future futureOrderStock = taskExecutor.submit(orderStockThread);
                futureOrderStock.get();
                taskExecutor.execute(orderStockDetailThread);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
        taskExecutor.execute(() -> {
            try {
                LOGGER.info("Executing Return Thread");
                final Future futureReturn = taskExecutor.submit(returnThread);
                futureReturn.get();
                taskExecutor.execute(returnDetailThread);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
        taskExecutor.execute(() -> {
            try {
                LOGGER.info("Executing Order Thread");
                final Future futureOrder = taskExecutor.submit(orderThread);
                futureOrder.get();
                taskExecutor.execute(orderDetailThread);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

        taskExecutor.execute(() -> {
            try {
                LOGGER.info("Executing Transfer Thread");
                final Future futureTransfer = taskExecutor.submit(transferThread);
                futureTransfer.get();
                LOGGER.info("Executing Transfer Detail Thread");
                taskExecutor.execute(transfersDetailThread);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

        return true;
    }

    public Map<String, MyThreadStatus> findAll() {
        return taskRepository.findAll();
    }

    public Boolean createMigrationTask() throws Exception {
        taskExecutor.execute(() -> {
            try {
                LOGGER.debug("Executing Product Migration");
                final Future futureWarehouse = taskExecutor.submit(imsWarehouseThread);
                final Future futureChemical = taskExecutor.submit(imsChemicalThread);
                final Future futureSupplier = taskExecutor.submit(imsSupplierThread);

                futureWarehouse.get();
                futureChemical.get();
                futureSupplier.get();

                taskExecutor.execute(imsWarehouseChemicalV2Thread); // haimt: new solution
                final Future futureImport = taskExecutor.submit(imsImportGoodsReceiptThread);
                final Future futureTransfer = taskExecutor.submit(imsTransferGoodsReceiptThread);
                final Future futureReturn = taskExecutor.submit(imsReturnGoodsReceiptThread);

                futureImport.get();
                futureTransfer.get();
                futureReturn.get();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
        taskExecutor.execute(imsCustomerSuggestionThread);
        taskExecutor.execute(upmsUserThread);
        return true;
    }
}
