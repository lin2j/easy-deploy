package tech.lin2j.idea.plugin.model;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColorUtil;
import tech.lin2j.idea.plugin.enums.I18nType;
import tech.lin2j.idea.plugin.enums.SFTPAction;
import tech.lin2j.idea.plugin.enums.TransferMode;
import tech.lin2j.idea.plugin.uitl.FileUtil;
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

    private Integer transferMode;
    private boolean updateCheck;

    // SFTP

    private String uploadProgressColor;

    private String downloadProgressColor;

    private SFTPAction doubleClickAction;

    private Integer historyPathSize;

    // Export & Import
    private String defaultExportImportPath;
    private ExportOptions exportOptions;

    // SSH Console Logging
    private boolean sshLogEnabled = false;
    private String sshLogDirectory = "~/logs";
    private int logRetentionDays = 30;

    // SFTP/SCP
    private boolean preferSftpOverScp = true;


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

    public Integer getTransferMode() {
        if (transferMode == null) {
            transferMode = TransferMode.SFTP.getType();
        }
        return transferMode;
    }

    public void setTransferMode(Integer transferMode) {
        this.transferMode = transferMode;
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

    public String getDefaultExportImportPath() {
        if (StringUtil.isEmpty(defaultExportImportPath)) {
            defaultExportImportPath = FileUtil.getHome().getPath();
        }
        return defaultExportImportPath;
    }

    public void setDefaultExportImportPath(String defaultExportImportPath) {
        this.defaultExportImportPath = defaultExportImportPath;
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

    public Integer getHistoryPathSize() {
        if (historyPathSize == null) {
            historyPathSize = 10;
        }
        return historyPathSize;
    }

    public void setHistoryPathSize(Integer historyPathSize) {
        this.historyPathSize = historyPathSize;
    }

    public boolean isSshLogEnabled() {
        return sshLogEnabled;
    }

    public void setSshLogEnabled(boolean sshLogEnabled) {
        this.sshLogEnabled = sshLogEnabled;
    }

    public String getSshLogDirectory() {
        if (sshLogDirectory == null || sshLogDirectory.isEmpty()) {
            sshLogDirectory = "~/logs";
        }
        return sshLogDirectory;
    }

    public void setSshLogDirectory(String sshLogDirectory) {
        this.sshLogDirectory = sshLogDirectory;
    }

    public int getLogRetentionDays() {
        return logRetentionDays > 0 ? logRetentionDays : 30;
    }

    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }

    public boolean isPreferSftpOverScp() {
        return preferSftpOverScp;
    }

    public void setPreferSftpOverScp(boolean preferSftpOverScp) {
        this.preferSftpOverScp = preferSftpOverScp;
    }
}