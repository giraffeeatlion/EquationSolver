package Classes;


public class Solver {
    //newton-raphson method is used in this to get da values fas fas. 
    public static double solve(FunctionExpression f, FunctionExpression df, double xMin,double xMax)
    {   
        int maxSteps = 30;
        double x = xMin+(xMax-xMin)/2;
        for(int i = 0; i < maxSteps;i++)
        {
            double y = f.evaluate(x);
            //System.out.println(y);
            if(y == 0)
            {   
                
                break;
            }
            double y_prime = df.evaluate(x);
            x = x - y/y_prime;
            
            if (Double.isNaN(x) || Double.isInfinite(x)) break;
        }
        return x;
    }
    //gotta code dis for finding intersections between two functions
    
    public static double intersectionSolver(FunctionExpression f, FunctionExpression g, double xMin, double xMax) {
        // Build h(x) = f(x) - g(x)
        FunctionExpression h = new FunctionExpression("(" + f.getExpressionString() + ") - (" + g.getExpressionString() + ")", false);
    
        
        String dfStr = FunctionExpression.derivative(f.getExpressionString());
        String dgStr = FunctionExpression.derivative(g.getExpressionString());
    
        if (dfStr == null || dgStr == null) {
            throw new IllegalArgumentException("Couldn't compute derivatives.");
        }
    
        FunctionExpression dh = new FunctionExpression("(" + dfStr + ") - (" + dgStr + ")", false);
    
        return solve(h, dh, xMin, xMax);
    }
    
}
