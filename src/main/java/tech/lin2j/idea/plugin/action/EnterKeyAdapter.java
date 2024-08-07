package tech.lin2j.idea.plugin.action;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 *
 * @author linjinjia
 * @date 2024/8/7 21:09
 */
public abstract class EnterKeyAdapter extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) {
            return;
        }
        doAction(e);
    }

    protected abstract void doAction(KeyEvent e);
}
