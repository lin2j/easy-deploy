package tech.lin2j.idea.plugin.file;

import java.io.IOException;

/**
 * @author linjinjia
 * @date 2022/12/11 20:47
 */
@FunctionalInterface
public interface FileAction<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws IOException IOException
     */
    void execute(T t) throws Exception;
}