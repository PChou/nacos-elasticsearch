package com.eoi.nacos.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ExecutorBuilder;
import org.elasticsearch.threadpool.FixedExecutorBuilder;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NacosPlugin extends Plugin {

    protected final Settings settings;
    protected final boolean enabled;

    public NacosPlugin(final Settings settings) {
        this.settings = settings;
        this.enabled = NacosPluginSettings.NACOS_ENABLED.get(settings);
    }

    /**
     * tell supported settings
     * @return
     */
    @Override
    public List<Setting<?>> getSettings() {
        return NacosPluginSettings.getSettings();
    }

    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool,
                                               ResourceWatcherService resourceWatcherService, ScriptService scriptService,
                                               NamedXContentRegistry xContentRegistry, Environment environment,
                                               NodeEnvironment nodeEnvironment, NamedWriteableRegistry namedWriteableRegistry) {
        return Collections.singletonList(new NacosRegisterComponent(settings, client, threadPool, nodeEnvironment));
    }

    @Override
    public List<ExecutorBuilder<?>> getExecutorBuilders(Settings settings) {
        if (enabled) {
            final FixedExecutorBuilder builder =
                    new FixedExecutorBuilder(
                            settings,
                            NacosRegisterComponent.NACOS_EXECUTOR_NAME,
                            1,
                            100,
                            "nacos.register");
            return Collections.singletonList(builder);
        }
        return Collections.emptyList();
    }
}
