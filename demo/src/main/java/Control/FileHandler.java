package Control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import Classes.FunctionExpression;
import Classes.FunctionRow;

public class FileHandler {
    public static void saveProjectState() {
        // Collect current state
        List<String> expressions = new ArrayList<>();
        List<Boolean> hasDerivatives = new ArrayList<>();
        List<String> derivativeExpressions = new ArrayList<>();
        
        for (FunctionRow row : FunctionRow.functionRows) {
            expressions.add(row.getFunctionText());
            hasDerivatives.add(row.hasDerivative());
            derivativeExpressions.add(row.hasDerivative() ? row.getDerivativeText() : "");
        }
        
        String areaFunctionExpr = FunctionExpression.areaFunction != null ? 
            FunctionExpression.areaFunction.getExpressionString() : null;
        
        ProjectState state = new ProjectState(
            expressions,
            hasDerivatives,
            derivativeExpressions,
            GUI_init.plot.getDomainAxis().getLowerBound(),
            GUI_init.plot.getDomainAxis().getUpperBound(),
            GUI_init.plot.getRangeAxis().getLowerBound(),
            GUI_init.plot.getRangeAxis().getUpperBound(),
            ControlPanel.plotZeroes,
            ControlPanel.plotSaddles,
            ControlPanel.toolTips,
            ControlPanel.plotIntersections,
            ControlPanel.calculateArea,
            Plotter.areaXMin,
            Plotter.areaXMax,
            areaFunctionExpr
        );
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Project");
        fileChooser.setFileFilter(new FileNameExtensionFilter("GraphCalc Project", "gcproj"));
        
        int userSelection = fileChooser.showSaveDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            
            if (!filePath.toLowerCase().endsWith(".gcproj")) {
                fileToSave = new File(filePath + ".gcproj");
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileToSave))) {
                oos.writeObject(state);
                JOptionPane.showMessageDialog(null, "Project saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error saving project: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void loadProjectState() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Project");
        fileChooser.setFileFilter(new FileNameExtensionFilter("GraphCalc Project", "gcproj"));
        
        int userSelection = fileChooser.showOpenDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileToLoad))) {
                ProjectState state = (ProjectState) ois.readObject();
                
                // Clear current state
                FunctionRow.functionRows.clear();
                GUI_init.functionBar.removeAll();
                FunctionExpression.expressions.clear();
                FunctionExpression.derivativeExpressions.clear();
                FunctionExpression.doubleDerExpressions.clear();
                FunctionExpression.tripleDerExpressions.clear();
                FunctionExpression.intersectionExpressions.clear();
                FunctionExpression.intersectionDerExpressions.clear();
                FunctionExpression.areaFunction = null;
                
                // Restore functions
                for (int i = 0; i < state.getFunctionExpressions().size(); i++) {
                    FunctionRow row = new FunctionRow();
                    GUI_init.functionBar.add(row.createFunctionRow());
                    row.setFunctionText(state.getFunctionExpressions().get(i));
                    
                    if (state.getHasDerivatives().get(i)) {
                        row.addDerivativeField();
                        row.setDerivativeText(state.getDerivativeExpressions().get(i));
                    }
                }
                
                // Restore view bounds
                GUI_init.plot.getDomainAxis().setRange(state.getXMin(), state.getXMax());
                GUI_init.plot.getRangeAxis().setRange(state.getYMin(), state.getYMax());
                
                // Restore settings
                ControlPanel.plotZeroes = state.isPlotZeroes();
                ControlPanel.plotSaddles = state.isPlotSaddles();
                ControlPanel.toolTips = state.isToolTips();
                ControlPanel.plotIntersections = state.isPlotIntersections();
                ControlPanel.calculateArea = state.isCalculateArea();
                
                Plotter.EnableZeroesSolver = state.isPlotZeroes();
                Plotter.EnableCriticalPointSolver = state.isPlotSaddles();
                Plotter.EnableToolTips = state.isToolTips();
                Plotter.EnableIntersectionSolver = state.isPlotIntersections();
                Plotter.EnableAreaCalculation = state.isCalculateArea();
                
                Plotter.areaXMin = state.getAreaXMin();
                Plotter.areaXMax = state.getAreaXMax();
                
                // Restore area function if exists
                if (state.getAreaFunctionExpression() != null) {
                    for (FunctionExpression expr : FunctionExpression.expressions) {
                        if (expr.getExpressionString().equals(state.getAreaFunctionExpression())) {
                            FunctionExpression.areaFunction = expr;
                            break;
                        }
                    }
                }
                
                // Replot
                ControlPanel.plotFunctions();
                
                JOptionPane.showMessageDialog(null, "Project loaded successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "Error loading project: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}