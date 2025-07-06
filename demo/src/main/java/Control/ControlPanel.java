package Control;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;

import Classes.FunctionExpression;
import Classes.FunctionRow;
import Classes.Solver;

public class ControlPanel {
    //This is for throttled rendering. To remember states lol.
    public static boolean plotZeroes = false;
    public static boolean plotSaddles = false;
    public static boolean toolTips = false;


    //adding a function the little thing where we type our function and alladat
    public static void addFunctionRow()
    {
        GUI_init.functionBar.add(new FunctionRow().createFunctionRow());
        GUI_init.functionBar.revalidate();
        GUI_init.functionBar.repaint();
    }


    public static void solveIntersections() {
    List<FunctionExpression> exprs = FunctionExpression.expressions;
    if (exprs.size() < 2) {
        JOptionPane.showMessageDialog(null, "Need at least 2 functions to find intersection.", "Not enough functions", JOptionPane.WARNING_MESSAGE);
        return;
    }

    StringBuilder result = new StringBuilder();
    double xMin = Plotter.xMinBound;
    double xMax = Plotter.xMaxBound;

    for (int i = 0; i < exprs.size(); i++) {
        for (int j = i + 1; j < exprs.size(); j++) {
            try {
                double x = Solver.intersectionSolver(exprs.get(i), exprs.get(j), xMin, xMax);
                double y = exprs.get(i).evaluate(x);
                result.append("funtion ").append(i).append(" & function ").append(j)
                      .append(" intersect at:\nx = ").append(String.format("%.4f", x))
                      .append(", y = ").append(String.format("%.4f", y)).append("\n\n");
            } catch (Exception ex) {
                result.append("f").append(i).append(" & f").append(j)
                      .append(": Error finding intersection: ").append(ex.getMessage()).append("\n\n");
            }
        }
    }

    JTextArea output = new JTextArea(result.toString(), 10, 40);
    output.setEditable(false);
    output.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
    JOptionPane.showMessageDialog(null, new JScrollPane(output), "Intersections", JOptionPane.INFORMATION_MESSAGE);
}


    public static void plotFunctions()
    {   
        //if we're replotting after adding new functions fire fire fire

        FunctionExpression.expressions = new ArrayList<>();
        FunctionExpression.derivativeExpressions = new ArrayList<>();
        FunctionExpression.doubleDerExpressions = new ArrayList<>();
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
                Plotter.EnableSaddlePointSolver = false;
                Plotter.plotExpressions();
                System.out.println(xMin + " " + xMax);
                
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
        plotSaddles = !plotSaddles;
        Plotter.EnableSaddlePointSolver = !Plotter.EnableSaddlePointSolver;
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
