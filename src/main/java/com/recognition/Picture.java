package com.recognition;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Picture {
    private List<Figure> figures = new ArrayList<>();
    private String filePath;

    public boolean isSimilarTo(Picture resource) {
        if(resource == null) return false;
        List<Figure> resourceFigures = resource.getFigures();

        int size;
        if((size = figures.size()) != resourceFigures.size()) return false;
        if(size == 1) {
            return checkCoincidences(resource);
        } else if(size == 2) {
            return defineCentersPositioning(
                    figures.get(0).defineCenterPoint(),
                    figures.get(1).defineCenterPoint()
            ) == defineCentersPositioning(
                    resourceFigures.get(0).defineCenterPoint(),
                    resourceFigures.get(1).defineCenterPoint()
            ) && checkCoincidences(resource);
        } else if(size == 3) {
            return defineCentersPositioning(
                    figures.get(0).defineCenterPoint(),
                    figures.get(1).defineCenterPoint()
            ) == defineCentersPositioning(
                    resourceFigures.get(0).defineCenterPoint(),
                    resourceFigures.get(1).defineCenterPoint()
            ) && defineCentersPositioning(
                    figures.get(0).getCenter(),
                    figures.get(2).getCenter()
            ) == defineCentersPositioning(
                    resourceFigures.get(0).getCenter(),
                    resourceFigures.get(2).getCenter()
            );
        } else return false;
    }

    private Positioning defineCentersPositioning(FigurePoint mainCenterPoint,
                                                 FigurePoint secondaryCenterPoint) {
        if(mainCenterPoint.getX() < secondaryCenterPoint.getX()
                && mainCenterPoint.getY() < secondaryCenterPoint.getY()) return Positioning.TOP_RIGHT;
        if(mainCenterPoint.getX() < secondaryCenterPoint.getX()
                && mainCenterPoint.getY() >= secondaryCenterPoint.getY()) return Positioning.BOTTOM_RIGHT;
        if(mainCenterPoint.getX() >= secondaryCenterPoint.getX()
                && mainCenterPoint.getY() < secondaryCenterPoint.getY()) return Positioning.TOP_LEFT;
        if(mainCenterPoint.getX() >= secondaryCenterPoint.getX()
                && mainCenterPoint.getY() >= secondaryCenterPoint.getY()) return Positioning.BOTTOM_LEFT;
        return null;
    }

    private boolean checkCoincidences(Picture resource) {
        Figure currWrapper = figures.get(0);
        Figure resourceWrapper = resource.getFigures().get(0);
        FigurePoint currStartPoint = currWrapper.defineCapturingWindow().getStartPoint();
        FigurePoint resourceStartPoint = resourceWrapper.defineCapturingWindow().getStartPoint();

        List<FigurePoint> currFigure = currWrapper.getPixels().stream()
                .map(point -> new FigurePoint(point.getX() - currStartPoint.getX(),
                        point.getY() - currStartPoint.getY()))
                .collect(Collectors.toCollection(ArrayList::new));
        List<FigurePoint> resourceFigure = resourceWrapper.getPixels().stream()
                .map(point -> new FigurePoint(point.getX() - resourceStartPoint.getX(),
                        point.getY() - resourceStartPoint.getY()))
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < currFigure.size(); i++) {
            if(!currFigure.get(i).equals(resourceFigure.get(i)))
                return false;
        }
        return true;
    }

    private enum Positioning {
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }
}
