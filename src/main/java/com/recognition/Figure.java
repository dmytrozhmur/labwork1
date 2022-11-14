package com.recognition;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Data
public class Figure {
    public static char nameCounter;
    private final String name;
    private List<FigurePoint> pixels;
    private Window capturingWindow;
    private FigurePoint center;
    private Queue<FigurePoint> lastAddedRow = new LinkedList<>();

    public Figure() {
        name = String.valueOf(nameCounter++);
        pixels = new ArrayList<>();
        capturingWindow = new Window();
        center = new FigurePoint();
    }

    public void addPixels(Queue<FigurePoint> points) {
        //lastAddedRow.clear();
        lastAddedRow.addAll(points);
        pixels.addAll(points);
    }

    public boolean checkBelonging(Queue<FigurePoint> points) {
        for (FigurePoint point : points) {
            if (lastAddedRow.contains(new FigurePoint(point.getX(), point.getY() - 1)))
                return true;
        }
        return false;
    }

    public Window defineCapturingWindow() {
        if(capturingWindow.getStartPoint() != null) return capturingWindow;

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
        int leftBorder = Integer.MAX_VALUE, topBorder = Integer.MAX_VALUE,
                rightBorder = Integer.MIN_VALUE, bottomBorder = Integer.MIN_VALUE;

        for (FigurePoint point : pixels) {
            if(point.getX() < leftBorder) leftBorder = point.getX();
            if(point.getX() > rightBorder) rightBorder = point.getX();
            if(point.getY() < topBorder) topBorder = point.getY();
            if(point.getY() > bottomBorder) bottomBorder = point.getY();
        }

        FigurePoint startPoint = new FigurePoint(leftBorder, topBorder);
        FigurePoint endPoint = new FigurePoint(rightBorder, bottomBorder);
        return center = new FigurePoint(
                startPoint.getX() + ((endPoint.getX() - startPoint.getX()) / 2),
                startPoint.getY() + ((endPoint.getY() - startPoint.getY()) / 2)
        );
    }

    public void correctPixels() {
        FigurePoint currStartPoint = defineCapturingWindow().getStartPoint();

        pixels = pixels.stream().map(point -> new FigurePoint(
                point.getX() - currStartPoint.getX(),
                point.getY() - currStartPoint.getY()))
                .collect(Collectors.toList());
        return;
    }

    public int getSquare() {
        return pixels.size();
    }
}
