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
            if (!fileToSave.getName().toLowerCase().endsWith(".svg")) {
                fileToSave = new File(fileToSave.getParent(), fileToSave.getName() + ".svg");
            }
            
            try {
                // Create SVG with desired dimensions
                int width = 800;
                int height = 600;
                SVGGraphics2D g2 = new SVGGraphics2D(width, height);
                
                // Set high-quality rendering hints
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw the chart
                chart.draw(g2, new Rectangle(width, height));
                
                // Get SVG content and modify for responsiveness
                String svgContent = g2.getSVGElement();
                svgContent = makeSvgResponsive(svgContent, width, height);
                
                // Save to file
                try (FileOutputStream fos = new FileOutputStream(fileToSave);
                     OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8")) {
                    writer.write(svgContent);
                    JOptionPane.showMessageDialog(parent, 
                        "Exported as zoomable vector SVG!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String makeSvgResponsive(String svg, int width, int height) {
        // Replace fixed dimensions with viewBox
        return svg.replaceFirst(
            "<svg width=\"" + width + "\" height=\"" + height + "\"",
            "<svg viewBox=\"0 0 " + width + " " + height + "\" " +
            "preserveAspectRatio=\"xMidYMid meet\""
        );
    }
}