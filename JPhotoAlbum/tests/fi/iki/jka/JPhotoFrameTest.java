package fi.iki.jka;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: eleova
 * Date: 6/28/11
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class JPhotoFrameTest {

    @Test
    public void OnStartSlideShowClickShouldStartSlideShow() throws Exception {
        JPhotoFrame photoFrame = new JPhotoFrame();
        PhotoCollectionFake photoCollectionFake = new PhotoCollectionFake();
        photoCollectionFake.add(0, new JPhoto());
        photoFrame.showSlideshow(photoCollectionFake);

//
//
//        JOptionPane.showMessageDialog.(
//                "No photos to show!",

    }

    private class PhotoCollectionFake extends JPhotoCollection implements IPhotoCollection {
    }
}
