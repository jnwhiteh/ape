package fi.iki.jka;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: eleova
 * Date: 6/28/11
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IPhotoCollection {
    boolean load(String albumFileName);

    Color getBackgroundColor();

    Object getElementAt(int i);

    boolean save(String file);

    boolean exportHtmlJari1(String albumFileName);

    boolean exportHtmlJari2(String albumFileName);

    boolean exportTemplateJari1(File target);

    boolean exportTemplateJari2(File target);

    boolean exportTemplateJari3(File target);

    boolean exportSubtitledPhotos(String albumFileName);

    boolean copyOriginals(String albumFileName, String originals, boolean b);

    boolean exportHtmlJari3(String albumFileName);

    boolean exportSlideshow(String targetFile, int i);

    boolean remove(JPhoto photo);

    int size();

    void add(int index, JPhoto item);

    Object getTitle();

    void setTitle(String res);

    Object getDescription();

    void setDescription(String res);

    String getKeywords();

    void setKeywords(String res);

    String getWatermark();

    void setWatermark(String res);

    IJPhoto get(int index);

    JPhotoPageInfo getPageInfo();

    int getSize();

    void setCoverPhoto(IJPhoto jPhoto);

    void setBackgroundColor(Color color);

    void setForegroundColor(Color color);

    boolean isDirty();

    Color getForegroundColor();

    void setDirty(boolean b);

    BufferedImage getCoverIcon();
}
