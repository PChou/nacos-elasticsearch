package com.eoi.nacos.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.env.NodeEnvironment;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegisterTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(RegisterTask.class);

    private final Client client;
    private final NacosClient nacosClient;
    private final NodeEnvironment nodeEnvironment;
    private final String serviceName;

    public RegisterTask(Client client, NacosClient nacosClient, NodeEnvironment nodeEnvironment, String serviceName) {
        this.client = client;
        this.nacosClient = nacosClient;
        this.nodeEnvironment = nodeEnvironment;
        this.serviceName = serviceName;
    }

    @Override
    public void run() {
        try {
            SpecialPermission.check();
            final String nodeId = nodeEnvironment.nodeId();
            final ClusterAdminClient clusterAdminClient = client.admin().cluster();
            final NodesInfoRequestBuilder builder = clusterAdminClient.prepareNodesInfo(nodeId);
            final ActionFuture<NodesInfoResponse> respFuture = clusterAdminClient.nodesInfo(builder.request());
            final NodesInfoResponse response = respFuture.get(5, TimeUnit.SECONDS);
            final TransportAddress httpAddr = response.getNodes().get(0).getHttp().address().publishAddress();
            AccessController.doPrivileged((PrivilegedExceptionAction<Boolean>) () -> {
                nacosClient.registerNode(serviceName, httpAddr.getAddress(), httpAddr.getPort(), new HashMap<>());
                return true;
            });
        } catch (Exception ex) {
            logger.warn("Failed to register or send beat to nacos", ex);
        }
    }
}
