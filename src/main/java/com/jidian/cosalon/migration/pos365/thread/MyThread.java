package com.jidian.cosalon.migration.pos365.thread;

import lombok.Getter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public abstract class MyThread implements Runnable {

//    @Autowired
//    private TaskRepository taskRepository;

    @Getter
    protected MyThreadStatus status = MyThreadStatus.IDLE;

//    @PostConstruct
//    public void init() {
//        status = MyThreadStatus.IDLE;
//    }
//
//    @PreDestroy
//    public void destroy() {
//        status = MyThreadStatus.DESTROYED;
//    }
//
//    @Override
//    public void run() {
//        try {
//            status = MyThreadStatus.RUNNING;
//            taskRepository.updateThread(Thread.currentThread().getName(), status);
//            doRun();
//            status = MyThreadStatus.IDLE;
//            taskRepository.updateThread(Thread.currentThread().getName(), status);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected abstract void doRun();
}
