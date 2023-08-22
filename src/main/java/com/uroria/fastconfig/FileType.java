package com.uroria.fastconfig;

import com.uroria.fastconfig.internal.utils.FileUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;

@Getter
@RequiredArgsConstructor
public enum FileType {
    JSON("json"),
    TOML("toml");

    private final String extension;

    public static FileType fromFile(@NonNull File file) {
        return fromExtension(FileUtils.getExtension(file));
    }

    public static FileType fromExtension(@NonNull String extension) {
        for (FileType value : values()) {
            if (value.extension.equals(extension)) return value;
        }
        return null;
    }
}
