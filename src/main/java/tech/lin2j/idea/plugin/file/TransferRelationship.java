package tech.lin2j.idea.plugin.file;

/**
 * @author linjinjia
 * @date 2024/4/13 16:03
 */
public class TransferRelationship {

    private TableFile source;
    private TableFile target;
    private boolean isUpload;

    public TransferRelationship(TableFile source, TableFile target) {
        this(source, target, true);
    }

    public TransferRelationship(TableFile source, TableFile target, boolean isUpload) {
        this.source = source;
        this.target = target;
        this.isUpload = isUpload;
    }

    public String getKey() {
        return source.getFilePath() + ":" + target.getFilePath() + ":" + isUpload;
    }

    public TableFile getSource() {
        return source;
    }

    public void setSource(TableFile source) {
        this.source = source;
    }

    public TableFile getTarget() {
        return target;
    }

    public void setTarget(TableFile target) {
        this.target = target;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }
}