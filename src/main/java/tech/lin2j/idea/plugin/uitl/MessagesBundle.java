/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * ResourceBundleUtils.java is part of Cool Request
 *
 * License: GPL-3.0+
 *
 * Cool Request is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Request is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Request.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.lin2j.idea.plugin.uitl;


import tech.lin2j.idea.plugin.model.ConfigHelper;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * i18n
 *
 * @author linjinjia 2024/5/4 16:13
 */
public class MessagesBundle {

    public static String getText(String key) {
        int languageValue = ConfigHelper.language();
        if (languageValue == -1) return getText(key, Locale.ENGLISH);
        if (languageValue == 0) return getText(key, Locale.ENGLISH);
        if (languageValue == 1) return getText(key, Locale.CHINESE);
        return getText(key, Locale.getDefault());
    }

    private static String getText(String key, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(key);
    }
}
