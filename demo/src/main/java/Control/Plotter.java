package Control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
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

import com.esotericsoftware.kryo.util.Null;

import Classes.FunctionExpression;
import Classes.Solver;

public class Plotter {
    public static double xMaxBound = 0;
    public static double xMinBound = 0;
    private static boolean isPlotting = false;
    public static boolean EnableToolTips = false;
    public static boolean EnableZeroesSolver = false;
    public static boolean EnableCriticalPointSolver = false;
    public static boolean EnableIntersectionSolver = false;
    public static boolean EnableAreaCalculation = true;
    public static double areaXMin = 0;
    public static double areaXMax = 0;
    public static double total_points = 500;
    private static final double LARGE_JUMP_THRESHOLD = 30;

    public static XYSeriesCollection functionDataset = new XYSeriesCollection();
    private static XYSeriesCollection pointDataset = new XYSeriesCollection();
    public static XYSeriesCollection areaDataset = new XYSeriesCollection();
    public static XYSeriesCollection axisDataset = new XYSeriesCollection();
    private static XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
    private static XYLineAndShapeRenderer pointRenderer = new XYLineAndShapeRenderer(false, true);
    private static XYLineAndShapeRenderer boundRenderer = new XYLineAndShapeRenderer(true, false);
    private static XYLineAndShapeRenderer axisRenderer = new XYLineAndShapeRenderer(true, false);
    private static XYAreaRenderer areaRenderer = new XYAreaRenderer() {
        @Override
        public Paint getSeriesPaint(int series) {
            return new Color(100, 100, 255, 80);
        }
    };

