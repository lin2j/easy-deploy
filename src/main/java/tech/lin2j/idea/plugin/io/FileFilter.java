package tech.lin2j.idea.plugin.io;

import java.io.File;

/**
 * FileFilter is an interface used by scp for filtering
 * the set of files uploaded to ssh server
 *
 * @author linjinjia
 * @date 2022/12/9 22:54
 */
public interface FileFilter {

    /**
     * Whether the given file is accepted by this filter.
     *
     * @param f the File to test
     * @return true if the file is to be accepted
     */
    boolean accept(File f);

    /**
     * Whether the given file is accepted by this filter.
     *
     * @param filename the name of File to test
     * @return true if the file is to be accepted
     */
    boolean accept(String filename);
}