package com.recognition;

//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.pdf.*;
//import com.itextpdf.text.pdf.fonts.otf.TableHeader;
//import com.openhtmltopdf.css.parser.property.PrimitivePropertyBuilders;
//import nu.pattern.OpenCV;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDTableAttributeObject;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

/**
 * Hello world!
 *
 */
public class App {
    public static final String RESOURCE_DIR_PATH = "./src/main/resources/_BANK_U/";
    public static final String RESULT_DIR_PATH = "./src/main/resources/processed/";
    private static final String IMAGE_FORMAT = ".jpg";
    private static long imageCounter = 0;
    private static File currentFileProcessing;
    private static List<Figure> figures = new ArrayList<>();
    //private static JTable table;
    private static int[][] distanceArray;

    public static void main(String[] args) throws IOException {
        OpenCV.loadShared();
        setUpUi();
        File[] resourceFiles = new File(RESOURCE_DIR_PATH).listFiles();

        for (File resource : resourceFiles) {
            processFigure(resource);
        }
        for (Figure figure : figures) {
            System.out.println(figure.getPixels().get(0));
        }
    }


    private static void recognize() {
        Mat image = imread(currentFileProcessing.getPath());
        LocalTime before = LocalTime.now();
        processFigure(currentFileProcessing);


        for (Figure figure : figures) {
            highlightFigure(image, figure, new Scalar(0, 255, 0));
        }
        LocalTime after = LocalTime.now();

        System.out.println(after.getSecond() - before.getSecond());

        imwrite(String.format("%s%s", RESULT_DIR_PATH, currentFileProcessing.getName()), image);
    }

    private static void highlightFigure(Mat image, Figure figure, Scalar color, String... dopInfo) {
        Window window = figure.getCapturingWindow();
        FigurePoint center = figure.defineCenterPoint();
        FigurePoint windowStartPoint = window.getStartPoint();
        FigurePoint windowEndPoint = window.getEndPoint();

        Imgproc.rectangle(image,
                new Point(windowStartPoint.getX(), windowStartPoint.getY()),
                new Point(windowEndPoint.getX(), windowEndPoint.getY()),
                color, 1);
        Imgproc.circle(
                image, new Point(center.getX(), center.getY()), 1, color, 2);
        Imgproc.putText(
                image, figure.getName() + Arrays.stream(dopInfo).findFirst().orElse(""),
                new Point(windowStartPoint.getX() + 2, windowStartPoint.getY() - 2),
                1, 1, color, 2);
    }

    private static void countDistances() {
        int quantity = figures.size();
        distanceArray = new int[quantity][quantity];

        for (int i = 0; i < quantity; i++) {
            for (int j = 0; j < quantity; j++) {
                distanceArray[i][j] = euclideanDistance(figures.get(i), figures.get(j));
            }
        }
    }

    private static int euclideanDistance(Figure figure1, Figure figure2) {
        FigurePoint center1 = figure1.getCenter();
        FigurePoint center2 = figure2.getCenter();

        int xDiff = (int) Math.pow(center1.getX() - center2.getX(), 2);
        int yDiff = (int) Math.pow(center1.getY() - center2.getY(), 2);

        return (int) Math.sqrt(xDiff + yDiff);
    }

    private static void processFigure(File imageFile) {
        String path = imageFile.getPath();
        Mat image = imread(path);
        Figure figure = new Figure();

        for (int y = 0; y < image.rows(); y++) {
            for (int x = 0; x < image.cols(); x++) {
                if(image.get(y, x)[0] < 128) {
                    figure.addPixel(new FigurePoint(x++, y));
                }
            }
        }

        figure.setFilePath(path);
        FigurePoint startPoint = figure.defineCapturingWindow().getStartPoint();
        figure.setPixels(figure.getPixels().stream()
                .map(point -> new FigurePoint(point.getX() - startPoint.getX(), point.getY() - startPoint.getY()))
                .collect(Collectors.toCollection(ArrayList::new)));
        figures.add(figure);
        highlightFigure(image, figure, new Scalar(255, 0, 200));
        imwrite(String.format("%s%s", RESULT_DIR_PATH, imageFile.getName()), image);
    }

    private static Figure checkFigure(Queue<FigurePoint> points) {
        for (Figure figure : figures) {
            if(figure.checkBelonging(points)) return figure;
        }

        Figure figure = new Figure();
        figures.add(figure);
        return figure;
    }

