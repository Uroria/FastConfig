package com.uroria.fastconfig.internal.utils.serialize;

import lombok.NonNull;

public interface ConfSerializable<T> {

    T deserialize(@NonNull Object obj) throws ClassCastException;

    Object serialize(@NonNull T t) throws ClassCastException;

    Class<T> getClazz();
}