    static Color[] colors = {
        Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.GRAY, Color.DARK_GRAY
    };

public static void plotExpressions() {
    if (isPlotting) return;
    isPlotting = true;

    double xMin = GUI_init.plot.getDomainAxis().getLowerBound();
    double xMax = GUI_init.plot.getDomainAxis().getUpperBound();
    xMaxBound = xMax;
    xMinBound = xMin;


    axisDataset.removeAllSeries();
    XYSeries xaxis = new XYSeries("X axis");
    XYSeries yaxis = new XYSeries("Y axis");
    xaxis.add(xMin,0);
    xaxis.add(xMax,0);
    yaxis.add(0,GUI_init.plot.getRangeAxis().getLowerBound());
    yaxis.add(0,GUI_init.plot.getRangeAxis().getUpperBound());
    axisDataset.addSeries(xaxis);
    axisDataset.addSeries(yaxis);
    GUI_init.plot.setDataset(0, axisDataset);
    GUI_init.plot.setRenderer(0, axisRenderer);

    for (int i = 0; i < axisDataset.getSeriesCount(); i++) {
        axisRenderer.setSeriesStroke(i, new BasicStroke(2.0f));
        axisRenderer.setSeriesPaint(i, Color.BLACK);
        axisRenderer.setSeriesShapesVisible(i, false);
    }

    functionDataset.removeAllSeries();
    pointDataset.removeAllSeries();

    for (int i = 0; i < FunctionExpression.expressions.size(); i++) {
        FunctionExpression function = FunctionExpression.expressions.get(i);
        FunctionExpression derFunction = FunctionExpression.derivativeExpressions.get(i);
        FunctionExpression doubleDerFunction = FunctionExpression.doubleDerExpressions.get(i);

        XYSeries functionSeries = new XYSeries(function.getExpressionString() + " [" + i + "]");
        XYSeries derivativeSeries = function.plotDerivative() ? new XYSeries(derFunction.getExpressionString() + " derivative [" + i + "]") : null;

        XYSeries zeroSeries = EnableZeroesSolver ? new XYSeries(function.getExpressionString() + ": Zeroes [" + i + "]") : null;
        XYSeries derZeroSeries = (EnableZeroesSolver && function.plotDerivative()) ? new XYSeries(derFunction.getExpressionString() + ": Zeroes [" + i + "]") : null;

        XYSeries saddleSeries = EnableCriticalPointSolver ? new XYSeries(function.getExpressionString() + ": Critical [" + i + "]") : null;
        XYSeries derSaddleSeries = (EnableCriticalPointSolver && function.plotDerivative()) ? new XYSeries(derFunction.getExpressionString() + ": Critical [" + i + "]") : null;

        



        double prevY = function.evaluate(xMin);
        double prevY_prime = derFunction.evaluate(xMin);
        double prevY_2prime = doubleDerFunction.evaluate(xMin);
        double prevX = xMin;
        double resolution = (xMax - xMin) / total_points;
        if (resolution == 0) resolution = Double.MIN_VALUE;

        for (double x = xMin; x <= xMax; x += resolution) {
            try {
                double y = function.evaluate(x);
                // if(!(!Double.isNaN(y) && !Double.isInfinite(y)))
                // {   
                //     functionSeries.add(x,null);
                //     derivativeSeries.add(x,null);
                //     continue;
                    
                // }
                double y_prime = derFunction.evaluate(x);
                double y_2prime = doubleDerFunction.evaluate(x);
                boolean largeJump = Math.abs(y - prevY) > LARGE_JUMP_THRESHOLD && y*prevY<0;

                functionSeries.add(x, !largeJump ? y : null);
                if (function.plotDerivative()) {
                    derivativeSeries.add(x, !largeJump ? y_prime : null);
                    
                }

                // Zeroes of function
                if (EnableZeroesSolver && !largeJump && (y * prevY <= 0 || (Math.abs(y) < 0.01 && Math.abs(y - prevY) < 0.01))) {
                    double zeroPoint = Solver.solve(function, derFunction, prevX, x);
                    if (Math.abs(function.evaluate(zeroPoint)) < 0.001)
                        zeroSeries.add(zeroPoint, 0);
                }

                // Zeroes of derivative
                if (EnableZeroesSolver && function.plotDerivative() && !largeJump && (y_prime * prevY_prime <= 0 || (Math.abs(y_prime) < 0.01 && Math.abs(y_prime - prevY_prime) < 0.01))) {
                    double derZeroPoint = Solver.solve(derFunction, doubleDerFunction, prevX, x);
                    if (Math.abs(derFunction.evaluate(derZeroPoint)) < 0.001)
                        derZeroSeries.add(derZeroPoint, 0);
                }

                // Saddles of function (where f'(x) = 0 and f''(x) ≈ 0)
                if (EnableCriticalPointSolver && (y_prime * prevY_prime <= 0 || (Math.abs(y_prime) < 0.01 && Math.abs(y_prime - prevY_prime) < 0.01))) {
                    double saddleX = Solver.solve(derFunction, doubleDerFunction, prevX, x);
                    double saddleY = function.evaluate(saddleX);
                    if (Math.abs(derFunction.evaluate(saddleX)) < 0.1 )
                        saddleSeries.add(saddleX, saddleY);
                }

                // Saddles of derivative (where f''(x) = 0 and f'''(x) ≈ 0)
                if (EnableCriticalPointSolver && function.plotDerivative() && !largeJump && (y_2prime * prevY_2prime <= 0 || (Math.abs(y_2prime) < 0.01 && Math.abs(y_2prime - prevY_2prime) < 0.01))) {
                    // For derivative's saddle, you would need triple derivative; assuming you have it:
                    FunctionExpression tripleDerFunction = FunctionExpression.tripleDerExpressions.get(i);
                    double derSaddleX = Solver.solve(doubleDerFunction, tripleDerFunction, prevX, x);
                    double derSaddleY = derFunction.evaluate(derSaddleX);
                    if (Math.abs(doubleDerFunction.evaluate(derSaddleX)) < 0.1)
                        derSaddleSeries.add(derSaddleX, derSaddleY);
                }

                prevX = x;
                prevY = y;
                prevY_prime = y_prime;
                prevY_2prime = y_2prime;

            } catch (Exception e) {
                //System.err.println("Evaluation error at x = " + x + ": " + e.getMessage());
            }
        }

        functionDataset.addSeries(functionSeries);
        if (function.plotDerivative()) {
            functionDataset.addSeries(derivativeSeries);
        }

        if (EnableZeroesSolver) pointDataset.addSeries(zeroSeries);
        if (EnableZeroesSolver && function.plotDerivative()) pointDataset.addSeries(derZeroSeries);

        if (EnableCriticalPointSolver) pointDataset.addSeries(saddleSeries);
        if (EnableCriticalPointSolver && function.plotDerivative()) pointDataset.addSeries(derSaddleSeries);
    }

    
    if(FunctionExpression.areaFunction != null)
        updateAreaShading(FunctionExpression.areaFunction, areaXMin, areaXMax);

    for (int i = 0; i < functionDataset.getSeriesCount(); i++) {
        lineRenderer.setSeriesStroke(i, new BasicStroke(2.0f));
        lineRenderer.setSeriesPaint(i, colors[i % colors.length]);
        lineRenderer.setSeriesShapesVisible(i, false);
    }

    for (int i = 0; i < pointDataset.getSeriesCount(); i++) {
        pointRenderer.setSeriesPaint(i, Color.BLUE);
        pointRenderer.setSeriesShape(i, new Ellipse2D.Double(-2, -2, 4, 4));
    }
    if (EnableIntersectionSolver) {
        Plotter.plotIntersections();
    }

    GUI_init.plot.setDataset(1, functionDataset);
    GUI_init.plot.setRenderer(1, lineRenderer);
    GUI_init.plot.setDataset(2, pointDataset);
    GUI_init.plot.setRenderer(2, pointRenderer);

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

    public static void plotIntersections() {
    // Optionally clear previous intersection series from pointDataset if needed

    XYSeries functionIntersectionSeries = new XYSeries("Function Intersections");

    double resolution = (xMaxBound - xMinBound) / total_points;
    if (resolution == 0) resolution = Double.MIN_VALUE;

    double tolerance = resolution / 4.0; // Stricter for duplicate filtering
    double epsilon = 1e-6; // For near-zero detection
    boolean inNearZero = false; // Debounce flag

    for (int i = 0; i < FunctionExpression.intersectionExpressions.size(); i++) {
        FunctionExpression intersectionFunction = FunctionExpression.intersectionExpressions.get(i);
        FunctionExpression intersectionDerivative = FunctionExpression.intersectionDerExpressions.get(i);
        FunctionExpression plotFunction = intersectionFunction.intersectExpression;

        double prevX = xMinBound;
        double prevY = 0;
        try {
            prevY = intersectionFunction.evaluate(prevX);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (double x = xMinBound + resolution; x <= xMaxBound; x += resolution) {
            double y;
            try {
                y = intersectionFunction.evaluate(x);
            } catch (Exception e) {
                prevX = x;
                prevY = Double.NaN;
                continue;
            }

            boolean isRoot = false;
            // Trigger only once per crossing or entry into near-zero region
            if(y * prevY <= 0 || (Math.abs(y) < 0.01 && Math.abs(y - prevY) < 0.01))
                isRoot = true;

            if (isRoot) {
                double rootX = Solver.solve(intersectionFunction, intersectionDerivative, prevX, x);
                if (!Double.isNaN(rootX)) {
                    double val = plotFunction.evaluate(rootX);
                    if (Math.abs(intersectionFunction.evaluate(rootX)) < 1e-6) {{
                           // System.out.println(intersectionFunction.getExpressionString()+"| derivative: "+intersectionDerivative.getExpressionString()+"\n"+plotFunction.getExpressionString()+": " +intersectionFunction.evaluate(rootX)+" : "+val);
                            functionIntersectionSeries.add(rootX, val);
                        }
                    }
                }
            }
            prevX = x;
            prevY = y;
        }
    }

    // Add the intersection series to the point dataset with appropriate styling
    if (functionIntersectionSeries.getItemCount() > 0) {
        pointDataset.addSeries(functionIntersectionSeries);
        int seriesIndex = pointDataset.getSeriesCount() - 1;
        pointRenderer.setSeriesPaint(seriesIndex, Color.DARK_GRAY);
        pointRenderer.setSeriesShape(seriesIndex, new Ellipse2D.Double(-3, -3, 6, 6));
    }
}
    public static void updateAreaShading(FunctionExpression function, double areaXMin, double areaXMax) {
    // Return early if function is null
    if (function == null) {
        areaDataset.removeAllSeries();
        GUI_init.plot.setDataset(3, null);
        GUI_init.plot.setDataset(4, null);
        GUI_init.plot.getRangeAxis().setLabel("Y");
        GUI_init.chart.fireChartChanged();
        return;
    }

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
Boolean hasNull = false;
    // Build area series within bounds
    XYSeries areaSeries = new XYSeries("Area Under Curve");
    double areaSum = 0;
    double prevX = Double.NaN, prevY = Double.NaN;
    boolean firstPoint = true;

    for (int i = 0; i < targetSeries.getItemCount(); i++) {
        Number xNum = targetSeries.getX(i);
        Number yNum = targetSeries.getY(i);
        if (xNum == null || yNum == null){
            if(xNum.doubleValue() >= areaXMin && xNum.doubleValue() <= areaXMax)
            {   areaSeries.add(xNum.doubleValue(),null);
                hasNull = true;
            }
            continue;
        }
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
        if(!hasNull)
            GUI_init.plot.getRangeAxis().setLabel("Y (Area: " + df.format(Math.abs(areaSum)) + ")");
        else
            GUI_init.plot.getRangeAxis().setLabel("Y (Area: Not defined)");
        GUI_init.plot.setDataset(3, areaDataset);
        GUI_init.plot.setRenderer(3, areaRenderer);
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
        GUI_init.plot.setDataset(4, markerDataset);
        GUI_init.plot.setRenderer(4, boundRenderer);
    } else {
        // No valid area, clear dataset and reset label
        GUI_init.plot.setDataset(3, null);
        GUI_init.plot.getRangeAxis().setLabel("Y");
    }
    
    GUI_init.chart.fireChartChanged();
}
}