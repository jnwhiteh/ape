
/*
UnsharpMaskFilter for JAlbum
Copyright (C) 2003  Janne Kipinä

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Free Software Foundation: http://www.fsf.org/
GNU General Public License: http://www.fsf.org/licenses/gpl.html
*/

// import se.datadosen.jalbum.JAFilter;
package fi.iki.jka;
import java.util.Map;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
* <p>UnsharpMaskFilter for JAlbum.</p>
*
* <p>This is a free filter for the <a href="http://www.datadosen.se/jalbum/">JAlbum</a> web photo
* album generator. The filter does the unsharp mask type sharpening operation to the image.
* There are two user definable parameters, radius and amount. Radius controls how many
* surrounding pixels are taken into account when calculating a new value for the given pixel.
* Amount is used to control effectiveness of the operation.</p>
*
* <p>Homepage for the filter is at <a href="http://www.ratol.fi/~jakipina/java/">
* http://www.ratol.fi/~jakipina/java/</a>.</p>
*
* @author Janne Kipin&auml; (<a href="mailto:jedah@surfeu.fi">jedah@surfeu.fi</a>)
* @copyright Copyright &copy; 2003
* @version 1.1 (13.09.2003)
*
* @see <a href="http://www.ratol.fi/~jakipina/java/">Filter homepage</a>
* @see <a href="http://www.datadosen.se/jalbum/">JAlbum homepage</a>
*/
public class UnsharpMaskFilter {

public static final int DEFAULT_RADIUS = 2;
public static final int MINIMUM_RADIUS = 1;
public static final int MAXIMUM_RADIUS = 10;

public static final int DEFAULT_AMOUNT = 15;
public static final int MINIMUM_AMOUNT = 1;
public static final int MAXIMUM_AMOUNT = 100;

private int radius = DEFAULT_RADIUS;
private int amount = DEFAULT_AMOUNT;

public void setRadius(int newRadius) {
    if (newRadius < MINIMUM_RADIUS) {
        radius = MINIMUM_RADIUS;
    } else if (newRadius > MAXIMUM_RADIUS) {
        radius = MAXIMUM_RADIUS;
    } else {
        radius = newRadius;
    }
}

public int getRadius() {
    return radius;
}

public void setAmount(int newAmount) {
    if (newAmount < MINIMUM_AMOUNT) {
        amount = MINIMUM_AMOUNT;
    } else if (newAmount > MAXIMUM_AMOUNT) {
        amount = MAXIMUM_AMOUNT;
    } else {
        amount = newAmount;
    }
}

public int getAmount() {
    return amount;
}

public String getName() {
    return "UnsharpMask";
}

public String getDescription() {
    return "Performs the unsharp mask type sharpening operation";
}

public BufferedImage filter(BufferedImage bi, Map vars) {
    if ((bi.getWidth() < bi.getHeight()) && (radius > bi.getWidth())) {
        radius = bi.getWidth();
    } else if ((bi.getHeight() < bi.getWidth()) && (radius > bi.getHeight())) {
        radius = bi.getHeight();
    }

    int size = (radius * 2) + 1;
    float standard_deviation = amount / 20f;
    float[] elements = getGaussianBlurKernel(size, standard_deviation);
    int center = ((size * size) - 1) / 2;
    elements[center] = 0f;

    float sum = 0f;
    for (int i = 0; i < elements.length; i++) {
        sum += elements[i];
        elements[i] = -elements[i];
    }
    elements[center] = sum + 1;

    BufferedImage large = getEnlargedImageWithMirroring(bi, radius);
    BufferedImage sharpened = new BufferedImage(large.getWidth(), large.getHeight(), large.getType());

    Kernel kernel = new Kernel(size, size, elements);
    ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    cop.filter(large, sharpened);

    BufferedImage out = sharpened.getSubimage(radius, radius, bi.getWidth(), bi.getHeight());

    return out;
}

private BufferedImage getEnlargedImageWithMirroring(BufferedImage bi, int size) {
    int s = size;

    int width = bi.getWidth() + 2 * s;
    int height = bi.getHeight() + 2 * s;
    BufferedImage out = new BufferedImage(width, height, bi.getType());
    Graphics2D g = out.createGraphics();
    g.drawImage(bi, s, s, null);

    BufferedImage part;

    //top-left corner
    part = bi.getSubimage(0, 0, s, s);
    part = doHorizontalFlip(part);
    part = doVerticalFlip(part);
    g.drawImage(part, 0, 0, null);

    //top-right corner
    part = bi.getSubimage(bi.getWidth()-s, 0, s, s);
    part = doHorizontalFlip(part);
    part = doVerticalFlip(part);
    g.drawImage(part, width-s, 0, null);

    //bottom-left corner
    part = bi.getSubimage(0, bi.getHeight()-s, s, s);
    part = doHorizontalFlip(part);
    part = doVerticalFlip(part);
    g.drawImage(part, 0, height-s, null);

    //bottom-right corner
    part = bi.getSubimage(bi.getWidth()-s, bi.getHeight()-s, s, s);
    part = doHorizontalFlip(part);
    part = doVerticalFlip(part);
    g.drawImage(part, width-s, height-s, null);

    //left border
    part = bi.getSubimage(0, 0, s, bi.getHeight());
    part = doHorizontalFlip(part);
    g.drawImage(part, 0, s, null);

    //right border
    part = bi.getSubimage(bi.getWidth()-s, 0, s, bi.getHeight());
    part = doHorizontalFlip(part);
    g.drawImage(part, width-s, s, null);

    //top border
    part = bi.getSubimage(0, 0, bi.getWidth(), s);
    part = doVerticalFlip(part);
    g.drawImage(part, s, 0, null);

    //bottom border
    part = bi.getSubimage(0, bi.getHeight()-s, bi.getWidth(), s);
    part = doVerticalFlip(part);
    g.drawImage(part, s, height-s, null);

    return out;
}

private BufferedImage doHorizontalFlip(BufferedImage bi) {
    BufferedImage out = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());

    AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
    transform.translate(-bi.getWidth(), 0);
    AffineTransformOp atop = new AffineTransformOp(transform,
            AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    out = atop.filter(bi, null);
    return out;
}

private BufferedImage doVerticalFlip(BufferedImage bi) {
    BufferedImage out = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());

    AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
    transform.translate(0, -bi.getHeight());
    AffineTransformOp atop = new AffineTransformOp(transform,
            AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    out = atop.filter(bi, null);
    return out;
}

private float[] getGaussianBlurKernel(int size, float standard_deviation) {
    /*
    v = e ^ ( -(x*x + y*y) / (2 * sd * sd) )
    Where sd is the standard deviation and x and y are the relative position to the center.
    */
    double nominator = 2 * standard_deviation * standard_deviation;
    float[] values = new float[size*size];

    int center = (size - 1) / 2;
    int limit = size - 1;
    int xx;
    int yy;
    float sum = 0f;
    float value = 0f;

    for (int y = 0; y < size; y++) {
        for (int x = 0; x < size; x++) {
            if ((y <= center) && (x <= center)) {
                if (x >= y) {
                    //calculate new value
                    xx = center - x;
                    yy = center - y;
                    value = (float) Math.exp(-(xx*xx + yy*yy) / nominator);
                    values[(y*size)+x] = value;
                    sum += value;
                } else {
                    //copy existing value
                    value = values[(x*size)+y];
                    values[(y*size)+x] = value;
                    sum += value;
                }
            } else {
                xx = x;
                yy = y;
                if (yy > center) yy = limit - yy;
                if (xx > center) xx = limit - xx;
                value = values[(yy*size)+xx];
                values[(y*size)+x] = value;
                sum += value;
            }
        }
    }

    for (int i = 0; i < values.length; i++) {
        values[i] = values[i] / sum;
    }

    return values;
}

}