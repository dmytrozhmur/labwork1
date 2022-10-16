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
import java.util.*;
import java.util.List;

        import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * Hello world!
 *
 */
public class App {
    public static final String RESOURCE_DIR_PATH = "./src/main/resources/raw/";
    public static final String RESULT_DIR_PATH = "./src/main/resources/processed/";
    private static final String IMAGE_FORMAT = ".jpg";
    private static long imageCounter = 0;
    private static File currentResourceFile;
    private static List<Figure> figures = new ArrayList<>();
    //private static JTable table;
    private static int[][] distanceArray;

    public static void main(String[] args) throws IOException {
        OpenCV.loadShared();
        setUpUi();
    }

    private static void setUpUi() {
        JFrame container = new JFrame();
        container.setLayout(new BorderLayout());
        //JTable infoTable = new JTable();

        JPanel tablePanel = setUpTablePanel();
        container.add(tablePanel, BorderLayout.SOUTH);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setVisible(true);

        JLabel rawImageLabel = new JLabel();
        JLabel processedImageLabel = new JLabel();
        rawImageLabel.setVisible(true);
        processedImageLabel.setVisible(true);
        container.add(rawImageLabel, BorderLayout.WEST);
        container.add(processedImageLabel, BorderLayout.EAST);

        JButton choiceButton = new JButton("Choose image");
        choiceButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(RESOURCE_DIR_PATH);
            int response = fileChooser.showOpenDialog(null);

            if(response == JFileChooser.APPROVE_OPTION) {
                currentResourceFile = fileChooser.getSelectedFile();
                drawImage(rawImageLabel, RESOURCE_DIR_PATH);
            }
        });

        JButton processButton = new JButton("Process image");
        processButton.addActionListener(e -> {
            if (currentResourceFile == null) {
                JOptionPane.showMessageDialog(null, "Please, choose the image!");
                return;
            }

            tablePanel.removeAll();
            JTable infoTable = setUpInfoTable();
            figures.clear();
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
                    distanceTable.setValueAt(distanceArray[i][j], i + 1, j + 1);
                }
            }

            tablePanel.add(distanceTable, BorderLayout.SOUTH);
            tablePanel.revalidate();
        });


        buttonPanel.add(choiceButton);
        buttonPanel.add(processButton);

        container.add(buttonPanel, BorderLayout.NORTH);
        container.setVisible(true);
    }

    private static JTable setUpDistanceTable() {
        int size = figures.size();
        JTable table = new JTable(size + 1, size + 1);

        for (int i = 1, j = 65; i <= size; i++, j++) {
            char name = (char) j;
            table.setValueAt(name, 0, i);
            table.setValueAt(name, i, 0);
        }

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
            Mat mat = imread(RESULT_DIR_PATH + currentResourceFile.getName());
//            Window maxCapturingWindow = max.getCapturingWindow();
//            Imgproc.rectangle(mat, new Point(maxCapturingWindow.getStartPoint().getX(), maxCapturingWindow.getStartPoint().getY()),
//                    new Point(maxCapturingWindow.getEndPoint().getX(), maxCapturingWindow.getEndPoint().getY()), new Scalar(255, 0, 0),
//                    1);
//            Imgproc.putText(mat, max.getName(), new Point(maxCapturingWindow.getStartPoint().getX() + 2, maxCapturingWindow.getStartPoint().getY() - 2),
//                    1, 1, new Scalar(255, 0, 0), 2);
            highlightFigure(mat, max, new Scalar(255, 0, 0), " (S(max))");
            highlightFigure(mat, min, new Scalar(0, 0, 255), " (S(min))");

            Imgcodecs.imwrite(String.format("%s%s", RESULT_DIR_PATH, currentResourceFile.getName()), mat);
        }

        try {
            String path = dirPath + currentResourceFile.getName();
            image = ImageIO.read(new File(path));
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, ioe.getMessage());
        }

//        int minSquare = Integer.MAX_VALUE;
//        int maxSquare = Integer.MIN_VALUE;


        processedImageLabel.setIcon(new ImageIcon(image));
        processedImageLabel.repaint();
    }

    private static JTable setUpInfoTable() {
        JTable table = new JTable();

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addColumn("Property names", new Object[] { "Square", "Center" });
        table.setVisible(true);

        return table;
    }

    private static void recognize() {
        Mat image = imread(currentResourceFile.getPath());
        processFigures(image);

        for (Figure figure : figures) {
            highlightFigure(image, figure, new Scalar(0, 255, 0));
        }

        Imgcodecs.imwrite(String.format("%s%s", RESULT_DIR_PATH, currentResourceFile.getName()), image);
    }

    private static void highlightFigure(Mat image, Figure figure, Scalar color, String... dopInfo) {
        Window window = figure.defineCapturingWindow();
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

    private static void processFigures(Mat image) {
        Figure.nameCounter = 65;
        Queue<FigurePoint> pointsRow = new LinkedList<>();

        for (int y = 0; y < image.rows(); y++) {
            for (int x = 0; x < image.cols(); x++) {
                while (image.get(y, x)[0] < 128) {
                    //for (int i = x; image.get(y, x)[0] )
                    pointsRow.add(new FigurePoint(x++, y));
                }
                if (!pointsRow.isEmpty()) {
                    Figure figure = checkFigure(pointsRow);
                    figure.addPixels(pointsRow);
                    pointsRow.clear();
                }
            }
            pointsRow.clear();
        }

//        System.out.println(figures.size());
//        System.out.println();
//        for (Figure figure : figures) {
//            System.out.println(figure.defineCapturingWindow());
//            System.out.println(figure.defineCenterPoint());
//            System.out.println(figure.defineSquare());
//            System.out.println();
//        }
//
//        HighGui.namedWindow("image", HighGui.WINDOW_AUTOSIZE);
//        HighGui.imshow("image", image);
//        HighGui.waitKey();
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
}
