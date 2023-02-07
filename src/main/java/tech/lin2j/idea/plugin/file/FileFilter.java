package tech.lin2j.idea.plugin.file;

import java.io.IOException;

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
     * @param filename the name of a file to test
     * @return true if the file is to be accepted
     */
    boolean accept(String filename);

    /**
     * Test whether the file needs to be filtered,
     * and then perform a file operation
     *
     * @param filename the name of a file to test
     * @param action   execute a {@link FileAction}
     * @throws IOException IOException
     */
    default void accept(String filename, FileAction<Boolean> action) throws IOException {
        boolean result = accept(filename);
        action.execute(result);
    }


}