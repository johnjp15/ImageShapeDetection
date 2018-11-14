import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length <= 1) {
            System.err.println("Missing file argument. Usage: main input_file output_file");
            System.exit(-1);
        } else if (args.length > 3) {
            System.err.println("Too many arguments.");
            System.exit(-1);
        }

        BufferedImage bi;
        try {
//            System.out.println("before read");
            System.out.println(System.getProperty("user.dir") + "/" + args[0]);
            bi = ImageIO.read(new File(System.getProperty("user.dir") + "/" + args[0]));
//            System.out.println("imageio read");
            BufferedImage copy = deepCopy(bi);

            // get gray scale
            makeGray(copy);
            // blur the gray scale
//            blur(copy, 0, 0, copy.getWidth(), copy.getHeight());
            int i = 20;
            while(i-- > 0)  {
                System.out.println("i = " + i);
                blur(copy, copy.getWidth() / 4, copy.getHeight() / 4, (int) (copy.getWidth() * 0.75), (int) (copy.getHeight() * 0.75));
            }


            // identify edges


            // output file
            File outputFile = new File(args[1]);
            ImageIO.write(copy, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public static BufferedImage deepCopy(BufferedImage bi)
    {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static void makeGray(BufferedImage image)
    {
        for(int x = 0; x < image.getWidth(); x++)   {
            for(int y = 0; y < image.getHeight(); y++)  {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8)  & 0xFF;
                int b = (rgb)       & 0xFF;

                int grayLevel = (r + g + b ) / 3;
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
                image.setRGB(x, y, gray);
            }
        }
    }

    public static void blur(BufferedImage image)
    {
        blur(image, 0, 0, image.getWidth(), image.getHeight());
    }

    public static void blur(BufferedImage image, int start_x, int start_y, int end_x, int end_y)
    {
//        System.out.println("IN BLUR: start_x = " + start_x + ", start_y = " + start_y + ", end_x = " + end_x + ", end_y = " + end_y);
        if(start_x < 0 || start_x > image.getWidth() || end_x < 0 || end_x > image.getWidth())    {
//            System.out.println("return 1");
            return;
        }
        if(start_y < 0 || start_y > image.getHeight() || end_y < 0 || end_y > image.getHeight())    {
//            System.out.println("return 2");
            return;
        }

//        BufferedImage blurredImage = new BufferedImage(image);

        int[][] newBlurredColors = new int[end_x - start_x][end_y - start_y];

        int[][] blurWeights1 = new int[][] {
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };

        int[][] blurWeights2 = new int[][]{
                {1, 4, 6, 4, 1},
                {4, 16, 24, 16, 4},
                {6, 24, 36, 24, 6},
                {4, 16, 24, 16, 4},
                {1, 4, 6, 4, 1}
        };

        int[][] blurWeights = blurWeights2;


        for(int i = start_x; i < end_x; i++)   {
            for(int j = start_y; j < end_y; j++)  {
                // get average of the surrounding pixels
                int blurSum = 0;
//                System.out.println(blurSum);
                for(int dx = -1 * (blurWeights.length / 2); dx <= blurWeights.length / 2; dx++) {
                    for(int dy = -1 * (blurWeights.length / 2); dy <= blurWeights[0].length / 2; dy++) {
                        if(i + dx < 0 || j + dy < 0 || i + dx >= image.getWidth() || j + dy >= image.getHeight())   {
                            continue;
                        }
                        int gray = image.getRGB(i + dx, j + dy) & 0x000000FF;
                        int blurred = gray * blurWeights[dx + blurWeights.length / 2][dy + blurWeights.length / 2];
                        blurSum += blurred;
//                        System.out.println(blurSum);
                    }
                }

                blurSum /= Math.pow(blurWeights.length - 1, 4) * 1.0;
//                System.out.println(blurSum + " divided ");
                newBlurredColors[i - start_x][j - start_y] = (blurSum << 16) + (blurSum << 8) + blurSum;
            }
        }

        for(int i = start_x; i < end_x; i++) {
            for (int j = start_y; j < end_y; j++) {
                image.setRGB(i, j, newBlurredColors[i - start_x][j - start_y]);
            }
        }
    }
}
