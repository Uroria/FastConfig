package com.uroria.fastconfig.sections;

import com.uroria.fastconfig.Configuration;
import com.uroria.fastconfig.internal.AbstractConfigFile;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class ConfigFileSection implements Configuration {
    protected final AbstractConfigFile conf;
    private @Getter final String pathPrefix;

    public ConfigFileSection getSection(String pathPrefix) {
        return new ConfigFileSection(this.conf, createFinalKey(pathPrefix));
    }

    @Override
    public Set<String> singleLayerKeySet() {
        return conf.singleLayerKeySet(pathPrefix);
    }

    @Override
    public Set<String> singleLayerKeySet(String key) {
        return conf.singleLayerKeySet(createFinalKey(key));
    }

    @Override
    public Set<String> keySet() {
        return conf.keySet(pathPrefix);
    }

    @Override
    public Set<String> keySet(String key) {
        return conf.keySet(createFinalKey(key));
    }

    @Override
    public void remove(String key) {
        conf.remove(createFinalKey(key));
    }

    @Override
    public void set(String key, Object value) {
        conf.set(createFinalKey(key), value);
    }

    @Override
    public boolean contains(String key) {
        return conf.contains(createFinalKey(key));
    }

    @Override
    public Object get(String key) {
        return conf.get(createFinalKey(key));
    }

    @Override
    public <E extends Enum<E>> E getEnum(@NonNull String key, @NonNull Class<E> enumType) {
        return conf.getEnum(createFinalKey(key), enumType);
    }

    private String createFinalKey(String key) {
        return pathPrefix == null || pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
    }

}
