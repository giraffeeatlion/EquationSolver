package Control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import Classes.FunctionExpression;
import Classes.Solver;

public class Plotter {
    public static double xMaxBound = 0;
    public static double xMinBound = 0;
    private static boolean isPlotting = false;
    public static boolean EnableToolTips = false;
    public static boolean EnableZeroesSolver = false;
    public static boolean EnableCriticalPointSolver = false;
    public static boolean EnableAreaCalculation = true;
    public static double areaXMin = 0;
    public static double areaXMax = 0;
    public static double total_points = 500;
    private static final double LARGE_JUMP_THRESHOLD = 30;

    public static XYSeriesCollection functionDataset = new XYSeriesCollection();
    private static XYSeriesCollection pointDataset = new XYSeriesCollection();
    public static XYSeriesCollection areaDataset = new XYSeriesCollection();

    private static XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
    private static XYLineAndShapeRenderer pointRenderer = new XYLineAndShapeRenderer(false, true);
    private static XYLineAndShapeRenderer boundRenderer = new XYLineAndShapeRenderer(true, false);
    private static XYAreaRenderer areaRenderer = new XYAreaRenderer() {
        @Override
        public Paint getSeriesPaint(int series) {
            return new Color(100, 100, 255, 80);
        }
    };

    static Color[] colors = {
        Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA,
        Color.CYAN, Color.PINK, Color.YELLOW, Color.GRAY, Color.DARK_GRAY
    };

    public static void plotExpressions() {
        if (isPlotting) return;
        isPlotting = true;

        double xMin = GUI_init.plot.getDomainAxis().getLowerBound();
        double xMax = GUI_init.plot.getDomainAxis().getUpperBound();
        xMaxBound = xMax;
        xMinBound = xMin;

        functionDataset.removeAllSeries();
        pointDataset.removeAllSeries();

        for (int i = 0; i < FunctionExpression.expressions.size(); i++) {
            FunctionExpression function = FunctionExpression.expressions.get(i);
            FunctionExpression derFunction = FunctionExpression.derivativeExpressions.get(i);
            FunctionExpression doubleDerFunction = FunctionExpression.doubleDerExpressions.get(i);

            XYSeries functionSeries = new XYSeries(function.getExpressionString() + " [" + i + "]");
            XYSeries derivativeSeries = function.plotDerivative() ? new XYSeries(derFunction.getExpressionString() + " derivative [" + i + "]") : null;
            XYSeries zeroSeries = EnableZeroesSolver ? new XYSeries(function.getExpressionString() + ": Zeroes [" + i + "]") : null;
            XYSeries criticalPointSeries = EnableCriticalPointSolver ? new XYSeries(function.getExpressionString() + ": Critical Points [" + i + "]") : null;

            double prevY = function.evaluate(xMin);
            double prevY_prime = derFunction.evaluate(xMin);
            double prevX = xMin;
            double resolution = (xMax - xMin) / total_points;
            if (resolution == 0) resolution = Double.MIN_VALUE;

            for (double x = xMin; x <= xMax; x += resolution) {
                try {
                    double y = function.evaluate(x);
                    double y_prime = derFunction.evaluate(x);
                    boolean largeJump = Math.abs(y - prevY) > LARGE_JUMP_THRESHOLD;

                    functionSeries.add(x, !largeJump ? y : null);
                    if (function.plotDerivative()) {
                        derivativeSeries.add(x, !largeJump ? y_prime : null);
                    }

                    if (EnableZeroesSolver && y * prevY <= 0) {
                        double zeroPoint = Solver.solve(function, derFunction, prevX, x);
                        if (Math.abs(function.evaluate(zeroPoint)) < 0.001)
                            zeroSeries.add(zeroPoint, 0);
                    }

                    if (EnableCriticalPointSolver && y_prime * prevY_prime <= 0) {
                        double criticalPoint = Solver.solve(derFunction, doubleDerFunction, prevX, x);
                        double critPointValue = function.evaluate(criticalPoint);
                        if (Math.abs(derFunction.evaluate(criticalPoint)) < 0.1)
                            criticalPointSeries.add(criticalPoint, critPointValue);
                    }

                    prevX = x;
                    prevY = y;
                    prevY_prime = y_prime;

                } catch (Exception e) {
                    System.err.println("Evaluation error at x = " + x + ": " + e.getMessage());
                }
            }

            functionDataset.addSeries(functionSeries);
            if (function.plotDerivative()) {
                functionDataset.addSeries(derivativeSeries);
            }

            if (EnableZeroesSolver) pointDataset.addSeries(zeroSeries);
            if (EnableCriticalPointSolver) pointDataset.addSeries(criticalPointSeries);
        }

        for (int i = 0; i < functionDataset.getSeriesCount(); i++) {
            lineRenderer.setSeriesStroke(i, new BasicStroke(2.0f));
            lineRenderer.setSeriesPaint(i, colors[i % colors.length]);
            lineRenderer.setSeriesShapesVisible(i, false);
        }

        for (int i = 0; i < pointDataset.getSeriesCount(); i++) {
            String key = pointDataset.getSeries(i).getKey().toString();
            if (key.contains("Critical Points")) {
                pointRenderer.setSeriesPaint(i, Color.DARK_GRAY);
                pointRenderer.setSeriesShape(i, new Rectangle2D.Double(-4, -4, 8, 8));
            } else {
                pointRenderer.setSeriesPaint(i, colors[(i + functionDataset.getSeriesCount()) % colors.length]);
                pointRenderer.setSeriesShape(i, new Ellipse2D.Double(-2, -2, 4, 4));
            }
        }

        GUI_init.plot.setDataset(0, functionDataset);
        GUI_init.plot.setRenderer(0, lineRenderer);
        GUI_init.plot.setDataset(1, pointDataset);
        GUI_init.plot.setRenderer(1, pointRenderer);

        if (EnableToolTips) {
            XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator(
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                new DecimalFormat("0.000"),
                new DecimalFormat("0.000")
            );
            lineRenderer.setDefaultToolTipGenerator(toolTipGenerator);
            pointRenderer.setDefaultToolTipGenerator(toolTipGenerator);
        } else {
            lineRenderer.setDefaultToolTipGenerator(null);
            pointRenderer.setDefaultToolTipGenerator(null);
        }

        GUI_init.plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        GUI_init.chart.fireChartChanged();
        isPlotting = false;
    }