    private static void printImage(byte[][] array) {
        System.out.println(Arrays.deepToString(array));
    }

    private static void setUpUi() {
        JFrame container = new JFrame();
        container.setLayout(new BorderLayout());
        //JTable infoTable = new JTable();

        JPanel tablePanel = setUpTablePanel();
        container.add(tablePanel, BorderLayout.SOUTH);
        JPanel buttonPanel = new JPanel();

        JPanel imagePanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        JLabel rawImageLabel = new JLabel();
        JLabel processedImageLabel = new JLabel();
        imagePanel.add(rawImageLabel, BorderLayout.WEST);
        imagePanel.add(processedImageLabel, BorderLayout.EAST);
        container.add(scrollPane, BorderLayout.CENTER);

        JButton choiceButton = new JButton("Choose image");
        choiceButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(RESOURCE_DIR_PATH);
            int response = fileChooser.showOpenDialog(null);

            if(response == JFileChooser.APPROVE_OPTION) {
                currentFileProcessing = fileChooser.getSelectedFile();
                drawImage(rawImageLabel, RESOURCE_DIR_PATH);
            }
        });

        JButton processButton = new JButton("Process image");
        processButton.addActionListener(e -> {
            if (currentFileProcessing == null) {
                JOptionPane.showMessageDialog(null, "Please, choose the image!");
                return;
            }

            tablePanel.removeAll();
            JTable infoTable = setUpInfoTable();
            recognize();

            DefaultTableModel infoTableModel = (DefaultTableModel) infoTable.getModel();
            for (int i = 0; i < figures.size(); i++) {
                Figure figure = figures.get(i);
                infoTableModel.addColumn(figure.getName(), new Object[]{
                        figure.getSquare(),
                        figure.getCenter()
                });
            }

            drawImage(processedImageLabel, RESULT_DIR_PATH);

            tablePanel.add(infoTable.getTableHeader(), BorderLayout.NORTH);
            tablePanel.add(infoTable, BorderLayout.CENTER);

            countDistances();
            JTable distanceTable = setUpDistanceTable();

            for (int i = 0; i < distanceArray.length; i++) {
                for (int j = 0; j < distanceArray[i].length; j++) {
                    distanceTable.setValueAt(distanceArray[i][j], i, j + 1);
                }
            }

            tablePanel.add(distanceTable, BorderLayout.SOUTH);
            tablePanel.revalidate();

            figures.clear();
        });


        buttonPanel.add(choiceButton);
        buttonPanel.add(processButton);

        container.add(buttonPanel, BorderLayout.NORTH);
        container.setVisible(true);
    }

    private static JTable setUpDistanceTable() {
        int size = figures.size();
        JTable table = new JTable(size, size + 1);

        for (int i = 0, j = 65; i < size; i++, j++) {
            char name = (char) j;
            table.setValueAt("Distance to " + name, i, 0);
        }
        table.setBackground(new Color(0, 255, 255));

        return table;
    }

    private static JPanel setUpTablePanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setVisible(true);
        return tablePanel;
    }

    private static void drawImage(JLabel processedImageLabel, String dirPath) {
        BufferedImage image = null;

        if(figures.size() > 0) {
            Figure max = figures.get(0);
            Figure min = figures.get(0);
            for (int i = 1; i < figures.size(); i++) {
                Figure curr = figures.get(i);
                if(curr.getSquare() < min.getSquare()) min = curr;
                if(curr.getSquare() > max.getSquare()) max = curr;
            }
            Mat mat = imread(RESULT_DIR_PATH + currentFileProcessing.getName());
            highlightFigure(mat, max, new Scalar(255, 0, 0), " (S(max))");
            highlightFigure(mat, min, new Scalar(0, 0, 255), " (S(min))");

            imwrite(String.format("%s%s", RESULT_DIR_PATH, currentFileProcessing.getName()), mat);
        }

        try {
            String path = dirPath + currentFileProcessing.getName();
            image = ImageIO.read(new File(path));
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, ioe.getMessage());
        }


        processedImageLabel.setIcon(new ImageIcon(image));
        processedImageLabel.repaint();
    }

    private static JTable setUpInfoTable() {
        JTable table = new JTable();

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addColumn("Property", new Object[] { "Square", "Center" });
        table.setVisible(true);
        table.setBackground(new Color(255, 0, 255));

        return table;
    }
}
