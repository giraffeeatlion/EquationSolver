package Control;

import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import Classes.FunctionExpression;
import Classes.Solver;

import java.awt.Color;
import java.text.DecimalFormat;

//smol problem (i thought saddles were points where f'(x) = 0 but it is not so however i plotted all f'(x) = 0)

public class Plotter {
    public static double xMaxBound = 0; //current maxLeftBound
    public static double xMinBound = 0; //current maxrightBound
    static double points = 0;   //chumma for debugging
    private static boolean isPlotting = false;//this is for making sure throttled rendering works well
    //the next three are self explanatory
    public static boolean EnableToolTips = true; 
    public static boolean EnableZeroesSolver = false;
    public static boolean EnableSaddlePointSolver = false;

    //this is the resolution in a way. total number of x values evenly spaced to calculate corresponding y values.
    public static double total_points = 2000;

    //
    public static XYSeriesCollection axisDataset = new XYSeriesCollection();

    //This will have all the data that the functions will need
    public static XYSeriesCollection functionDataset = new XYSeriesCollection();
    
    //data of points (saddles and zeroes)
    private static XYSeriesCollection pointDataset = new XYSeriesCollection();

    //this is places where the derivative is not defined/ explodes to infinity (eg. floor(x))
    private static XYSeriesCollection discontinuityDataset = new XYSeriesCollection();

