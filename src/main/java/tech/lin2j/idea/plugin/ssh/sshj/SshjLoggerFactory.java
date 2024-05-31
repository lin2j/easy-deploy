package tech.lin2j.idea.plugin.ssh.sshj;

import net.schmizz.sshj.common.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

public class SshjLoggerFactory implements LoggerFactory {

    private final com.intellij.openapi.diagnostic.Logger logger;
    private final Logger sshjLogger = new SshjLogger();

    public SshjLoggerFactory(com.intellij.openapi.diagnostic.Logger logger) {
        this.logger = logger;
    }

    @Override
    public Logger getLogger(String name) {
        return sshjLogger;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return sshjLogger;
    }

    private class SshjLogger extends MarkerIgnoringBase {
        boolean debugEnable = true;

        private SshjLogger() {
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public void trace(String msg) {
        }

        @Override
        public void trace(String format, Object arg) {
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
        }

        @Override
        public void trace(String format, Object... arguments) {
        }

        @Override
        public void trace(String msg, Throwable t) {
        }

        @Override
        public boolean isDebugEnabled() {
            return debugEnable;
        }

        @Override
        public void debug(String msg) {
        }

        @Override
        public void debug(String format, Object arg) {
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
        }

        @Override
        public void debug(String format, Object... arguments) {
        }

        @Override
        public void debug(String msg, Throwable t) {
        }

        @Override
        public boolean isInfoEnabled() {
            return debugEnable;
        }

        @Override
        public void info(String msg) {
            logger.info(msg);
        }

        @Override
        public void info(String format, Object arg) {
            logger.info(MessageFormatter.format(format, arg).getMessage());
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
            logger.info(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        }

        @Override
        public void info(String format, Object... arguments) {
            logger.info(MessageFormatter.arrayFormat(format, arguments).getMessage());
        }

        @Override
        public void info(String msg, Throwable t) {
            logger.info(msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return debugEnable;
        }

        @Override
        public void warn(String msg) {
            logger.warn(msg);
        }

        @Override
        public void warn(String format, Object arg) {
            logger.warn(MessageFormatter.format(format, arg).getMessage());
        }

        @Override
        public void warn(String format, Object... arguments) {
            logger.warn(MessageFormatter.arrayFormat(format, arguments).getMessage());
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
            logger.warn(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        }

        @Override
        public void warn(String msg, Throwable t) {
            logger.warn(msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return debugEnable;
        }

        @Override
        public void error(String msg) {
            logger.error(msg);
        }

        @Override
        public void error(String format, Object arg) {
            logger.error(MessageFormatter.format(format, arg).getMessage());
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
            logger.error(MessageFormatter.format(format, new Object[]{arg1, arg2}).getMessage());
        }

        @Override
        public void error(String format, Object... arguments) {
            logger.error(MessageFormatter.arrayFormat(format, arguments).getMessage());
        }

        @Override
        public void error(String msg, Throwable t) {
            logger.error(msg, t);
        }
    }
}
