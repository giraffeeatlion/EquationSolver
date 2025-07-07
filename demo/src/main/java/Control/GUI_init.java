package Control;

import org.jfree.chart.ChartPanel;
import javax.swing.*;
import java.awt.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.ValueMarker;
import java.awt.event.*;

public class GUI_init {
    public static JFreeChart chart;
    public static ChartPanel chartPanel;
    public static JPanel controlBar = new JPanel();
    public static JPanel functionBar = new JPanel();
    public static JScrollPane functionScrollPane;
    public static JPanel functionPanelWrapper = new JPanel(new BorderLayout()); // ✅ wrapper to control scroll resize
    public static XYPlot plot;
    public static Timer highResTimer;
    public static JCheckBox autoPlotCheckbox = new JCheckBox("AutoPlot");
    public static int MaxResolution = 1000;
    public static boolean highResPending = false;

    public GUI_init() {
        init_chart();
        functionBar.setLayout(new BoxLayout(functionBar, BoxLayout.Y_AXIS));

        // UI buttons
        JButton plotGraphBtn = new JButton("Plot Graph");
        JButton actionMenuButton = new JButton("Actions");
        JButton addFunctionButton = new JButton("Add Function");

        // Popup menu for additional actions
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add Function");
        JMenuItem plotItem = new JMenuItem("Plot Graph");
        JMenuItem resetItem = new JMenuItem("Reset Zoom");
        JMenuItem setZoomManual = new JMenuItem("Set Bounds Manually");
        JMenuItem toggleZeroesSolver = new JMenuItem("Toggle Zero Solver");
        JMenuItem toggleSaddleSolver = new JMenuItem("Toggle Saddle Solver");
        JMenuItem toggleToolTips = new JMenuItem("Toggle Point Tooltips");
        JMenuItem setResolution = new JMenuItem("Set Custom Resolution");
        JMenuItem intersectionSolver = new JMenuItem("Solve for Intersections");

        popupMenu.add(addItem);
        popupMenu.add(plotItem);
        popupMenu.add(resetItem);
        popupMenu.add(setZoomManual);
        popupMenu.add(toggleZeroesSolver);
        popupMenu.add(toggleSaddleSolver);
        popupMenu.add(toggleToolTips);
        popupMenu.add(setResolution);
        popupMenu.add(intersectionSolver);

        actionMenuButton.addActionListener(e -> popupMenu.show(actionMenuButton, 0, actionMenuButton.getHeight()));
        addItem.addActionListener(e -> ControlPanel.addFunctionRow());
        plotItem.addActionListener(e -> ControlPanel.plotFunctions());
        resetItem.addActionListener(e -> ControlPanel.resetZoom());
        setZoomManual.addActionListener(e -> ControlPanel.manualZoom());
        toggleZeroesSolver.addActionListener(e -> ControlPanel.toggleZeroSolver());
        toggleSaddleSolver.addActionListener(e -> ControlPanel.toggleSaddleSolver());
        toggleToolTips.addActionListener(e -> ControlPanel.toggleToolTips());
        setResolution.addActionListener(e -> MaxResolution = ControlPanel.askResolution());
        intersectionSolver.addActionListener(e -> ControlPanel.solveIntersections());

        controlBar.add(addFunctionButton);
        controlBar.add(actionMenuButton);
        controlBar.add(plotGraphBtn);
        controlBar.add(autoPlotCheckbox);

        addFunctionButton.addActionListener(e -> ControlPanel.addFunctionRow());
        plotGraphBtn.addActionListener(e -> ControlPanel.plotFunctions());

        // Scroll Pane and wrapper
        functionScrollPane = new JScrollPane(functionBar);
        functionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        functionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        functionScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        functionScrollPane.setPreferredSize(new Dimension(320, 500));
        functionBar.setAlignmentY(Component.TOP_ALIGNMENT);

        functionPanelWrapper.add(functionScrollPane, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(controlBar, BorderLayout.NORTH);
        leftPanel.add(functionPanelWrapper, BorderLayout.CENTER);

        // Instructions area
        JTextArea instructions = new JTextArea(
            "Instructions:\n" +
            "- Enter math expressions like sin(x), x^2, etc.\n" +
            "- Tick derivative checkbox to plot f'(x)\n" +
            "- Use zoom/pan (Ctrl + Scroll/Move)\n" +
            "- Ctrl+Click to reset zoom"
        );
        instructions.setEditable(false);
        instructions.setFont(new Font("Monospaced", Font.PLAIN, 12));
        instructions.setBackground(new Color(245, 245, 245));
        instructions.setBorder(BorderFactory.createTitledBorder("Help"));

        JScrollPane instructionScroll = new JScrollPane(instructions);
        instructionScroll.setPreferredSize(new Dimension(5, 100));
        leftPanel.add(instructionScroll, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, chartPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0);

        JFrame frame = new JFrame("Graphing Calculator and Equation Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(splitPane);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Add 1 row at startup
        ControlPanel.addFunctionRow();

        highResTimer = new Timer(100, e -> {
            if (highResPending) {
                Plotter.total_points = MaxResolution;
                Plotter.EnableToolTips = ControlPanel.toolTips;
                Plotter.EnableZeroesSolver = ControlPanel.plotZeroes;
                Plotter.EnableSaddlePointSolver = ControlPanel.plotSaddles;
                Plotter.plotExpressions();
                highResPending = false;
            }
        });
        highResTimer.setRepeats(false);

        plot.addChangeListener(e -> ControlPanel.zoomOrPan());
    }

    public void init_chart() {
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        xAxis.setRange(-10.0, 10.0);
        yAxis.setRange(-10.0, 10.0);

        plot = new XYPlot(null, xAxis, yAxis, null);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        ValueMarker verticalLine = new ValueMarker(0.0);
        verticalLine.setPaint(Color.BLACK);
        verticalLine.setStroke(new BasicStroke(2.5f));
        plot.addDomainMarker(verticalLine);

        ValueMarker horizontalLine = new ValueMarker(0.0);
        horizontalLine.setPaint(Color.BLACK);
        horizontalLine.setStroke(new BasicStroke(2.5f));
        plot.addRangeMarker(horizontalLine);

        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        chart = new JFreeChart("Graphing Calculator", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setDisplayToolTips(true);
    }
}
