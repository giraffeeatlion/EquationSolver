package Classes;

import java.util.*;

import javax.swing.*;

import Control.ControlPanel;
import Control.GUI_init; 

//This is the little function field that pops up on the left when we press add function
//I mean this object contains that Panel but making it an object was better to understand what has to be plotted.
public class FunctionRow {
    private JTextField functionField;
    private JTextField derivativeField;
    private JButton addDerivativeButton;
    private JButton deleteFieldButton;
    private boolean hasDerivative = false;
    private JPanel functionPanel;

    //main reason being dis, we'll know what functions to plot using this and then put those functions in expressions Array
    public static List<FunctionRow> functionRows = new ArrayList<>();


    //self explanatory
    public FunctionRow()
    {
        functionField = new JTextField();
        addDerivativeButton = new JButton("Add f'(x)");
        deleteFieldButton = new JButton("Delete");
    }
    public boolean hasDerivative()
    {
        return hasDerivative;
    
    }
    public String getFunctionText() {
        return functionField.getText();
    }
    public void setFunctionText(String s)
    {
        functionField.setText(s);
    }
    public void setDerivativeText(String s)
    {   
        if(this.hasDerivative())
            derivativeField.setText(s);
    }
    // this is called by ControlPanel if we have to actually add a row and alladat. The logic also is sorta self explanatory
    public JPanel createFunctionRow()
    {   
        functionPanel = new JPanel();
        functionPanel.setLayout(null);
        JLabel label = new JLabel("f(x)");
        label.setBounds(10, 30, 30, 30);
        functionField.setBounds(50, 30, 300, 30);
        addDerivativeButton.setBounds(50, 70, 100, 30);
        deleteFieldButton.setBounds(250,70,100,30);
        functionPanel.add(label);
        functionPanel.add(functionField);
        functionPanel.add(addDerivativeButton);
        functionPanel.add(deleteFieldButton);

        functionRows.add(this);
        deleteFieldButton.addActionListener(e->{
            this.deleteField();
        });
        addDerivativeButton.addActionListener(e->{
            this.addDerivativeField();
        });
        return functionPanel;
    }
    private void addDerivativeField()
    {
        hasDerivative = true;
        derivativeField = new JTextField();

        String derivativeString = FunctionExpression.derivative(getFunctionText());
        JLabel label = new JLabel("f'(x)");

        derivativeField.setText(derivativeString);
        derivativeField.setEditable(false);
        functionPanel.remove(deleteFieldButton);
        functionPanel.remove(addDerivativeButton);
        
        label.setBounds(10, 70, 30, 30);
        derivativeField.setBounds(50, 70, 300, 30);
        deleteFieldButton.setBounds(250,110,100,30);

        functionPanel.add(label);
        functionPanel.add(derivativeField);
        functionPanel.add(deleteFieldButton);

        GUI_init.functionBar.revalidate();
        GUI_init.functionBar.repaint();


    }
    private void deleteField()
    {   
        int ind = functionRows.indexOf(this);
        functionRows.remove(ind);
        GUI_init.functionBar.remove(functionPanel);

        GUI_init.functionBar.revalidate();
        GUI_init.functionBar.repaint();
        ControlPanel.plotFunctions();
    }    

}
