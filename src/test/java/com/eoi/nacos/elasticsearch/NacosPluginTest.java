package com.eoi.nacos.elasticsearch;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Ignore;

import java.util.Collection;
import java.util.Collections;

// -Djava.security.policy=$ProjectFileDir$/src/main/resources/plugin-security.policy
@Ignore
public class NacosPluginTest extends ESIntegTestCase {
    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder()
                .put("nacos.register.enabled", true)
                .put("nacos.server.addr", "192.168.32.25")
                .put("nacos.server.user", "nacos")
                .put("nacos.server.password", "nacos")
                .put(super.nodeSettings(nodeOrdinal))
                .build();
    }

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.singletonList(NacosPlugin.class);
    }

    public void testSleep() throws Exception {
        Thread.sleep(15000);
    }
}
