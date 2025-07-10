package Control;

import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;

import Classes.FunctionExpression;
import Classes.FunctionRow;

public class ControlPanel {
    //This is for throttled rendering. To remember states lol.
    public static boolean plotZeroes = false;
    public static boolean plotSaddles = false;
    public static boolean toolTips = false;


    //adding a function the little thing where we type our function and alladat
    public static boolean calculateArea = true;
    public static double areaXMin = 0;
    public static double areaXMax = 0;

    // Add this method
    public static void AreaCalculator(FunctionExpression function) {
        // Determine the allowed x-range for the sliders
        double plotMin = Plotter.xMinBound;
        double plotMax = Plotter.xMaxBound;
        FunctionExpression.areaFunction = function;
        if(Plotter.areaXMin<plotMin)
        {
            Plotter.areaXMin = plotMin;
        }
        if(Plotter.areaXMax>plotMax)
        {
            Plotter.areaXMax = plotMax;
        }
        // Slider granularity (adjust as needed)
        int sliderSteps = 1000;
        int minSlider = 0;
        int maxSlider = sliderSteps;

        // Map current area bounds to slider positions
        int sliderXMin = (int) ((Plotter.areaXMin - plotMin) / (plotMax - plotMin) * sliderSteps);
        int sliderXMax = (int) ((Plotter.areaXMax - plotMin) / (plotMax - plotMin) * sliderSteps);

        JSlider xMinSlider = new JSlider(JSlider.HORIZONTAL, minSlider, maxSlider, sliderXMin);
        JSlider xMaxSlider = new JSlider(JSlider.HORIZONTAL, minSlider, maxSlider, sliderXMax);

        JLabel xMinLabel = new JLabel("From x: " + formatDouble(Plotter.areaXMin));
        JLabel xMaxLabel = new JLabel("To x: " + formatDouble(Plotter.areaXMax));
        // Synchronize sliders so min <= max
        ChangeListener sliderListener = e -> {
            int minVal = Math.min(xMinSlider.getValue(), xMaxSlider.getValue());
            int maxVal = Math.max(xMinSlider.getValue(), xMaxSlider.getValue());
            double xMin = plotMin + (plotMax - plotMin) * minVal / sliderSteps;
            double xMax = plotMin + (plotMax - plotMin) * maxVal / sliderSteps;
            xMinLabel.setText("From x: " + formatDouble(xMin));
            xMaxLabel.setText("To x: " + formatDouble(xMax));
            Plotter.areaXMin = xMin;
            Plotter.areaXMax = xMax;
            Plotter.updateAreaShading(function, xMin, xMax);
        };
        xMinSlider.addChangeListener(sliderListener);
        xMaxSlider.addChangeListener(sliderListener);

        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.add(xMinLabel);
        panel.add(xMinSlider);
        panel.add(xMaxLabel);
        panel.add(xMaxSlider);

        int result = JOptionPane.showConfirmDialog(
            null, panel, "Set Area Bounds", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            Plotter.EnableAreaCalculation = true;
            //Plotter.updateAreaShading(function, Double.NaN, Double.NaN); // Clear area if cancelled
        }
    }

    // Helper method to format double values
    private static String formatDouble(double value) {
        return new DecimalFormat("0.###").format(value);
    }
    public static void addFunctionRow()
    {
        GUI_init.functionBar.add(new FunctionRow().createFunctionRow());
        GUI_init.functionBar.revalidate();
        GUI_init.functionBar.repaint();
    }

    public static void plotFunctions()
    {   
        //if we're replotting after adding new functions fire fire fire
        FunctionExpression.areaFunction = null;
        FunctionExpression.expressions = new ArrayList<>();
        FunctionExpression.derivativeExpressions = new ArrayList<>();
        FunctionExpression.doubleDerExpressions = new ArrayList<>();
        FunctionExpression.tripleDerExpressions = new ArrayList<>();
        String expr;        
        for(FunctionRow rows: FunctionRow.functionRows)
        {
            expr = rows.getFunctionText();
            expr = FunctionExpression.autoFixParentheses(expr);
            rows.setFunctionText(expr);
            FunctionExpression.expressions.add(new FunctionExpression(expr,rows.hasDerivative()));
            expr = FunctionExpression.derivative(expr);
            rows.setDerivativeText(expr);
            FunctionExpression.derivativeExpressions.add(new FunctionExpression(expr,false));
            expr = FunctionExpression.derivative(expr);
            FunctionExpression.doubleDerExpressions.add(new FunctionExpression(expr,false));
            expr = FunctionExpression.derivative(expr);
            FunctionExpression.tripleDerExpressions.add(new FunctionExpression(expr,false));

        }
        GUI_init.functionBar.revalidate();
        GUI_init.functionBar.repaint();
        Plotter.plotExpressions();
    }
    
