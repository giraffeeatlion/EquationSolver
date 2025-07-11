package Control;

import org.jfree.chart.JFreeChart;
import org.jfree.svg.SVGGraphics2D;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class VectorSVGExporter {
    
    public static void exportAsVectorSVG(JFreeChart chart, Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export as Vector SVG");
        fileChooser.setFileFilter(new FileNameExtensionFilter("SVG Files", "svg"));
        
        int userSelection = fileChooser.showSaveDialog(parent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure .svg extension
            if (!fileToSave.getName().toLowerCase().endsWith(".svg")) {
                fileToSave = new File(fileToSave.getParent(), fileToSave.getName() + ".svg");
            }
            
            try {
                // Create SVG graphics context
                SVGGraphics2D g2 = new SVGGraphics2D(800, 600);
                
                // Set anti-aliasing for better quality
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw the chart to the SVG graphics context
                chart.draw(g2, new Rectangle(800, 600));
                
                // Write to file
                try (FileOutputStream fos = new FileOutputStream(fileToSave);
                     OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8")) {
                    writer.write(g2.getSVGDocument());
                    JOptionPane.showMessageDialog(parent, 
                        "Chart successfully exported as vector SVG!", 
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                    "Error exporting SVG: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}