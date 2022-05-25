package tech.lin2j.idea.plugin.event.listener;

import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.domain.model.event.UploadProfileSelectedEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;

import java.util.Objects;

/**
 * @author linjinjia
 * @date 2022/5/24 17:54
 */
public class UploadProfileSelectedListener implements ApplicationListener<UploadProfileSelectedEvent> {
    @Override
    public void onApplicationEvent(UploadProfileSelectedEvent event) {
        UploadProfile profile = (UploadProfile) event.getSource();
        ConfigHelper.getUploadProfileBySshId(profile.getSshId()).forEach(s -> {
            if (!Objects.equals(profile, s)) {
                s.setSelected(false);
            }
        });
    }
}