package tech.lin2j.idea.plugin.uitl;

import net.schmizz.sshj.xfer.FilePermission;
import tech.lin2j.idea.plugin.factory.SshServiceFactory;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linjinjia
 * @date 2024/4/20 23:21
 */
public class PosixUtil {

    /**
     * key: ip_uid, value: user
     */
    private static final Map<String, String> USER_NAME_MAP = new ConcurrentHashMap<>();

    private static final ISshService sshClient = SshServiceFactory.getSshService();

    /**
     * Get the username of the specified server by UID
     *
     * @param server server id
     * @param uid    user id
     * @return user name
     */
    public static String getUser(SshServer server, int uid) {
        String key = server.getIp() + "_" + uid;
        if (USER_NAME_MAP.containsKey(key)) {
            return USER_NAME_MAP.get(key);
        }

        SshStatus status = sshClient.execute(server, "id -nu " + uid);
        if (status.isSuccess()) {
            String user = status.getMessage();
            USER_NAME_MAP.put(key, user);
            return user;
        }
        return "";
    }

    /**
     * Translate permissions into the form of rwx.
     *
     * @param perms permissions
     * @return permissions
     */
    public static String toString(Set<FilePermission> perms) {
        StringBuilder sb = new StringBuilder(9);
        writeBits(
                sb,
                perms.contains(FilePermission.USR_R),
                perms.contains(FilePermission.USR_W),
                perms.contains(FilePermission.USR_X));
        writeBits(
                sb,
                perms.contains(FilePermission.GRP_R),
                perms.contains(FilePermission.GRP_W),
                perms.contains(FilePermission.GRP_X));
        writeBits(
                sb,
                perms.contains(FilePermission.OTH_R),
                perms.contains(FilePermission.OTH_W),
                perms.contains(FilePermission.OTH_X));
        return sb.toString();
    }

    private static void writeBits(StringBuilder sb, boolean r, boolean w, boolean x) {
        if (r) {
            sb.append('r');
        } else {
            sb.append('-');
        }
        if (w) {
            sb.append('w');
        } else {
            sb.append('-');
        }
        if (x) {
            sb.append('x');
        } else {
            sb.append('-');
        }
    }
}