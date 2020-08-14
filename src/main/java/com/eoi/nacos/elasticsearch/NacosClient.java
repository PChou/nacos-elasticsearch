package com.eoi.nacos.elasticsearch;

import java.util.Map;

public interface NacosClient {

    void registerNode(String serviceName, String host, int Ip, Map<String, String> metadata) throws RegisterException;
    void refresh() throws RefreshExecption;
    void close() throws Exception;

    class RefreshExecption extends Exception {
        public RefreshExecption(String message, Throwable ex) {
            super(message, ex);
        }
    }

    class RegisterException extends Exception {
        public RegisterException(String message, Throwable ex) {
            super(message, ex);
        }
    }
}
