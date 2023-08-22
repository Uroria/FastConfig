package com.uroria.fastconfig;

import com.uroria.fastconfig.internal.AbstractConfigFile;
import com.uroria.fastconfig.internal.FileData;
import com.uroria.fastconfig.internal.utils.FileUtils;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

@Getter
public final class Json extends AbstractConfigFile {

    public Json(@NonNull Json json) {
        super(json.file);
        this.fileData = json.fileData;
        this.pathPrefix = json.pathPrefix;
    }

    public Json(@NonNull String name, @NonNull String path) {
        this(name, path, null);
    }

    public Json(@NonNull String name, @NonNull String path, @Nullable InputStream inputStream) {
        super(name, path, FileType.JSON);
        if ((create() || this.file.length() == 0) && inputStream != null)  {
            FileUtils.writeToFile(this.file, inputStream);
        }
        reload();
    }

    public Json(@NonNull File file) {
        super(file, FileType.JSON);
        create();
        reload();
    }

    @Override
    protected Map<String, Object> readToMap() throws IOException {
        if (this.file.length() == 0) {
            Files.write(this.file.toPath(), Collections.singletonList("{}"));
        }

        JSONTokener jsonTokener = new JSONTokener(FileUtils.createInputStream(this.file));
        return new JSONObject(jsonTokener).toMap();
    }

    @Override
    protected void write(@NonNull FileData data) throws IOException {
        @Cleanup Writer writer = FileUtils.createWriter(this.file);
        writer.write(data.toJsonObject().toString(3));
        writer.flush();
    }
}
