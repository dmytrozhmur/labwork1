package com.recognition;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

@Data
public class Figure {
    public static char nameCounter;
    private final String name;
    private ArrayList<FigurePoint> pixels;
    private Window capturingWindow;
    private FigurePoint center;
    private Queue<FigurePoint> lastAddedRow = new LinkedList<>();
    private String filePath;

    public Figure() {
        name = String.valueOf(nameCounter++);
        pixels = new ArrayList<>();
        capturingWindow = new Window();
        center = new FigurePoint();
    }

    public void addPixel(FigurePoint point) {
        pixels.add(point);
    }

    public boolean checkBelonging(Queue<FigurePoint> points) {
        for (FigurePoint point : points) {
            if (lastAddedRow.contains(new FigurePoint(point.getX(), point.getY() - 1)))
                return true;
        }
        return false;
    }

    public Window defineCapturingWindow() {
        int leftBorder = Integer.MAX_VALUE, topBorder = Integer.MAX_VALUE,
                rightBorder = Integer.MIN_VALUE, bottomBorder = Integer.MIN_VALUE;

        for (FigurePoint point : pixels) {
            if(point.getX() < leftBorder) leftBorder = point.getX();
            if(point.getX() > rightBorder) rightBorder = point.getX();
            if(point.getY() < topBorder) topBorder = point.getY();
            if(point.getY() > bottomBorder) bottomBorder = point.getY();
        }

        capturingWindow.setStartPoint(new FigurePoint(leftBorder - 2, topBorder - 2));
        capturingWindow.setEndPoint(new FigurePoint(rightBorder + 2, bottomBorder + 2));

        return capturingWindow;
    }

    public FigurePoint defineCenterPoint() {
        if(capturingWindow.getStartPoint() == null)
            defineCapturingWindow();

        FigurePoint startPoint = capturingWindow.getStartPoint();
        FigurePoint endPoint = capturingWindow.getEndPoint();
        return center = new FigurePoint(
                startPoint.getX() + ((endPoint.getX() - startPoint.getX()) / 2),
                startPoint.getY() + ((endPoint.getY() - startPoint.getY()) / 2)
        );
    }

    public int getSquare() {
        return pixels.size();
    }
}
