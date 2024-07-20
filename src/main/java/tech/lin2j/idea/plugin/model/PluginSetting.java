package tech.lin2j.idea.plugin.model;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.ui.ColorUtil;
import tech.lin2j.idea.plugin.enums.I18nType;
import tech.lin2j.idea.plugin.enums.SFTPAction;
import tech.lin2j.idea.plugin.uitl.ImportExportUtil;

import java.awt.Color;

/**
 * @author linjinjia
 * @date 2024/5/25 10:11
 */
public class PluginSetting {
    private static final int defaultHeartbeatInterval = 30;
    private static final Color defaultUploadColor = ColorProgressBar.BLUE;
    private static final Color defaultDownloadColor = ColorProgressBar.GREEN;

    // General
    private Integer i18nType;
    private boolean sshKeepalive;
    private Integer heartbeatInterval;

    private boolean updateCheck;

    // SFTP

    private String uploadProgressColor;

    private String downloadProgressColor;

    private SFTPAction doubleClickAction;

    // Export & Import
    private ExportOptions exportOptions;


    public boolean isUpdateCheck() {
        return updateCheck;
    }

    public void setUpdateCheck(boolean updateCheck) {
        this.updateCheck = updateCheck;
    }

    public String getUploadProgressColor() {
        if (uploadProgressColor == null) {
            uploadProgressColor = ColorUtil.toHex(defaultUploadColor);
        }
        return uploadProgressColor;
    }

    public Color uploadProgressColor() {
        String hex = getUploadProgressColor();
        return ColorUtil.fromHex(hex);
    }

    public void setUploadProgressColor(String uploadProgressColor) {
        this.uploadProgressColor = uploadProgressColor;
    }

    public String getDownloadProgressColor() {
        if (downloadProgressColor == null) {
            downloadProgressColor = ColorUtil.toHex(defaultDownloadColor);
        }
        return downloadProgressColor;
    }

    public Color downloadProgressColor() {
        String hex = getDownloadProgressColor();
        return ColorUtil.fromHex(hex);
    }

    public void setDownloadProgressColor(String downloadProgressColor) {
        this.downloadProgressColor = downloadProgressColor;
    }

    public SFTPAction getDoubleClickAction() {
        if (doubleClickAction == null) {
            doubleClickAction = SFTPAction.NONE;
        }
        return doubleClickAction;
    }

    public void setDoubleClickAction(SFTPAction doubleClickAction) {
        this.doubleClickAction = doubleClickAction;
    }

    public boolean isSshKeepalive() {
        return sshKeepalive;
    }

    public void setSshKeepalive(boolean sshKeepalive) {
        this.sshKeepalive = sshKeepalive;
    }

    public Integer getI18nType() {
        if (i18nType == null) {
            i18nType = I18nType.English.getType();
        }
        return i18nType;
    }

    public void setI18nType(Integer i18nType) {
        this.i18nType = i18nType;
    }

    public Integer getHeartbeatInterval() {
        if (heartbeatInterval == null) {
            heartbeatInterval = defaultHeartbeatInterval;
        }
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public ExportOptions getExportOptions() {
        if (exportOptions == null) {
            exportOptions = ImportExportUtil.allExport();
        }
        return exportOptions;
    }

    public void setExportOptions(ExportOptions exportOptions) {
        this.exportOptions = exportOptions;
    }
}