package fi.iki.jka;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by IntelliJ IDEA.
 * User: eleova
 * Date: 6/28/11
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JMock.class)
public class JPhotoFrameTest {

    Mockery context = new Mockery();
    @Test
    public void OnStartSlideShowClickShouldStartSlideShow() throws Exception {
        JPhotoFrame photoFrame = new JPhotoFrame();

        // Setup the mocks
        final IPhotoCollection mockPhotoCollection = context.mock(IPhotoCollection.class);
        final IJPhoto photoMock = context.mock(IJPhoto.class);

        context.checking(new Expectations() {{
            allowing(mockPhotoCollection).getSize();will(returnValue(1));
            allowing(mockPhotoCollection).get(0);will(returnValue(photoMock));
            will(returnValue(photoMock));
        }});

        photoFrame.showSlideshow(mockPhotoCollection);



//        JOptionPane.showMessageDialog.(
//                "No photos to show!",

    }
}
