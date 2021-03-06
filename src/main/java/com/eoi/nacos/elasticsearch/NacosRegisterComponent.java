package com.eoi.nacos.elasticsearch;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.threadpool.Scheduler;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Optional;

import static com.eoi.nacos.elasticsearch.NacosPluginSettings.*;

public class NacosRegisterComponent extends AbstractLifecycleComponent {

    public static final String NACOS_EXECUTOR_NAME = "nacos";
    private static final Logger logger = LogManager.getLogger(NacosRegisterComponent.class);

    private final Client client;
    private final ThreadPool threadPool;
    private final NodeEnvironment nodeEnvironment;
    private final Settings settings;
    private final String serviceName;
    private SecureString securePassword;

    private Scheduler.Cancellable beatScheduler;
    private Scheduler.Cancellable refreshScheduler;
    private NacosClient nacosClient;

    public NacosRegisterComponent(Settings settings, Client client, ThreadPool threadPool,
                                  NodeEnvironment nodeEnvironment) {
        this.client = client;
        this.threadPool = threadPool;
        this.nodeEnvironment = nodeEnvironment;
        this.settings = settings;
        this.serviceName = NACOS_SERVICE_NAME.get(settings);
        this.securePassword = NACOS_SECURE_PASSWORD.get(settings);
    }

    @Override
    protected void doStart() {
        boolean enabled = NACOS_ENABLED.get(this.settings);

        if (!enabled) {
            return;
        }
        TimeValue beatInterval = NACOS_BEAT_INTERVAL.get(this.settings);
        TimeValue refreshInterval = NACOS_BEAT_INTERVAL.get(this.settings);
        String namespace = NACOS_SERVICE_NAMESPACE.get(this.settings);
        List<String> server = NACOS_SERVERS.get(settings);
        Double weight = NACOS_NODE_WEIGHT.get(settings);
        if (server == null || server.isEmpty()) {
            logger.warn("Missing `nacos.server.addr`, so that failed to start nacos register loop");
            return;
        }
        String user = NacosPluginSettings.NACOS_USER.get(settings);
        String password;
        if (securePassword == null || securePassword.length() == 0) {
            password = NacosPluginSettings.NACOS_PASSWORD.get(settings);
        } else {
            password = securePassword.toString();
        }
        try {
            final String pwd = password;
            nacosClient = AccessController.doPrivileged((PrivilegedExceptionAction<NacosClient>)
                    () -> new NacosClientImpl(server, namespace, user, pwd));
        } catch (Exception ex) {
            logger.error("Failed to create nacos NamingService", ex);
            return;
        }
        beatScheduler = threadPool.scheduleWithFixedDelay(
                new RegisterTask(client, nacosClient, nodeEnvironment, serviceName,
                        Optional.ofNullable(weight).orElse(1.0)), beatInterval, NACOS_EXECUTOR_NAME);
        refreshScheduler = threadPool.scheduleWithFixedDelay(
                new RefreshTask(nacosClient), refreshInterval, NACOS_EXECUTOR_NAME);
    }

    @Override
    protected void doStop() {
        if (beatScheduler != null && !beatScheduler.isCancelled()) {
            beatScheduler.cancel();
        }
        if (refreshScheduler != null && refreshScheduler.isCancelled()) {
            refreshScheduler.cancel();
        }
        if (nacosClient != null) {
            try {
                nacosClient.close();
            } catch (Exception ex) {}
        }
    }

    @Override
    protected void doClose() throws IOException { }
}
