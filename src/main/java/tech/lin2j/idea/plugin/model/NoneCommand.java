package tech.lin2j.idea.plugin.model;

/**
 * @author linjinjia
 * @date 2022/5/7 09:19
 */
public class NoneCommand extends Command {

    public static final NoneCommand INSTANCE = new NoneCommand();

    public NoneCommand() {
        setTitle("None");
    }

    @Override
    public String toString() {
        return "None";
    }
}