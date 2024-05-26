package tech.lin2j.idea.plugin.domain.model;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.ui.ColorUtil;
import tech.lin2j.idea.plugin.enums.SFTPAction;

import java.awt.Color;

/**
 * @author linjinjia
 * @date 2024/5/25 10:11
 */
public class PluginSetting {
    // General

    private boolean updateCheck;

    // SFTP //
    private static final Color defaultUploadColor = ColorProgressBar.BLUE;
    private static final Color defaultDownloadColor = ColorProgressBar.GREEN;

    private String uploadProgressColor;

    private String downloadProgressColor;

    private SFTPAction doubleClickAction;

    public boolean isUpdateCheck() {
        return updateCheck;
    }

    public void setUpdateCheck(boolean updateCheck) {
        this.updateCheck = updateCheck;
    }

    public Color getUploadProgressColor() {
        if (uploadProgressColor == null) {
            uploadProgressColor = ColorUtil.toHex(defaultUploadColor);
        }
        return ColorUtil.fromHex(uploadProgressColor);
    }

    public void setUploadProgressColor(Color uploadProgressColor) {
        this.uploadProgressColor = ColorUtil.toHex(uploadProgressColor);
    }

    public Color getDownloadProgressColor() {
        if (downloadProgressColor == null) {
            downloadProgressColor = ColorUtil.toHex(defaultDownloadColor);
        }
        return ColorUtil.fromHex(downloadProgressColor);
    }

    public void setDownloadProgressColor(Color downloadProgressColor) {
        this.downloadProgressColor = ColorUtil.toHex(downloadProgressColor);
    }

    public SFTPAction getDoubleClickAction() {
        return doubleClickAction;
    }

    public void setDoubleClickAction(SFTPAction doubleClickAction) {
        this.doubleClickAction = doubleClickAction;
    }
}