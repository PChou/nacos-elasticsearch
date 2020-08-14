package com.eoi.nacos.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public class RefreshTask implements Runnable {
    private static final Logger logger = LogManager.getLogger(RefreshTask.class);

    private final NacosClient nacosClient;

    public RefreshTask(NacosClient nacosClient) {
        this.nacosClient = nacosClient;
    }

    @Override
    public void run() {
        try {
            SpecialPermission.check();
            AccessController.doPrivileged((PrivilegedExceptionAction<Boolean>) () -> {
                nacosClient.refresh();
                return true;
            });
        } catch (Exception ex) {
            logger.warn("Failed to do refresh to nacos", ex);
        }
    }
}
