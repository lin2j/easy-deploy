package tech.lin2j.idea.plugin.model;

/**
 * @author linjinjia
 * @date 2022/5/7 09:19
 */
public class SeparatorCommand extends Command {

    public static final SeparatorCommand INSTANCE = new SeparatorCommand();

    public SeparatorCommand() {
        setTitle("---------------- Sharable ----------------");
    }

    @Override
    public String toString() {
        return "Sharable Separator";
    }
}