package Control;

import org.jfree.chart.ChartPanel;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.BasicStroke;
import org.jfree.chart.plot.ValueMarker;
import java.awt.Color;
import java.awt.Font;



import Control.Plotter;
//import tech.tablesaw.plotly.components.Font;




public class GUI_init {

    //JfreeChart ka chart this is
    public static JFreeChart chart;

    //Where the chart will be placed
    public static ChartPanel chartPanel;

    //Where you'll be able to see the options to add function, plotgraph, menu and alladat.
    public static JPanel controlBar = new JPanel();
    public static JPanel functionBar = new JPanel();
    static boolean isInMotion = false;
    static boolean highResPending = false;
    public static int MaxResolution = 1000;
    public static XYPlot plot;
    public static Timer highResTimer;

    public GUI_init()
    {
        init_chart();//below this function
        //i have to change this because layout is lil dodgy        
        functionBar.setLayout(new BoxLayout(functionBar, BoxLayout.Y_AXIS));
        

        //All of these are sorta self explanatory
        JButton plotGraphBtn = new JButton("Plot Graph");
        JButton actionMenuButton = new JButton("Actions");
        JButton addFunctionButton = new JButton("add Function");

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add Function");
        JMenuItem plotItem = new JMenuItem("Plot Graph");
        JMenuItem resetItem = new JMenuItem("Reset Zoom");
        JMenuItem setZoomManual = new JMenuItem("Set Bounds Manually");
        JMenuItem toggleZeroesSolver = new JMenuItem("Toggle Zero Solver");
        JMenuItem toggleSaddleSolver = new JMenuItem("Toggle Saddle Solver");
        JMenuItem toggleToolTips = new JMenuItem("Toggle Point Tooltips");
        JMenuItem setResolution = new JMenuItem("Set Custom Resolution");
        //JMenuItem intersectionSolver = new JMenuItem("Solve for intersections");
        popupMenu.add(addItem);
        popupMenu.add(plotItem);
        popupMenu.add(resetItem);
        popupMenu.add(setZoomManual);
        popupMenu.add(toggleZeroesSolver);
        popupMenu.add(toggleSaddleSolver);
        popupMenu.add(toggleToolTips);
        popupMenu.add(setResolution);
        actionMenuButton.addActionListener(e -> popupMenu.show(actionMenuButton, 0, actionMenuButton.getHeight()));

        addItem.addActionListener(e->{
            ControlPanel.addFunctionRow();
        });
        controlBar.add(addFunctionButton);
        controlBar.add(actionMenuButton);
        controlBar.add(plotGraphBtn);
        addFunctionButton.addActionListener(e->{
            ControlPanel.addFunctionRow();
        });
        plotGraphBtn.addActionListener(e->{
            ControlPanel.plotFunctions();
        });
        
        resetItem.addActionListener(e->
        {
            ControlPanel.resetZoom();
        });
        toggleZeroesSolver.addActionListener(e->{
            ControlPanel.toggleZeroSolver();
        });
        toggleSaddleSolver.addActionListener(e->{
            ControlPanel.toggleSaddleSolver();
        });
        toggleToolTips.addActionListener(e->{
            ControlPanel.toggleToolTips();
        });
        setResolution.addActionListener(e->
        {
            MaxResolution = ControlPanel.askResolution();
        });
        setZoomManual.addActionListener(e->{
            ControlPanel.manualZoom();
        });
        

        
        //Implementing throttled rendering, so if someone is zooming or panning, we disable all solvers and tooltips and reduce resolution. We enable them and replot with higher resolution after a few milliseconds of it being idle. So lets say we're just chumma zooming in and out and panning nonStop. plotting at a hihgerResolution while solving for points nonStop is gonna slow down the entire process so during that chumma time we'll disable them and re-enable them if need be.
        highResTimer = new Timer(100,e->{
            if(highResPending)
            {   
                Plotter.total_points = MaxResolution;
                Plotter.EnableToolTips = ControlPanel.toolTips;
                Plotter.EnableZeroesSolver = ControlPanel.plotZeroes;
                Plotter.EnableSaddlePointSolver = ControlPanel.plotSaddles;
                Plotter.plotExpressions();
                highResPending = false;
            }
        });
        highResTimer.setRepeats(false);

        plot.addChangeListener(e->
        {
            ControlPanel.zoomOrPan();
        });

        //i still dk how to use this pls help
        JScrollPane scrollPane = new JScrollPane(functionBar);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        //final setup
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(controlBar,BorderLayout.NORTH);
        leftPanel.add(scrollPane,BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftPanel,
            chartPanel
        );
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0);

        //done
        JFrame frame = new JFrame("Graphing Calculator and Equation Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(splitPane);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        JTextArea instructions = new JTextArea(
    "Instructions:\n" +
    "- Enter math expressions like sin(x), x^2, etc.\n" +
    "- Tick derivative checkbox to plot f'(x)\n" +
    "- Enable solvers to find zeroes/saddles\n" +
    "- Use zoom/pan (Ctrl + Scroll/Move) for better view\n" +
    "- Ctrl+Click to reset zoom"
);
instructions.setEditable(false);
instructions.setFont(new Font("Monospaced", Font.PLAIN, 12));
instructions.setBackground(new Color(245, 245, 245));
instructions.setBorder(BorderFactory.createTitledBorder("Help"));


JScrollPane instructionScroll = new JScrollPane(instructions);
instructionScroll.setPreferredSize(new java.awt.Dimension(5, 120)); // Set height as needed

leftPanel.add(instructionScroll, BorderLayout.SOUTH);
leftPanel.revalidate();
leftPanel.repaint();




    }

    public void init_chart()
    {   
        //chumma i created this function because it looked to clumsy to understand otherwise. Ik other parts are clumsier but it is what it is


        //the X and y axis that you see are intialized like dis

        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        xAxis.setRange(-10.0, 10.0);
        yAxis.setRange(-10.0, 10.0);

        //this initializes the plot
        plot = new XYPlot(null, xAxis, yAxis, null);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        //next set of commented lines can be used to bold the boundaries of the graph
        //lines starting from ValueMarker bold the intersection of axes, also added a few imports
        //plot.getDomainAxis().setAxisLineStroke(new BasicStroke(2f));
        //plot.getRangeAxis().setAxisLineStroke(new BasicStroke(2f));
        //plot.getDomainAxis().setTickMarkStroke(new BasicStroke(2f));
        //plot.getRangeAxis().setTickMarkStroke(new BasicStroke(2f));
        ValueMarker verticalLine = new ValueMarker(0.0);
        verticalLine.setPaint(Color.BLACK);
        verticalLine.setStroke(new BasicStroke(2.5f));
        plot.addDomainMarker(verticalLine);
        ValueMarker horizontalLine = new ValueMarker(0.0);
        horizontalLine.setPaint(Color.BLACK);
        horizontalLine.setStroke(new BasicStroke(2.5f));
        plot.addRangeMarker(horizontalLine);

        //Wow
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        //wow
        chart = new JFreeChart("Graphing Calculator", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

        //dont know what exactly it does but it should make the curve smoother
        chart.setAntiAlias(true); // Enables chart anti-aliasing
        chart.setTextAntiAlias(true); 

        //rest self explanatory.
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);   
        chartPanel.setMouseZoomable(true);       
        chartPanel.setDomainZoomable(true);      
        chartPanel.setRangeZoomable(true);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setInitialDelay(0);
    }
    
}
