package com.recognition;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Picture {
    private List<Figure> figures = new ArrayList<>();
    private String filePath;

    public boolean isSimilarTo(Picture resource) {
        if(resource == null) return false;
        List<Figure> currFigures = this.getFigures();
        List<Figure> resourceFigures = resource.getFigures();

        int size;
        if((size = currFigures.size()) != resourceFigures.size()) return false;
        if(size == 1) {
            return checkCoincidences(resource);
        } else return false;
    }

    private boolean checkCoincidences(Picture resource) {
        List<FigurePoint> currFigure = figures.get(0).getPixels();
        List<FigurePoint> resourceFigure = resource.getFigures().get(0).getPixels();

        for (int i = 0; i < currFigure.size(); i++) {
            if(!currFigure.get(i).equals(resourceFigure.get(i)))
                return false;
        }
        return true;
    }
}
