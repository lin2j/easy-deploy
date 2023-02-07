package tech.lin2j.idea.plugin.file;

import org.junit.Test;

/**
 * @author linjinjia
 * @date 2022/12/11 00:00
 */
public class FilterTest {

    @Test
    public void testExtensionFilter() {
        String extensions = "*.iml;.log;****.bat";
        ExtensionFilter fileFilter = new ExtensionFilter(extensions);
        String[] suffix = {"bat", "log", "iml"};
        for (String s : suffix) {
            assert fileFilter.getExtensionSet().contains(s);
        }
        assert !fileFilter.accept("abc.iml");
        assert fileFilter.accept("abc.txt");
        assert fileFilter.accept("abc.");
    }
}