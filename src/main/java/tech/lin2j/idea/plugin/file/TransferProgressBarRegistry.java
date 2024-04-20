package tech.lin2j.idea.plugin.file;

import com.intellij.openapi.progress.util.ColorProgressBar;
import tech.lin2j.idea.plugin.ui.table.ProgressCell;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linjinjia
 * @date 2024/4/14 14:41
 */
public class TransferProgressBarRegistry {

    private static final Map<String, ProgressCell> map = new ConcurrentHashMap<>();

    public static void registry(String key, ProgressCell progressBar) {
        map.put(key, progressBar);
    }

    public static @Nullable ProgressCell getByKey(String key) {
        return map.get(key);
    }

    public static void clear() {
        map.clear();
    }

    public static void removeByKey(String key) {
        map.remove(key);
    }
}