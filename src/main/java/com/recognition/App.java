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
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * Hello world!
 *
 */
public class App {
    public static final String RESOURCE_DIR_PATH = "./src/main/resources/_BANK_E/";
    public static final String TARGET_DIR_PATH = "./src/main/resources/_BANK_U/";
    public static final String RESULT_DIR_PATH = "./src/main/resources/scaled/";
    private static final String IMAGE_FORMAT = ".jpg";
    private static Map<Picture, Picture> targetResourceMatcher = new HashMap<>();
    private static List<Picture> resources = new ArrayList<>();
    private static long imageCounter = 0;
    private static File currentResourceFile;
    private static int[][] distanceArray;

    public static void main(String[] args) throws IOException {
        OpenCV.loadShared();
        setUpUi();

        File[] resourceFiles = new File(RESOURCE_DIR_PATH).listFiles();
        for (File resource : resourceFiles) {
            processFigures(resource, true);
        }
    }

    private static void setUpUi() {
        JFrame container = new JFrame();
        container.setLayout(new BorderLayout());

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
            JFileChooser fileChooser = new JFileChooser(TARGET_DIR_PATH);
            int response = fileChooser.showOpenDialog(null);

            if(response == JFileChooser.APPROVE_OPTION) {
                currentResourceFile = fileChooser.getSelectedFile();
                drawImage(rawImageLabel, currentResourceFile.getPath());
            }
        });

        JButton processButton = new JButton("Process image");
        processButton.addActionListener(e -> {
            if (currentResourceFile == null) {
                JOptionPane.showMessageDialog(null, "Please, choose the image!");
                return;
            }

//            tablePanel.removeAll();
//            JTable infoTable = setUpInfoTable();
            recognize();

            for (Map.Entry<Picture, Picture> matching : targetResourceMatcher.entrySet()) {
                if(matching.getKey().getFilePath().equals(currentResourceFile.getPath())) {
                    drawImage(processedImageLabel, matching.getValue().getFilePath());
                }
            }

//            DefaultTableModel infoTableModel = (DefaultTableModel) infoTable.getModel();
//            for (int i = 0; i < figures.size(); i++) {
//                Figure figure = figures.get(i);
//                infoTableModel.addColumn(figure.getName(), new Object[]{
//                        figure.getSquare(),
//                        figure.getCenter()
//                });
//            }
//
//            drawImage(processedImageLabel, RESULT_DIR_PATH);
//
//            tablePanel.add(infoTable.getTableHeader(), BorderLayout.NORTH);
//            tablePanel.add(infoTable, BorderLayout.CENTER);
//
//            countDistances();
//            JTable distanceTable = setUpDistanceTable();
//
//            for (int i = 0; i < distanceArray.length; i++) {
//                for (int j = 0; j < distanceArray[i].length; j++) {
//                    distanceTable.setValueAt(distanceArray[i][j], i, j + 1);
//                }
//            }
//
//            tablePanel.add(distanceTable, BorderLayout.SOUTH);
//            tablePanel.revalidate();
//
//            figures.clear();
        });


        buttonPanel.add(choiceButton);
        buttonPanel.add(processButton);

        container.add(buttonPanel, BorderLayout.NORTH);
        container.setVisible(true);
    }

    private static JPanel setUpTablePanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setVisible(true);
        return tablePanel;
    }

    private static void drawImage(JLabel imageLabel, String filePath) {
        BufferedImage image = null;

        try {
            String path = filePath;
            image = ImageIO.read(new File(path));
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, ioe.getMessage());
        }

        imageLabel.setIcon(new ImageIcon(image));
        imageLabel.repaint();
    }

    private static JTable setUpInfoTable() {
        JTable table = new JTable();

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addColumn("Property", new Object[] { "Square", "Center" });
        table.setVisible(true);
        table.setBackground(new Color(255, 0, 255));

        return table;
    }

    private static void recognize() {
        Mat image = imread(currentResourceFile.getPath());
        LocalTime before = LocalTime.now();
        processFigures(currentResourceFile, true);


//        for (Figure figure : figures) {
//            highlightFigure(image, figure, new Scalar(0, 255, 0));
//        }
        LocalTime after = LocalTime.now();

        System.out.println(after.getSecond() - before.getSecond());

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

    private static List<Figure> processFigures(File imageFile, boolean toMatcher) {
        Picture picture = new Picture();
        List<Figure> figures = new ArrayList<>();

        String path = imageFile.getPath();
        picture.setFilePath(path);
        Mat image = imread(path);

        Figure.nameCounter = 65;
        Queue<FigurePoint> pointsRow = new LinkedList<>();

        for (int y = 0; y < image.rows(); y++) {
            for (int x = 0; x < image.cols(); x++) {
                while (image.get(y, x)[0] < 128) {
                    pointsRow.add(new FigurePoint(x++, y));
                }
                if (!pointsRow.isEmpty()) {
                    Figure figure = checkFigure(pointsRow, figures);
                    figure.addPixels(pointsRow);
                    pointsRow.clear();
                }
            }
            pointsRow.clear();
        }

        picture.setFigures(figures);
        if(toMatcher) addToMatcher(picture);

        return figures;
    }

    private static void addToMatcher(Picture picture) {
        if(resources.size() >= 19) {
            List<Picture> priorityResources = resources.stream().filter(res -> {
                FigurePoint currStartPoint = picture.getFigures().get(0).defineCapturingWindow().getStartPoint();
                FigurePoint currEndPoint = picture.getFigures().get(0).defineCapturingWindow().getEndPoint();
                FigurePoint resStartPoint = picture.getFigures().get(0).defineCapturingWindow().getStartPoint();
                FigurePoint resEndPoint = picture.getFigures().get(0).defineCapturingWindow().getEndPoint();

                int currWidth = currStartPoint.getX() - currEndPoint.getX();
                int currHeight = currStartPoint.getY() - currEndPoint.getY();
                int resWidth = resStartPoint.getX() - resEndPoint.getX();
                int resHeight = resStartPoint.getY() - resEndPoint.getY();

                return currWidth == resWidth && currHeight == resHeight;
            }).filter(picture::isSimilarTo).collect(Collectors.toList());
            if(priorityResources.size() == 1) {
                targetResourceMatcher.put(picture, priorityResources.get(0));
                return;
            }
//                    res -> {
//                if(picture.isSimilarTo(res)) {
//
//                }
//            }).;
            for (Picture resource : priorityResources) {
                changeScale(picture, resource);
                if(picture.isSimilarTo(resource)) {
                    targetResourceMatcher.put(picture, resource);
                    return;
                }
            }
        } else {
            resources.add(picture);
        }
    }

    private static Figure checkFigure(Queue<FigurePoint> points, List<Figure> figures) {
        for (Figure figure : figures) {
            if(figure.checkBelonging(points)) return figure;
        }

        Figure figure = new Figure();
        figures.add(figure);
        return figure;
    }

    private static void changeScale(Picture target, Picture resource) {
        FigurePoint currStartPoint = target.getFigures().get(0).defineCapturingWindow().getStartPoint();
        FigurePoint currEndPoint = target.getFigures().get(0).defineCapturingWindow().getEndPoint();
        FigurePoint resStartPoint = resource.getFigures().get(0).defineCapturingWindow().getStartPoint();
        FigurePoint resEndPoint = resource.getFigures().get(0).defineCapturingWindow().getEndPoint();

        int currWidth = currStartPoint.getX() - currEndPoint.getX();
        int currHeight = currStartPoint.getY() - currEndPoint.getY();
        float resWidth = resStartPoint.getX() - resEndPoint.getX();
        float resHeight = resStartPoint.getY() - resEndPoint.getY();
        double widthDiff = currWidth / resWidth;
        double heightDiff = currHeight / resHeight;

        resizeFigures(widthDiff, heightDiff, target);
    }

    private static void resizeFigures(double widthDiff, double heightDiff, Picture picture) {
        BufferedImage bufferedImage;
        File newImageFile = new File(RESULT_DIR_PATH + currentResourceFile.getName());

        try {
            bufferedImage = ImageIO.read(new File(picture.getFilePath()));
            Image result = bufferedImage.getScaledInstance(
                    (int) (bufferedImage.getWidth() * widthDiff),
                    (int) (bufferedImage.getHeight() * heightDiff),
                    Image.SCALE_DEFAULT);

            BufferedImage bimage = new BufferedImage(result.getWidth(null), result.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            Graphics2D bGr = bimage.createGraphics();
            bGr.drawImage(result, 0, 0, null);
            bGr.dispose();

            ImageIO.write(bimage, "png", newImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        picture.setFigures(processFigures(newImageFile, false));
    }

    private static void printImage(byte[][] array) {
        System.out.println(Arrays.deepToString(array));
    }
}
