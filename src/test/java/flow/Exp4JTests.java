package flow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Exp4JTests {

    @Test
    public void testBasicExpressionEvaluation() {
        Expression expression = new ExpressionBuilder("2 * x + 3")
                .variables("x")
                .build();

        double result = expression.setVariable("x", 5.0)
                .evaluate();

        assertEquals(13.0, result, 1e-6); // Assert with a tolerance
    }

    @Test
    public void testTrigonometricFunctions() {
        Expression expression = new ExpressionBuilder("sin(x) + cos(x)")
                .variables("x")
                .build();

        double result = expression.setVariable("x", Math.PI / 4)
                .evaluate();

        assertEquals(Math.sqrt(2), result, 1e-6); // Assert with a tolerance
    }

    @Test
    public void testExponentialFunction() {
        Expression expression = new ExpressionBuilder("exp(x)")
                .variables("x")
                .build();

        double result = expression.setVariable("x", 1.0)
                .evaluate();

        assertEquals(Math.exp(1), result, 1e-6); // Assert with a tolerance
    }

    @Test
    public void testCustomFunction() {
        Expression expression = new ExpressionBuilder("myFunction(x)")
                .variables("x")
                .functions(new Exp4jCustomFunction("myFunction", x -> Math.sqrt(x)))
                .build();

        double result = expression.setVariable("x", 16.0)
                .evaluate();

        assertEquals(4.0, result, 1e-6); // Assert with a tolerance
    }
}

class Exp4jCustomFunction extends net.objecthunter.exp4j.function.Function {
    private java.util.function.Function<Double, Double> function;

    public Exp4jCustomFunction(String name, java.util.function.Function<Double, Double> function) {
        super(name, 1);
        this.function = function;
    }

    @Override
    public double apply(double... args) {
        return function.apply(args[0]);
    }
}
