package com.uroria.fastconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.uroria.fastconfig.internal.utils.ClassWrapper;
import com.uroria.fastconfig.internal.utils.serialize.SerializeUtils;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public interface Configuration {
    @Nullable
    Object get(final String key);

    boolean contains(final String key);

    void set(final String key, final Object value);

    Set<String> singleLayerKeySet();

    Set<String> singleLayerKeySet(final String key);

    Set<String> keySet();

    Set<String> keySet(final String key);

    void remove(final String key);

    default <T> Optional<T> find(final String key, final Class<T> type) {
        final Object raw = get(key);
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.of(ClassWrapper.getFromDef(raw, type));
    }

    default <T> void setSerializable(@NonNull final String key, @NonNull final T value) {
        try {
            final Object data = SerializeUtils.serialize(value);
            set(key, data);
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    default <T> T get(final String key, final T def) {
        final Object raw = get(key);
        return raw == null ? def : ClassWrapper.getFromDef(raw, def);
    }

    default String getString(final String key) {
        return getOrDefault(key, "");
    }

    default long getLong(final String key) {
        return getOrDefault(key, 0L);
    }

    default int getInt(final String key) {
        return getOrDefault(key, 0);
    }

    default byte getByte(final String key) {
        return getOrDefault(key, (byte) 0);
    }

    default boolean getBoolean(final String key) {
        return getOrDefault(key, false);
    }

    default float getFloat(final String key) {
        return getOrDefault(key, 0F);
    }

    default double getDouble(final String key) {
        return getOrDefault(key, 0D);
    }

    default List<?> getList(final String key) {
        return getOrDefault(key, new ArrayList<>());
    }

    default <T> List<T> getListParameterized(final String key) {
        return getOrSetDefault(key, new ArrayList<>());
    }

    default List<String> getStringList(final String key) {
        return getOrDefault(key, new ArrayList<>());
    }

    default List<Integer> getIntegerList(final String key) {
        return getOrDefault(key, new ArrayList<>());
    }

    default List<Byte> getByteList(final String key) {
        return getOrDefault(key, new ArrayList<>());
    }

    default List<Long> getLongList(final String key) {
        return getOrDefault(key, new ArrayList<>());
    }

    default Map<?, ?> getMap(final String key) {
        return getOrDefault(key, new HashMap<>());
    }

    default <K, V> Map<K, V> getMapParameterized(final String key) {
        return getOrSetDefault(key, new HashMap<>());
    }

    default <E extends Enum<E>> E getEnum(@NonNull String key, @NonNull Class<E> enumType) {
        final Object object = get(key);
        if (object == null) return null;
        return Enum.valueOf(enumType, (String) object);
    }

    @Nullable
    default <T> T getSerializable(final String key, final Class<T> clazz) {
        if (!contains(key)) {
            return null;
        }
        Object raw = get(key);
        if (raw == null) {
            return null;
        }
        return SerializeUtils.deserialize(raw, clazz);
    }

    @Nullable
    default <T> List<T> getSerializableList(final String key, final Class<T> type) {
        if (!contains(key)) {
            return null;
        }

        final List<?> rawList = getList(key);

        return rawList
                .stream()
                .map(input -> SerializeUtils.deserialize(input, type))
                .collect(Collectors.toList());
    }

    default <T> T getOrDefault(final String key, @NonNull final T def) {
        final Object raw = get(key);
        return raw == null ? def : ClassWrapper.getFromDef(raw, def);
    }

    default void setDefault(final String key, final Object value) {
        if (!contains(key)) {
            set(key, value);
        }
    }

    default <T> T getOrSetDefault(final String key, final T def) {
        final Object raw = get(key);
        if (raw == null) {
            set(key, def);
            return def;
        } else {
            return ClassWrapper.getFromDef(raw, def);
        }
    }
}
