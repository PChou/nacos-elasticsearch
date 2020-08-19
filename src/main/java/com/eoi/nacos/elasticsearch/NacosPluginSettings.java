package com.eoi.nacos.elasticsearch;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.unit.TimeValue;

import java.util.ArrayList;
import java.util.List;

public class NacosPluginSettings {
    /** Setting for enabling or disabling nacos register. Defaults to false. */
    public static final Setting<Boolean> NACOS_ENABLED = Setting.boolSetting("nacos.register.enabled", false, Setting.Property.NodeScope);
    public static final Setting<String> NACOS_SERVICE_NAME = Setting.simpleString("nacos.service.name", "elasticsearch", Setting.Property.NodeScope);
    public static final Setting<TimeValue> NACOS_BEAT_INTERVAL = Setting.timeSetting("nacos.beat.interval", TimeValue.timeValueSeconds(10), Setting.Property.NodeScope);
    public static final Setting<TimeValue> NACOS_REFRESH_INTERVAL = Setting.timeSetting("nacos.refresh.interval", TimeValue.timeValueSeconds(30), Setting.Property.NodeScope);
    public static final Setting<String> NACOS_SERVER = Setting.simpleString("nacos.server.addr", "", Setting.Property.NodeScope);
    public static final Setting<Integer> NACOS_PORT = Setting.intSetting("nacos.server.port", 8848, Setting.Property.NodeScope);
    public static final Setting<String> NACOS_USER = Setting.simpleString("nacos.server.user", "", Setting.Property.NodeScope);
    public static final Setting<String> NACOS_PASSWORD = Setting.simpleString("nacos.server.password", "", Setting.Property.NodeScope);
    public static final Setting<Double> NACOS_NODE_WEIGHT = Setting.doubleSetting("nacos.node.weight", 1.0, 0.0, Setting.Property.NodeScope);

    public static List<Setting<?>> getSettings() {
        List<Setting<?>> settings = new ArrayList<>();
        settings.add(NACOS_ENABLED);
        settings.add(NACOS_BEAT_INTERVAL);
        settings.add(NACOS_REFRESH_INTERVAL);
        settings.add(NACOS_SERVER);
        settings.add(NACOS_PORT);
        settings.add(NACOS_USER);
        settings.add(NACOS_PASSWORD);
        settings.add(NACOS_NODE_WEIGHT);
        return settings;
    }
}
