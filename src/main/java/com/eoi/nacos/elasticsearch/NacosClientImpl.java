package com.eoi.nacos.elasticsearch;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.api.naming.NamingResponseCode;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.client.naming.beat.BeatInfo;
import com.alibaba.nacos.client.naming.utils.InitUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Properties;

public class NacosClientImpl implements NacosClient {
    private static final Logger logger = LogManager.getLogger(NacosClientImpl.class);

    private final String server;
    private final int port;
    private final String user;
    private final String password;
    private final String groupName = Constants.DEFAULT_GROUP;

    private NamingProxy serverProxy;
    private boolean lightBeatEnabled = false;

    public NacosClientImpl(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, String.format("%s:%d", server, port));
        if (user != null && !user.isEmpty()) {
            properties.put(PropertyKeyConst.USERNAME, user);
        }
        if (password != null && !password.isEmpty()) {
            properties.put(PropertyKeyConst.PASSWORD, password);
        }
        String namespace = InitUtils.initNamespaceForNaming(properties);
        String serverList = properties.getProperty(PropertyKeyConst.SERVER_ADDR);
        String endpoint = InitUtils.initEndpoint(properties);
        if (StringUtils.isNotEmpty(endpoint)) {
            serverList = "";
        }
        this.serverProxy = new NamingProxy(namespace, endpoint, serverList, properties);
    }

    @Override
    public void registerNode(String serviceName, String Ip, int port, Map<String, String> metadata)
            throws RegisterException {
        try {
            String groupedServiceName = NamingUtils.getGroupedName(serviceName, groupName);
            Instance instance = new Instance();
            instance.setIp(Ip);
            instance.setPort(port);
            instance.setMetadata(metadata);

            BeatInfo beatInfo = new BeatInfo();
            beatInfo.setServiceName(groupedServiceName);
            beatInfo.setIp(instance.getIp());
            beatInfo.setPort(instance.getPort());
            beatInfo.setMetadata(instance.getMetadata());
            JsonNode result = serverProxy.sendBeat(beatInfo, lightBeatEnabled);
            if (result.has(CommonParams.LIGHT_BEAT_ENABLED)) {
                lightBeatEnabled = result.get(CommonParams.LIGHT_BEAT_ENABLED).asBoolean();
            }
            int code = NamingResponseCode.OK;
            if (result.has(CommonParams.CODE)) {
                code = result.get(CommonParams.CODE).asInt();
            }
            if (code == NamingResponseCode.RESOURCE_NOT_FOUND) {
                serverProxy.registerService(beatInfo.getServiceName(),
                        NamingUtils.getGroupName(beatInfo.getServiceName()), instance);
            }
        } catch (Exception ex) {
            throw new RegisterException(String.format("Failed to send beat or register to nacos %s:%d", this.server, this.port), ex);
        }
    }

    @Override
    public void refresh() throws RefreshExecption {
        try {
            serverProxy.refreshSrvIfNeed();
        } catch (Exception ex) {
            throw new RefreshExecption("Failed to refresh servers endpoint", ex);
        }
    }

    @Override
    public void close() throws Exception {
        if (this.serverProxy != null) {
            this.serverProxy.shutdown();
        }
    }
}
