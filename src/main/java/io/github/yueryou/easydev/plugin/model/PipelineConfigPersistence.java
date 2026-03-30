package io.github.yueryou.easydev.plugin.model;

import com.intellij.openapi.application.ApplicationManager;
import tech.lin2j.idea.plugin.model.ConfigPersistence;

import java.util.List;
import java.util.UUID;

/**
 * 流水线配置持久化工具类
 *
 * 注意：从 2026-03-30 版本开始，Server 配置从 Pipeline 级别移动到步骤级别。
 * 旧版本的 Pipeline 配置中的 serverId 字段在加载时会被忽略，用户需要重新编辑
 * 每个包含 UPLOAD 或 REMOTE_COMMAND 步骤的流水线，为每个步骤配置 Server。
 */
public class PipelineConfigPersistence {

    // 禁止实例化
    private PipelineConfigPersistence() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取所有流水线
     */
    public static List<Pipeline> getAllPipelines() {
        ConfigPersistence persistence = ApplicationManager.getApplication().getService(ConfigPersistence.class);
        return persistence.getPipelines();
    }

    /**
     * 根据 ID 获取流水线
     */
    public static Pipeline getPipelineById(String id) {
        return getAllPipelines().stream()
                .filter(p -> p.getId() != null && p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据 UID 获取流水线
     */
    public static Pipeline getPipelineByUid(String uid) {
        return getAllPipelines().stream()
                .filter(p -> uid.equals(p.getUid()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加流水线
     */
    public static void addPipeline(Pipeline pipeline) {
        ConfigPersistence persistence = ApplicationManager.getApplication().getService(ConfigPersistence.class);

        // 生成 UID
        if (pipeline.getUid() == null) {
            pipeline.setUid(UUID.randomUUID().toString());
        }

        // 生成 ID (使用时间戳确保唯一性)
        if (pipeline.getId() == null) {
            pipeline.setId(String.valueOf(System.currentTimeMillis()));
        }

        persistence.getPipelines().add(pipeline);
    }

    /**
     * 更新流水线
     */
    public static void updatePipeline(Pipeline pipeline) {
        // 更新操作由调用者直接修改对象属性后自动生效
        // 因为 ConfigPersistence 使用的是引用
    }

    /**
     * 删除流水线
     */
    public static void removePipeline(Pipeline pipeline) {
        ConfigPersistence persistence = ApplicationManager.getApplication().getService(ConfigPersistence.class);
        persistence.getPipelines().remove(pipeline);
    }
}
