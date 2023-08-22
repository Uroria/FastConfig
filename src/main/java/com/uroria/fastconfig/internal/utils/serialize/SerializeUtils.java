package com.uroria.fastconfig.internal.utils.serialize;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@SuppressWarnings({"unchecked", "rawtypes"})
@UtilityClass
public class SerializeUtils {
    private final ObjectList<ConfSerializable<?>> serializables = ObjectLists.synchronize(new ObjectArrayList<>());

    public boolean isSerializable(Class<?> clazz) {
        return findSerializable(clazz) != null;
    }

    public void registerSerializable(@NonNull ConfSerializable<?> serializable) {
        serializables.add(serializable);
    }

    public ConfSerializable<?> findSerializable(Class<?> clazz) {
        for (ConfSerializable<?> serializable : serializables) {
            if (serializable.getClazz().equals(clazz)) return serializable;
        }
        return null;
    }

    public Object serialize(@NonNull Object object) {
        ConfSerializable serializable = findSerializable(object.getClass());
        return serializable.serialize(object);
    }

    public <T> T deserialize(Object raw, Class<T> type) {
        ConfSerializable<?> serializable = findSerializable(type);
        return (T) serializable.deserialize(raw);
    }
}
