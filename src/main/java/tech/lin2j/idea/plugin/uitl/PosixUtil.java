package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.util.text.StringUtil;
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

    /**
     * key: ip_user, value: group
     */
    private static final Map<String, String> USER_GROUP_MAP = new ConcurrentHashMap<>();

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
            if (StringUtil.isNotEmpty(user) && user.contains("no such user")) {
                user = uid + "";
            }
            USER_NAME_MAP.put(key, user);
            return user;
        }
        return "";
    }

    /**
     * Get the user group of the specified server by UID
     *
     * @param server server id
     * @param uid    user id
     * @return user name
     */
    public static String getGroup(SshServer server, int uid) {
        String key = server.getIp() + "_" + uid;
        if (USER_GROUP_MAP.containsKey(key)) {
            return USER_GROUP_MAP.get(key);
        }

        SshStatus status = sshClient.execute(server, "id -Gn " + uid);
        if (status.isSuccess()) {
            String group = status.getMessage();
            if (StringUtil.isNotEmpty(group) && group.contains("no such user")) {
                group = uid + "";
            }
            USER_GROUP_MAP.put(key, group);
            return group;
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