    //this is to set the colours and all of the plotlines and plotpoint to render them lol. discontinuity renderer and point renderer were seperated because point renderes fill be filled and discontinuity renderer will not be fi
    private static XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true,false);
    private static XYLineAndShapeRenderer pointRenderer = new XYLineAndShapeRenderer(false, true); // points only 
    private static XYLineAndShapeRenderer discontinuityRenderer = new XYLineAndShapeRenderer(false, true); // points only
    private static XYLineAndShapeRenderer axisRenderer = new XYLineAndShapeRenderer(true, false); 
    
    //These thresholds are for understanding whether the function has discontinuities or some crazy ahh jump. They have to be tuned for better ferformace possible by changing it depending on range.
    private static final double THRESHOLD = 15;
    private static final double FUNCTION_THRESHOLD = 30;

    //these colors are for plotting colors lol
    static Color[] colors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA,
            Color.CYAN, Color.PINK, Color.YELLOW, Color.GRAY, Color.DARK_GRAY
        };
    


    //rah the actual plotting
    public static void plotExpressions()
    {   
        
        if (isPlotting) return;  // Prevent re-entry
            isPlotting = true;

        //finding what the plotsize is currently (if zoom or set custom bounds etc etc)
        double xMin = GUI_init.plot.getDomainAxis().getLowerBound();
        double xMax = GUI_init.plot.getDomainAxis().getUpperBound();
        double yMax = GUI_init.plot.getRangeAxis().getUpperBound();
        double yMin = GUI_init.plot.getRangeAxis().getLowerBound();
        xMaxBound = xMax;
        xMinBound = xMin;
        
        //we remove the values from the previous time we plotted or else omg too many points will be plotted off and boom pc explodes
        functionDataset.removeAllSeries();
        pointDataset.removeAllSeries();
        discontinuityDataset.removeAllSeries();
        axisDataset.removeAllSeries();

        XYSeries xAxisLine = new XYSeries("X-Axis");
        xAxisLine.add(xMin, 0);
        xAxisLine.add(xMax, 0);

        XYSeries yAxisLine = new XYSeries("Y-Axis");
        yAxisLine.add(0, yMin);
        yAxisLine.add(0, yMax);


        axisDataset.addSeries(xAxisLine);
        axisDataset.addSeries(yAxisLine);

        axisRenderer.setSeriesStroke(0, new BasicStroke(3.0f));
        axisRenderer.setSeriesStroke(1, new BasicStroke(3.0f));
        axisRenderer.setSeriesPaint(0, Color.BLACK);
        axisRenderer.setSeriesPaint(1, Color.BLACK);

        GUI_init.plot.setDataset(0, axisDataset);      // Use slot 3 or next free slot
        GUI_init.plot.setRenderer(0, axisRenderer);
        //iterating through the functions that have to be plotted
        for(int i = 0; i < FunctionExpression.expressions.size(); i++)
        {   
            //doing the necessary inits
            FunctionExpression function = FunctionExpression.expressions.get(i);
            FunctionExpression derFunction = FunctionExpression.derivativeExpressions.get(i);
            FunctionExpression doubleDerFunction = FunctionExpression.doubleDerExpressions.get(i);

            //these are series for each function which will be added to appropriate datasets later on
            XYSeries functionSeries = new XYSeries(function.getExpressionString() + " " +" [" + i + "]");
            XYSeries derivativeSeries = new XYSeries(derFunction.getExpressionString() + " " +" [" + i + "]");
            XYSeries discontinuitySeries = new XYSeries(derFunction.getExpressionString() + " discontinuity" +" [" + i + "]");

            //these are only initialized if corresponding booleans are true.
            XYSeries zeroSeries = null;
            XYSeries saddleSeries = null;
            XYSeries derZeroSeries = null;
            XYSeries derSaddleSeries = null;
            if(EnableZeroesSolver)
            {   
                zeroSeries = new XYSeries(function.getExpressionString() + ": Zeroes");
                if(function.plotDerivative())
                {
                    derZeroSeries = new XYSeries(derFunction.getExpressionString() + ": Zeroes" +" [" + i + "]");
                }
            }
            if(EnableSaddlePointSolver)
            {
                saddleSeries = new XYSeries(function.getExpressionString() + ": Saddles");
                if(function.plotDerivative())
                {
                    derSaddleSeries = new XYSeries(derFunction.getExpressionString() + ": Saddles" +" [" + i + "]"); //notUsed
                }
            }


            //this is to store prev values. we'll use this later on to find appropriate zeroes
            double prevY = function.evaluate(xMin);
            double prevY_prime = derFunction.evaluate(xMin);
            double prevY_2prime = doubleDerFunction.evaluate(xMin);
            double prevX = xMin;
            
            double resolution = (xMax-xMin)/total_points;
            if(resolution == 0)
                resolution = Double.MIN_VALUE;
            
            for (double x = xMin; x <= xMax; x += resolution)
            {
                try{
                    double y = function.evaluate(x);
                    double y_prime = derFunction.evaluate(x);
                    double y_2prime = doubleDerFunction.evaluate(x);
                    if(y == Double.NEGATIVE_INFINITY || y_prime==Double.NEGATIVE_INFINITY|| y_2prime == Double.NEGATIVE_INFINITY)
                        break;
                    if (!Double.isNaN(y) && !Double.isInfinite(y))
                    {   
                        double approxSlope = (y - prevY) / resolution;
                        if(Math.abs(y -prevY-prevY_prime*resolution) < 0.1) //if(y is partially close to prevY +dy/dx*(del(x)))
                        {
                            functionSeries.add(x,y);
                            if(function.plotDerivative())//ONLY IF WE HAVE TO PLOT DER
                            {   

                                if(Math.abs((y_prime-(function.evaluate(x+resolution/2)-function.evaluate(x-resolution/2))/resolution))<FUNCTION_THRESHOLD)//if dy/dx is sorta equal to dely/delx
                                {   
                                    derivativeSeries.add(x,y_prime);
                                    if(EnableZeroesSolver && Math.abs(y_prime-prevY_prime)<THRESHOLD && y_prime*prevY_prime<=0)//only if y' changed signs, we search for zeroes. the reason we have the threshold is because functions like tan(x)can change from infinity to -infinity and it would count as a sign change and we'd try solving for a zero then.
                                    {   
                                        double zeroPoint = Solver.solve(derFunction,doubleDerFunction,prevX,x);
                                        derZeroSeries.add(zeroPoint,0);
                                    }
                                }
                                else  
                                {   
                                    //if it is way above the threshold, signs of obvious abnormality therefore we plot discontinuities but tbh this only works well with floor.
                                    //discontinuitySeries.add(prevX,prevY_prime);
                                    //discontinuitySeries.add(x,y_prime);
                                    if(y_prime*prevY_prime<=0)
                                        derivativeSeries.add(x,null);
                                    else
                                        derivativeSeries.add(x,y_prime);
                                    //functionSeries.add(x,null);
                                    //derivativeSeries.add(x,null);
                                }
                            }
                            if(EnableZeroesSolver && Math.abs(y-prevY)<THRESHOLD && y*prevY<=0)//only solves for zeroes when there is a sign change
                            {
                                double zeroPoint = Solver.solve(function,derFunction,prevX,x);
                                zeroSeries.add(zeroPoint,0);
                            }
                            if(EnableSaddlePointSolver)
                            {
                                if(Math.abs(y_prime-prevY_prime)<THRESHOLD && y_prime*prevY_prime<=0)//solves for f'(x) = 0
                                    {   
                                        double saddlePoint = Solver.solve(derFunction,doubleDerFunction,prevX,x);
                                        saddleSeries.add(saddlePoint,function.evaluate(saddlePoint));
                                    }
                            }
                        }
                        else{
                            if(function.plotDerivative())
                             {   
                            derivativeSeries.add(x,y_prime);
                            discontinuitySeries.add(x,y_prime);
                            discontinuitySeries.add(prevX,prevY_prime);}//we add this because the actual function could have discontinuities in general but the derivative might not in that range (eg. log(x) is not defined for negative numbers but its derivative is and we dont wanna plot that BY MISTAKE ALSO but adding this just makes sure there arent crazy lines in between connecting to each other )
                            if(y*prevY<=0)
                                functionSeries.add(x,null);//no crazy line connections
                            else
                                functionSeries.add(x,y);
                        }
                    }
                    else{
                        if(function.plotDerivative())
                        {
                            derivativeSeries.add(x,null);
                        }
                        functionSeries.add(x,null);
                    }

                    //updating prev values
                    prevX = x;
                    prevY = y;
                    prevY_prime = y_prime;
                    prevY_2prime = y_2prime;
                    
                }
                catch(Exception e){
                    System.err.println("Evaluation error at x = " + x + ": " + e.getMessage());
                }
            }
            xMaxBound = xMax;
            xMinBound = xMin;
            //adding all the series to appropriate datasets
            functionDataset.addSeries(functionSeries);

            //these if conditions are present because the series will be null otherwise
            if(function.plotDerivative())
            {
                functionDataset.addSeries(derivativeSeries);
                if(EnableZeroesSolver)
                {
                    pointDataset.addSeries(derZeroSeries);
                }
                discontinuityDataset.addSeries(discontinuitySeries);
            }
            if(EnableZeroesSolver)
            {
                pointDataset.addSeries(zeroSeries);
            }
            if(EnableSaddlePointSolver)
                pointDataset.addSeries(saddleSeries);      

        }


        //setting rendering
        for(int i = 0; i < functionDataset.getSeriesCount(); i++)
        {
            lineRenderer.setSeriesStroke(i, new BasicStroke(2.0f));
            lineRenderer.setSeriesPaint(i, colors[i % colors.length]);
            lineRenderer.setSeriesShapesVisible(i, false); // no points
        }

        //z_indexing
        GUI_init.plot.setDataset(1,functionDataset);
        GUI_init.plot.setRenderer(1,lineRenderer);

        //rendering for point dataset and discontinuity dataset and appropriately rendering them
        if(pointDataset != null)
        {
            for (int i = 0; i < pointDataset.getSeriesCount(); i++) {
            pointRenderer.setSeriesPaint(i, colors[(i + functionDataset.getSeriesCount()) % colors.length]);
            pointRenderer.setSeriesShape(i, new java.awt.geom.Ellipse2D.Double(-2, -2, 4, 4)); // small circle
            }
            GUI_init.plot.setDataset(2,pointDataset);
            GUI_init.plot.setRenderer(2,pointRenderer);
        }
        if(discontinuityDataset != null)
        {
            for (int i = 0; i < discontinuityDataset.getSeriesCount(); i++) {
            discontinuityRenderer.setSeriesPaint(i, colors[(i + functionDataset.getSeriesCount()) % colors.length]);
            discontinuityRenderer.setSeriesShape(i, new java.awt.geom.Ellipse2D.Double(-2, -2, 4, 4)); // small circle
            discontinuityRenderer.setSeriesShapesFilled(i, false);
            discontinuityRenderer.setSeriesShapesVisible(i, true);
            }
            GUI_init.plot.setDataset(3,discontinuityDataset);
            GUI_init.plot.setRenderer(3,discontinuityRenderer); 
        }

        //setting tooltips if enabled
        if(EnableToolTips)
        {
            XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator(
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                new DecimalFormat("0.000"),
                new DecimalFormat("0.000")
            );
            lineRenderer.setDefaultToolTipGenerator(toolTipGenerator);
            pointRenderer.setDefaultToolTipGenerator(toolTipGenerator);
            discontinuityRenderer.setDefaultToolTipGenerator(toolTipGenerator);
        }
        else{
            lineRenderer.setDefaultToolTipGenerator(null);
            pointRenderer.setDefaultToolTipGenerator(null);
            discontinuityRenderer.setDefaultToolTipGenerator(null);
        }

        isPlotting = false;
        //to implementing z_indexing in ascending order. things of index 1 will be plotted below index 1
        GUI_init.plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD); 
        GUI_init.chart.fireChartChanged();//firing wow

    }
}
