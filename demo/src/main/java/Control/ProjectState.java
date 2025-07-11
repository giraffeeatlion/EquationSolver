package Control;

import java.io.Serializable;
import java.util.*;

public class ProjectState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<String> functionExpressions;
    private List<Boolean> hasDerivatives;
    private List<String> derivativeExpressions;
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    private boolean plotZeroes;
    private boolean plotSaddles;
    private boolean toolTips;
    private boolean plotIntersections;
    private boolean calculateArea;
    private double areaXMin;
    private double areaXMax;
    private String areaFunctionExpression;

    public ProjectState(List<String> functionExpressions, List<Boolean> hasDerivatives, 
                       List<String> derivativeExpressions, double xMin, double xMax, 
                       double yMin, double yMax, boolean plotZeroes, boolean plotSaddles,
                       boolean toolTips, boolean plotIntersections, boolean calculateArea,
                       double areaXMin, double areaXMax, String areaFunctionExpression) {
        this.functionExpressions = functionExpressions;
        this.hasDerivatives = hasDerivatives;
        this.derivativeExpressions = derivativeExpressions;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.plotZeroes = plotZeroes;
        this.plotSaddles = plotSaddles;
        this.toolTips = toolTips;
        this.plotIntersections = plotIntersections;
        this.calculateArea = calculateArea;
        this.areaXMin = areaXMin;
        this.areaXMax = areaXMax;
        this.areaFunctionExpression = areaFunctionExpression;
    }

    // Getters
    public List<String> getFunctionExpressions() { return functionExpressions; }
    public List<Boolean> getHasDerivatives() { return hasDerivatives; }
    public List<String> getDerivativeExpressions() { return derivativeExpressions; }
    public double getXMin() { return xMin; }
    public double getXMax() { return xMax; }
    public double getYMin() { return yMin; }
    public double getYMax() { return yMax; }
    public boolean isPlotZeroes() { return plotZeroes; }
    public boolean isPlotSaddles() { return plotSaddles; }
    public boolean isToolTips() { return toolTips; }
    public boolean isPlotIntersections() { return plotIntersections; }
    public boolean isCalculateArea() { return calculateArea; }
    public double getAreaXMin() { return areaXMin; }
    public double getAreaXMax() { return areaXMax; }
    public String getAreaFunctionExpression() { return areaFunctionExpression; }
}