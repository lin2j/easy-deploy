package tech.lin2j.idea.plugin.file;

import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.util.text.StringUtil;
import tech.lin2j.idea.plugin.file.fileTypes.ConfigFileType;
import tech.lin2j.idea.plugin.file.fileTypes.DmgFileType;
import tech.lin2j.idea.plugin.file.fileTypes.EbookFileType;
import tech.lin2j.idea.plugin.file.fileTypes.ExcelFileType;
import tech.lin2j.idea.plugin.file.fileTypes.LogFileType;
import tech.lin2j.idea.plugin.file.fileTypes.MusicFileType;
import tech.lin2j.idea.plugin.file.fileTypes.PdfFileType;
import tech.lin2j.idea.plugin.file.fileTypes.PkgFileType;
import tech.lin2j.idea.plugin.file.fileTypes.PptFileType;
import tech.lin2j.idea.plugin.file.fileTypes.SpecifiedArchiveFileType;
import tech.lin2j.idea.plugin.file.fileTypes.VideoFileType;
import tech.lin2j.idea.plugin.file.fileTypes.WordFileType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linjinjia
 * @date 2024/4/21 12:18
 */
public class PluginFileTypeRegistry {

    private static final Map<String, FileType> FILE_TYPE_MAP = new ConcurrentHashMap<>();

    static {
        FILE_TYPE_MAP.put("7z", new SpecifiedArchiveFileType(ArchiveFileType.INSTANCE, "7z"));
        FILE_TYPE_MAP.put("zip", new SpecifiedArchiveFileType(ArchiveFileType.INSTANCE, "zip"));
        FILE_TYPE_MAP.put("rar", new SpecifiedArchiveFileType(ArchiveFileType.INSTANCE, "rar"));
        FILE_TYPE_MAP.put("tar", new SpecifiedArchiveFileType(ArchiveFileType.INSTANCE, "tar"));
        FILE_TYPE_MAP.put("gz", new SpecifiedArchiveFileType(ArchiveFileType.INSTANCE, "gz"));
        FILE_TYPE_MAP.put("bz2", new SpecifiedArchiveFileType(ArchiveFileType.INSTANCE, "bz2"));
        FILE_TYPE_MAP.put("xz", new SpecifiedArchiveFileType(ArchiveFileType.INSTANCE, "xz"));
        FILE_TYPE_MAP.put("z", new SpecifiedArchiveFileType(ArchiveFileType.INSTANCE, "z"));
        // video
        FILE_TYPE_MAP.put("mp4", new VideoFileType("mp4"));
        FILE_TYPE_MAP.put("mkv", new VideoFileType("mkv"));
        FILE_TYPE_MAP.put("avi", new VideoFileType("avi"));
        FILE_TYPE_MAP.put("mov", new VideoFileType("mov"));
        FILE_TYPE_MAP.put("wmv", new VideoFileType("wmv"));
        FILE_TYPE_MAP.put("flv", new VideoFileType("flv"));
        FILE_TYPE_MAP.put("mpeg", new VideoFileType("mpeg"));
        FILE_TYPE_MAP.put("mpg", new VideoFileType("mpg"));
        FILE_TYPE_MAP.put("m4v", new VideoFileType("m4v"));
        FILE_TYPE_MAP.put("webm", new VideoFileType("webm"));
        FILE_TYPE_MAP.put("3gp", new VideoFileType("3gp"));
        // ebook
        FILE_TYPE_MAP.put("epub", new EbookFileType("epub"));
        FILE_TYPE_MAP.put("mobi", new EbookFileType("mobi"));
        FILE_TYPE_MAP.put("azw", new EbookFileType("azw"));
        FILE_TYPE_MAP.put("azw3", new EbookFileType("azw3"));
        // cfg;config;ini;
        FILE_TYPE_MAP.put("cfg", new ConfigFileType("cfg"));
        FILE_TYPE_MAP.put("config", new ConfigFileType("config"));
        FILE_TYPE_MAP.put("ini", new ConfigFileType("ini"));
        // dmg;pkg
        FILE_TYPE_MAP.put("dmg", new DmgFileType());
        FILE_TYPE_MAP.put("pkg", new PkgFileType("pkg"));
        // pdf
        FILE_TYPE_MAP.put("pdf", new PdfFileType());
        // doc;docx
        FILE_TYPE_MAP.put("doc", new WordFileType("doc"));
        FILE_TYPE_MAP.put("docx", new WordFileType("docx"));
        // ppt;pptx
        FILE_TYPE_MAP.put("ppt", new PptFileType("ppt"));
        FILE_TYPE_MAP.put("pptx", new PptFileType("pptx"));
        // xlsx;xls
        FILE_TYPE_MAP.put("xlsx", new ExcelFileType("xlsx"));
        FILE_TYPE_MAP.put("xls", new ExcelFileType("xls"));
        // log
        FILE_TYPE_MAP.put("log", new LogFileType());
        // mp3;wav;flac;m4a
        FILE_TYPE_MAP.put("mp3", new MusicFileType("mp3"));
        FILE_TYPE_MAP.put("wav", new MusicFileType("wav"));
        FILE_TYPE_MAP.put("flac", new MusicFileType("flac"));
        FILE_TYPE_MAP.put("m4a", new MusicFileType("m4a"));

    }

    public static FileType getFileTypeByExtension(String ext) {
        if (StringUtil.isEmpty(ext)) {
            return UnknownFileType.INSTANCE;
        }
        ext = ext.toLowerCase();
        FileType fileType = FILE_TYPE_MAP.get(ext);
        if (fileType == null) {
            return UnknownFileType.INSTANCE;
        }
        return fileType;
    }
}