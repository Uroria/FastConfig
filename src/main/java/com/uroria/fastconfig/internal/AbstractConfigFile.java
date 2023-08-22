package com.uroria.fastconfig.internal;

import com.uroria.fastconfig.Configuration;
import com.uroria.fastconfig.FileType;
import com.uroria.fastconfig.internal.utils.FileUtils;
import com.uroria.fastconfig.sections.ConfigFileSection;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractConfigFile implements Configuration, Comparable<AbstractConfigFile> {
    protected final File file;
    protected @Getter final FileType fileType;
    protected @Getter FileData fileData;
    protected String pathPrefix;

    protected AbstractConfigFile(@NonNull String name, @NonNull String path, @NonNull FileType type) {
        this.fileType = type;
        this.file = new File(path.replace("\\", "/") + File.separator + FileUtils.replaceExtensions(name) + "." + type.getExtension());
    }

    protected AbstractConfigFile(@NonNull File file, @NonNull FileType type) {
        this.file = file;
        this.fileType = type;
    }

    protected AbstractConfigFile(@NonNull File file) {
        this.file = file;
        this.fileType = FileType.fromFile(file);
    }

    protected final boolean create() {
        return createFile(this.file);
    }

    private synchronized boolean createFile(File file) {
        if (file.exists()) return false;
        FileUtils.getAndMake(file);
        return true;
    }

    protected abstract Map<String, Object> readToMap() throws IOException;

    protected abstract void write(@NonNull FileData data) throws IOException;

    public final void write() {
        try {
            write(this.fileData);
        } catch (IOException exception) {
            handleReloadException(exception);
        }
    }

    protected final void handleReloadException(IOException exception) {

    }

    @Override
    public final synchronized void set(@NonNull String key, Object value) {
        String finalKey = this.pathPrefix == null ? key : this.pathPrefix + "." + key;
        getFileData().insert(finalKey, value);
        write();
    }

    @Override
    public final Object get(String key) {
        if (key == null) return null;
        final String finalKey = this.pathPrefix == null ? key : this.pathPrefix + "." + key;
        return getFileData().get(finalKey);
    }

    @Override
    public final boolean contains(String key) {
        if (key == null) return false;
        String finalKey = this.pathPrefix == null ? key : this.pathPrefix + "." + key;
        return getFileData().containsKey(finalKey);
    }

    @Override
    public Set<String> singleLayerKeySet() {
        return getFileData().singleLayerKeySet();
    }

    @Override
    public Set<String> singleLayerKeySet(String key) {
        if (key == null) return new ObjectArraySet<>();
        return getFileData().singleLayerKeySet(key);
    }

    @Override
    public Set<String> keySet() {
        return getFileData().keySet();
    }

    @Override
    public Set<String> keySet(String key) {
        if (key == null) return new ObjectArraySet<>();
        return getFileData().keySet(key);
    }

    @Override
    public void remove(String key) {
        if (key == null) return;
        getFileData().remove(key);
        write();
    }

    public final void putAll(@NonNull Map<String, Object> map) {
        getFileData().putAll(map);
        write();
    }

    public final Map<String, Object> getData() {
        return getFileData().toMap();
    }

    public final List<Object> getAll(String... keys) {
        ObjectList<Object> result = new ObjectArrayList<>();
        for (String key : keys) {
            result.add(get(key));
        }
        return result;
    }

    public final void removeAll(String... keys) {
        for (String key : keys) {
            getFileData().remove(key);
        }
        write();
    }

    public final void addDefaultsFromFileData(@NonNull FileData newData) {
        for (String key : newData.keySet()) {
            if (!getFileData().containsKey(key)) {
                getFileData().insert(key, newData.get(key));
            }
        }
        write();
    }

    public final void addDefaultsFromMap(@NonNull Map<String, Object> map) {
        addDefaultsFromFileData(new FileData(map));
    }

    public final String getName() {
        return this.file.getName();
    }

    public final String getFilePath() {
        return this.file.getAbsolutePath();
    }

    public synchronized void replace(@NonNull CharSequence target, @NonNull CharSequence replacement) throws IOException {
        List<String> lines = Files.readAllLines(this.file.toPath());
        ObjectList<String> result = new ObjectArrayList<>();
        for (String line : lines) {
            result.add(line.replace(target, replacement));
        }
        Files.write(this.file.toPath(), result);
    }

    public final void reload() {
        Map<String, Object> map = new Object2ObjectLinkedOpenHashMap<>();
        try {
            map = readToMap();
        } catch (IOException exception) {
            handleReloadException(exception);
        } finally {
            if (getFileData() == null) this.fileData = new FileData(map);
            else getFileData().loadData(map);
        }
    }

    public final void clear() {
        getFileData().clear();
        write();
    }

    public final void clearPathPrefix() {
        this.pathPrefix = null;
    }

    public final ConfigFileSection getSection(@NonNull String pathPrefix) {
        return new ConfigFileSection(this, pathPrefix);
    }

    @Override
    public int compareTo(@NotNull AbstractConfigFile conf) {
        return this.file.compareTo(conf.file);
    }
}
