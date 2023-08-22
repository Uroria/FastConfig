package com.uroria.fastconfig.internal;

import com.uroria.fastconfig.internal.utils.JsonUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unchecked")
public class FileData {
    private final Object2ObjectSortedMap<String, Object> localMap;

    public FileData(Map<String, Object> map) {
        this.localMap = new Object2ObjectLinkedOpenHashMap<>();

        this.localMap.putAll(map);
    }

    public FileData(JSONObject jsonObject) {
        this.localMap = new Object2ObjectLinkedOpenHashMap<>(jsonObject.toMap());
    }

    public void clear() {
        this.localMap.clear();
    }

    public void loadData(Map<String, Object> map) {
        clear();

        if (map != null) {
            this.localMap.putAll(map);
        }
    }

    public Object get(String key) {
        String[] parts = key.split("\\.");
        return get(this.localMap, parts, 0);
    }

    private Object get(Map<String, Object> map, String[] key, int id) {
        if (id < key.length - 1) {
            if (map.get(key[id]) instanceof Map) {
                Map<String, Object> tempMap = (Map<String, Object>) map.get(key[id]);
                return get(tempMap, key, id + 1);
            } else {
                return null;
            }
        } else {
            return map.get(key[id]);
        }
    }

    public synchronized void insert(String key, Object value) {
        String[] parts = key.split("\\.");
        this.localMap.put(parts[0], this.localMap.containsKey(parts[0]) && this.localMap.get(parts[0]) instanceof Map
                        ? insert((Map<String, Object>) this.localMap.get(parts[0]), parts, value, 1)
                        : insert(createNewMap(), parts, value, 1));
    }

    private Object insert(Map<String, Object> map, String[] key, Object value, int id) {
        if (id < key.length) {
            Map<String, Object> tempMap = createNewMap(map);
            Map<String, Object> childMap = map.containsKey(key[id]) && map.get(key[id]) instanceof Map ? (Map<String, Object>) map.get(key[id]) : createNewMap();
            tempMap.put(key[id], insert(childMap, key, value, id + 1));
            return tempMap;
        } else {
            return value;
        }
    }

    public boolean containsKey(String key) {

        String[] parts = key.split("\\.");
        return containsKey(this.localMap, parts, 0);
    }

    private boolean containsKey(Map<String, Object> map, String[] key, int id) {
        if (id < key.length - 1) {
            if (map.containsKey(key[id]) && map.get(key[id]) instanceof Map) {
                Map<String, Object> tempMap = (Map<String, Object>) map.get(key[id]);
                return containsKey(tempMap, key, id + 1);
            } else {
                return false;
            }
        } else {
            return map.containsKey(key[id]);
        }
    }

    public synchronized void remove(String key) {
        if (containsKey(key)) {
            String[] parts = key.split("\\.");
            remove(parts);
        }
    }

    private void remove(@NotNull String[] key) {
        if (key.length == 1) {
            this.localMap.remove(key[0]);
        } else {

            Object tempValue = this.localMap.get(key[0]);
            if (tempValue instanceof Map) {
                //noinspection unchecked
                this.localMap.put(key[0], this.remove((Map) tempValue, key, 1));
                if (((Map) this.localMap.get(key[0])).isEmpty()) {
                    this.localMap.remove(key[0]);
                }
            }
        }
    }

    private Map<String, Object> remove(

            Map<String, Object> map,

            String[] key,

            int keyIndex) {
        if (keyIndex < key.length - 1) {

            Object tempValue = map.get(key[keyIndex]);
            if (tempValue instanceof Map) {
                //noinspection unchecked
                map.put(key[keyIndex], this.remove((Map) tempValue, key, keyIndex + 1));
                if (((Map) map.get(key[keyIndex])).isEmpty()) {
                    map.remove(key[keyIndex]);
                }
            }
        } else {
            map.remove(key[keyIndex]);
        }
        return map;
    }

    public Set<String> singleLayerKeySet() {
        return this.localMap.keySet();
    }

    public Set<String> singleLayerKeySet(
            String key) {
        return get(key) instanceof Map
                ? ((Map<String, Object>) get(key)).keySet()
                : new ObjectArraySet<>();
    }

    public Set<String> keySet() {
        return multiLayerKeySet(this.localMap);
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return multiLayerEntrySet(this.localMap);
    }

    public Set<Map.Entry<String, Object>> singleLayerEntrySet() {
        return this.localMap.entrySet();
    }

    public Set<String> keySet(String key) {
        return get(key) instanceof Map ? multiLayerKeySet((Map<String, Object>) get(key)) : new ObjectArraySet<>();
    }

    private Set<String> multiLayerKeySet(Map<String, Object> map) {
        Set<String> out = new ObjectArraySet<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                for (String tempKey : multiLayerKeySet((Map<String, Object>) entry.getValue())) {
                    out.add(entry.getKey() + "." + tempKey);
                }
            } else {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    private Set<Map.Entry<String, Object>> multiLayerEntrySet(Map<String, Object> map) {
        Set<Map.Entry<String, Object>> out = new ObjectArraySet<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (map.get(entry.getKey()) instanceof Map) {
                for (String tempKey : multiLayerKeySet((Map<String, Object>) map.get(entry.getKey()))) {
                    out.add(new AbstractMap.SimpleEntry<>(entry.getKey() + "." + tempKey, entry.getValue()));
                }
            } else {
                out.add(entry);
            }
        }
        return out;
    }

    public int singleLayerSize() {
        return this.localMap.size();
    }

    public int singleLayerSize(String key) {
        return get(key) instanceof Map ? ((Map) get(key)).size() : 0;
    }

    public int size() {
        return this.localMap.size();
    }

    public int size(String key) {
        return this.localMap.size();
    }

    public void putAll(Map<String, Object> map) {
        this.localMap.putAll(map);
    }

    private int size(Map<String, Object> map) {
        int size = map.size();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                size += size((Map<String, Object>) entry.getValue());
            }
        }
        return size;
    }

    public Map<String, Object> toMap() {
        return Objects.requireNonNullElseGet(this.localMap, Object2ObjectLinkedOpenHashMap::new);
    }

    public JSONObject toJsonObject() {
        return JsonUtils.getJsonFromMap(this.localMap);
    }

    public Map<String, Object> createNewMap() {
        return new Object2ObjectLinkedOpenHashMap<>();
    }

    public Map<String, Object> createNewMap(Map<String, Object> value) {
        return new Object2ObjectLinkedOpenHashMap<>(value);
    }

    @Override
    public int hashCode() {
        return this.localMap.hashCode();
    }

    @Override
    public String toString() {
        return this.localMap.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            FileData fileData = (FileData) obj;
            return this.localMap.equals(fileData.localMap);
        }
    }
}
