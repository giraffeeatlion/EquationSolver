package Classes;

import org.matheclipse.core.interfaces.IExpr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.matheclipse.core.eval.ExprEvaluator;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

//This Class contains the data for each function (the ones that we're gonna use for plotting)

public class FunctionExpression { 
    private Expression expression; //This is the core exp4j object that we'll be using
    private String expressionString; //Storing the expressionString
    private Boolean plotDerivative = false; //If we wanna plot dis derivative
    public FunctionExpression intersectExpression;

    //These three are re-initialized only when we click plotAll
    public static List<FunctionExpression> expressions = new ArrayList<>();//Main function 
    public static List<FunctionExpression> derivativeExpressions = new ArrayList<>();//derivative function (needed for finding zeroes and saddles)
    public static List<FunctionExpression> doubleDerExpressions = new ArrayList<>();//double derivative (needed for finding saddles)
    public static List<FunctionExpression> tripleDerExpressions = new ArrayList<>();
    public static List<FunctionExpression> intersectionExpressions = new ArrayList<>();
    public static List<FunctionExpression> intersectionDerExpressions = new ArrayList<>();

    public static FunctionExpression areaFunction;
    //I wanna add tripleDer to be able to plot saddles of derivative function but it might end up becoming too much so i took lite for now.


    //static evaluator of symja. (NOT FOR CALCULATION BUT TO RETURN appropriate string (derivatives/simplification))
    private static ExprEvaluator evaluator = new ExprEvaluator();


    public FunctionExpression(String exprStr, boolean plot) {
        this.plotDerivative = plot;

        // Auto-fix unbalanced parentheses by appending missing ')'
        exprStr = autoFixParentheses(exprStr);
        this.expressionString = exprStr;


        //building function lol
        try {
            ExpressionBuilder builder = new ExpressionBuilder(exprStr).variable("x");
            for (Function f : CustomFunctions.customTrigFunctions) {
                builder.function(f);
            }
            this.expression = builder.build();
        } catch (Exception e) {
            //this kinda wont because when i tried for some inputs like sin (not sin(x)) it just threw exceptions and idk how to handle it yet. didnt put too much thought into it
            JOptionPane.showMessageDialog(
                null,
                "Invalid expression: " + e.getMessage() + "\n\n" + exprStr,
                "Expression Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
       
    public FunctionExpression(String exprStr, boolean plot,FunctionExpression Expression) {
        this.plotDerivative = plot;
        this.intersectExpression = Expression;
        // Auto-fix unbalanced parentheses by appending missing ')'
        exprStr = autoFixParentheses(exprStr);
        this.expressionString = exprStr;


        //building function lol
        try {
            ExpressionBuilder builder = new ExpressionBuilder(exprStr).variable("x");
            for (Function f : CustomFunctions.customTrigFunctions) {
                builder.function(f);
            }
            this.expression = builder.build();
        } catch (Exception e) {
            //this kinda wont because when i tried for some inputs like sin (not sin(x)) it just threw exceptions and idk how to handle it yet. didnt put too much thought into it
            JOptionPane.showMessageDialog(
                null,
                "Invalid expression: " + e.getMessage() + "\n\n" + exprStr,
                "Expression Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }


    public String getExpressionString() {
        return expressionString;
    }

    //used when we add f'(x)
    public boolean toggleDerivative()
    {   
        this.plotDerivative = !this.plotDerivative;
        System.out.println(this.plotDerivative);
        return this.plotDerivative;
    }
    public boolean plotDerivative()
    { 
        return this.plotDerivative;
    }

    //Actual calculation method
     public double evaluate(double x) {
        if (expression == null) {
            throw new IllegalStateException("Expression not initialized due to invalid input.");
        }
        return expression.setVariable("x", x).evaluate();
    }

    //this is to find the derivative string
    //eventually we'll create a function using this string to get the derivative expression
    public static String derivative(String exp) {
        exp = autoFixParentheses(exp);
        String expr = "D(" + exp.trim() + " , x)";  // Symbolic derivative
        try {
            IExpr result = evaluator.evaluate(expr);
            String der = result.toString().toLowerCase();
            System.out.println("Derivative: " + der);

            //some crazy syntax to remove some chuth pakoda things that can't be parsed in exp4j. Plotter handles the piecewiseness so dont worry.
            der = der.replaceAll("piecewise\\s*\\(\\s*\\{\\{[^}]+\\}\\}\\s*,\\s*indeterminate\\s*\\)", "0");
            result = evaluator.evaluate("Simplify(" + der + ")");
            
            der = result.toString().toLowerCase();
            der = der.replaceAll("piecewise\\s*\\(\\s*\\{\\{[^}]+\\}\\}\\s*,\\s*indeterminate\\s*\\)", "0");
            return der;  
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //to append parenthesis if the user forgor
    public static String autoFixParentheses(String expr) {
        int balance = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(') balance++;
            else if (c == ')') balance--;
        }

        if (balance > 0) {
            // Add missing closing parentheses
            return expr + ")".repeat(balance);
        }
        return expr;
    }
}
