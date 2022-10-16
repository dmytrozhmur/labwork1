package com.recognition;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

public class ArrayConverter {
    public static byte[][] imageTo2DArray(Mat image) {
//        byte[] image1DArray = imageToArray(image);
//        int sqrt = (int) Math.sqrt(image1DArray.length);

//        byte[][] image2DArray = new byte[sqrt][sqrt];
//        int counter = 0;
//        for (int i = 0; i < sqrt; i++) {
//            for (int j = 0; j < sqrt; j++) {
//                image2DArray[i][j] = image1DArray[counter++];
//            }
//        }
        int width = image.width();
        int height = image.height();
        byte[] data = new byte[height * width];
        image.get(0, 0, data);

        byte[][] image2DArray = new byte[height][width];
        int counter = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                image2DArray[i][j] = data[counter++];
            }
        }

        return image2DArray;
    }

    public static byte[] image2DArrayTo1DArray(byte[][] image2DArray) {
        int size = image2DArray.length * image2DArray[0].length;
        byte[] image1DArray = new byte[size];

        int counter = 0;
        for (int i = 0; i < image2DArray.length; i++) {
            for (int j = 0; j < image2DArray[i].length; j++) {
                image1DArray[counter++] = image2DArray[i][j];
            }
        }

        return image1DArray;
    }

    private static byte[] imageToArray(Mat img) {
        byte[] imgData = new byte[(int) (img.total())];
        img.get(0, 0, imgData);
        return imgData;
    }
}
