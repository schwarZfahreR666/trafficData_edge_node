package com.example.edge_node.utils;

import org.apache.commons.io.FileUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Create by zhangran
 */
@SuppressWarnings("restriction")
public class Base64ImageUtils {
    /**
     * 将网络图片进行Base64位编码
     *
     * @param imageUrl
     *            图片的url路径，如http://.....xx.jpg
     * @return
     */
    public static String encodeImgageToBase64(URL imageUrl) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        ByteArrayOutputStream outputStream = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(imageUrl);
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(outputStream.toByteArray());// 返回Base64编码过的字节数组字符串
    }

    /**
     * 将本地图片进行Base64位编码
     *
     * @param path
     *            图片的url路径，如F:/.....xx.jpg
     * @return
     */
    public static String encodeImgageToBase64(String path) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream inputStream = Base64ImageUtils.class.getResourceAsStream(path);
        File file = new File("ImageCache");
        try {
            assert inputStream != null;
            FileUtils.copyInputStreamToFile(inputStream,file);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream outputStream = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        assert outputStream != null;
        return encoder.encode(outputStream.toByteArray());// 返回Base64编码过的字节数组字符串
    }

    /**
     * 将Base64位编码的图片进行解码，并保存到指定文件夹
     *
     * @param base64
     *            base64编码的图片信息
     * @return
     */
    public static void decodeBase64ToImage(String base64, String path,
                                           String imgName) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            FileOutputStream write = new FileOutputStream(new File(path
                    + imgName));
            byte[] decoderBytes = decoder.decodeBuffer(base64);
            write.write(decoderBytes);
            write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * base64字符串转化成图片 ,图片进行噪点和像素处理
     * @param imageBase64 图片编码
     * @param color  需要处理的颜色
     * @return
     * @throws IOException
     */
    public static boolean GenerateImage(String imageBase64, String color, String path) throws IOException { // 对字节数组字符串进行Base64解码并生成图片
        if (imageBase64 == null) // 图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            byte[] b = decoder.decodeBuffer(imageBase64);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {// 调整异常数据
                    b[i] += 256;
                }
            }
            InputStream imageInput = new ByteArrayInputStream(b);
            pixelProcess(color,imageInput,path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }
    private static void pixelProcess(String color,InputStream imageInput,String path) throws IOException {

        //定义一个RGB的数组，因为图片的RGB模式是由三个 0-255来表示的 比如白色就是(255,255,255)
        int[] rgb = new int[3];
        //用来处理图片的缓冲流

        BufferedImage bi = null;
        try {
            //用ImageIO将图片读入到缓冲中
            bi = ImageIO.read(imageInput);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //得到图片的长宽
        int width = bi.getWidth();
        int height = bi.getHeight();
        int minx = bi.getMinX();
        int miny = bi.getMinY();
        /**
         * 这里是遍历图片的像素，因为要处理图片的背色，所以要把指定像素上的颜色换成目标颜色
         * 这里 是一个二层循环，遍历长和宽上的每个像素
         */
        for (int i = minx; i < width; i++) {
            for (int j = miny; j < height; j++) {
                /**
                 * 得到指定像素（i,j)上的RGB值，
                 */
                int pixel = bi.getRGB(i, j);
                /**
                 * 分别进行位操作得到 r g b上的值
                 */
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                /**
                 * 进行换色操作，我这里是要把蓝底换成白底，那么就判断图片中rgb值是否在蓝色范围的像素
                 */
                //begin
                if (color == "red") {
                    if (rgb[0] >= 128 && rgb[1] < 128 && rgb[2] < 128) {
                        bi.setRGB(i, j, 0x000000);
                    } else {
                        bi.setRGB(i, j, 0xffffff);
                    }
                } else if (color == "yellow") {
                    if (rgb[0] > 128 && rgb[1] > 128 && rgb[2] < 128) {
                        bi.setRGB(i, j, 0x000000);
                    } else {
                        bi.setRGB(i, j, 0xffffff);
                    }
                } else if (color == "blue") {
                    if (rgb[0] < 128 && rgb[1] < 128 && rgb[2] > 128) {
                        bi.setRGB(i, j, 0x000000);
                    } else {
                        bi.setRGB(i, j, 0xffffff);
                    }
                } else {
                    if (rgb[0] < 128 && rgb[1] < 128 && rgb[2] < 128) {
                        bi.setRGB(i, j, 0x000000);
                    } else {
                        bi.setRGB(i, j, 0xffffff);
                    }
                }
                //end

            }
        }
        //将缓冲对象保存到新文件中
        FileOutputStream ops = new FileOutputStream(new File(path));
        ImageIO.write(bi, "png", ops);
        ops.flush();
        ops.close();
    }

}