    public static void updateAreaShading(FunctionExpression function, double areaXMin, double areaXMax) {
        // Find the series corresponding to the function
        XYSeries targetSeries = null;
        for (int i = 0; i < functionDataset.getSeriesCount(); i++) {
            XYSeries series = functionDataset.getSeries(i);
            String key = series.getKey().toString();
            if (key.startsWith(function.getExpressionString() + " [")) {
                targetSeries = series;
                break;
            }
        }
        if (targetSeries == null) {
            
            return;
        }

        // Build area series within bounds
        XYSeries areaSeries = new XYSeries("Area Under Curve");
        double areaSum = 0;
        double prevX = Double.NaN, prevY = Double.NaN;
        boolean firstPoint = true;

        for (int i = 0; i < targetSeries.getItemCount(); i++) {
            Number xNum = targetSeries.getX(i);
            Number yNum = targetSeries.getY(i);
            if (xNum == null || yNum == null) continue;
            double x = xNum.doubleValue();
            double y = yNum.doubleValue();
            if (x >= areaXMin && x <= areaXMax) {
                areaSeries.add(x, y);
                if (!firstPoint) {
                    areaSum += 0.5 * (y + prevY) * (x - prevX);
                }
                prevX = x;
                prevY = y;
                firstPoint = false;
            }
        }

        // Only update if at least two points found
        areaDataset.removeAllSeries();
        if (areaSeries.getItemCount() > 1) {
            // Close the area polygon
            areaSeries.add(areaXMax, 0);
            areaSeries.add(areaXMin, 0);
            areaSeries.add(areaSeries.getX(0), areaSeries.getY(0));

            areaDataset.addSeries(areaSeries);
            areaRenderer.setSeriesFillPaint(0, new Color(100, 100, 255, 50));

            DecimalFormat df = new DecimalFormat("0.000");
            GUI_init.plot.getRangeAxis().setLabel("Y (Area: " + df.format(Math.abs(areaSum)) + ")");
            GUI_init.plot.setDataset(2, areaDataset);
            GUI_init.plot.setRenderer(2, areaRenderer);
            XYSeriesCollection markerDataset = new XYSeriesCollection();
            for (double xBound : new double[]{areaXMin, areaXMax}) {
                String s = xBound==areaXMin?"Left Bound":"Right Bound";
                XYSeries markSeries = new XYSeries(s);
                markSeries.add(xBound,function.evaluate(xBound));
                markSeries.add(xBound,0);
                markerDataset.addSeries(markSeries);
            }
            for(int i = 0; i < markerDataset.getSeriesCount(); i++) {
                boundRenderer.setSeriesStroke(i, new BasicStroke(2.0f));
                boundRenderer.setSeriesPaint(i, Color.BLUE);
                boundRenderer.setSeriesShapesVisible(i, false);
            }
            GUI_init.plot.setDataset(3, markerDataset);
            GUI_init.plot.setRenderer(3, boundRenderer);
        } else {
            // No valid area, clear dataset and reset label
            GUI_init.plot.setDataset(2, null);
            GUI_init.plot.getRangeAxis().setLabel("Y");
        }
        
        GUI_init.chart.fireChartChanged();
    }
}