    public static void zoomOrPan()
    {   
        double xMin = GUI_init.plot.getDomainAxis().getLowerBound();
        double xMax = GUI_init.plot.getDomainAxis().getUpperBound();
        //only replotting if bounds changed
         if (xMin != Plotter.xMinBound || xMax != Plotter.xMaxBound) {
                Plotter.xMinBound = xMin;
                Plotter.xMaxBound = xMax;
                GUI_init.highResPending = true;
                GUI_init.highResTimer.restart();

                //disabling and reducing resolution druing process
                Plotter.total_points = 500;
                Plotter.EnableToolTips = false;
                Plotter.EnableZeroesSolver = false;
                Plotter.EnableCriticalPointSolver = false;
                Plotter.plotExpressions();
                //System.out.println(xMin + " " + xMax);
                
            }
    }
    public static void toggleZeroSolver()
    {
        plotZeroes = !plotZeroes;
        Plotter.EnableZeroesSolver = !Plotter.EnableZeroesSolver;
        Plotter.plotExpressions();
    }
    public static void toggleSaddleSolver()
    {
        ControlPanel.plotSaddles = !ControlPanel.plotSaddles;
        Plotter.EnableCriticalPointSolver = !Plotter.EnableCriticalPointSolver;
        System.out.println(plotSaddles + " " + Plotter.EnableCriticalPointSolver);
        Plotter.plotExpressions();
    }
    public static void toggleToolTips()
    {
        toolTips = !toolTips;
        Plotter.EnableToolTips = !Plotter.EnableToolTips;
        Plotter.plotExpressions();
    }
    public static void resetZoom()
    {
        GUI_init.plot.getDomainAxis().setRange(-10, 10);
        GUI_init.plot.getRangeAxis().setRange(-10, 10);
        Plotter.plotExpressions();
    }

    //this is if we wanna set a custom resolution. 
    public static int askResolution() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                null,
                "Enter resolution (positive integer):",
                "Set Resolution",
                JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) {
                return GUI_init.MaxResolution;
            }

            try {
                int res = Integer.parseInt(input.trim());
                if (res > 0) {
                    return res;
                } else {
                    JOptionPane.showMessageDialog(null, "Resolution must be a positive integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    //to set bounds manually
    public static void manualZoom()
    {
        JTextField xMinField = new JTextField(String.valueOf(GUI_init.plot.getDomainAxis().getLowerBound()));
        JTextField xMaxField = new JTextField(String.valueOf(GUI_init.plot.getDomainAxis().getUpperBound()));
        JTextField yMinField = new JTextField(String.valueOf(GUI_init.plot.getRangeAxis().getLowerBound()));
        JTextField yMaxField = new JTextField(String.valueOf(GUI_init.plot.getDomainAxis().getUpperBound()));

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("X Min:"));
        panel.add(xMinField);
        panel.add(new JLabel("X Max:"));
        panel.add(xMaxField);
        panel.add(new JLabel("Y Min:"));
        panel.add(yMinField);
        panel.add(new JLabel("Y Max:"));
        panel.add(yMaxField);

        int result = JOptionPane.showConfirmDialog(
            null, panel, "Set Zoom Bounds", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                double xMin = Double.parseDouble(xMinField.getText());
                double xMax = Double.parseDouble(xMaxField.getText());
                double yMin = Double.parseDouble(yMinField.getText());
                double yMax = Double.parseDouble(yMaxField.getText());
                GUI_init.plot.getDomainAxis().setRange(xMin, xMax);
                GUI_init.plot.getRangeAxis().setRange(yMin, yMax);
                Plotter.plotExpressions();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter valid numbers.");
            }
        }
    }
}
