package math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuadraticFunctionTest {
    @Test
    public void testQuadraticFunctionValue() {
        double x = 2.0;

        QuadraticFunction f = new QuadraticFunction(3, -2, 1); // Represents f(x) = 3x^2 - 2x + 1
        QuadraticFunction u = new QuadraticFunction(2, 3, 5); // Represents u(x) = 2x^2 + 3x + 5

        // Calculate f(x)
        double f_result = f.getValue(x);
        assertEquals(9.0, f_result, 0.001);

        // Calculate f'(x)
        double df_result = f.getValue(x, true);
        assertEquals(10.0, df_result, 0.001);

        // Calculate u(x)
        double u_result = u.getValue(x);
        assertEquals(19.0, u_result, 0.001);

        // Calculate u'(x)
        double du_result = u.getValue(x, true);
        assertEquals(11.0, du_result, 0.001);

        // Calculate f(u(x)), u(x) = 19
        double fu_result = f.getValue(u, x);
        assertEquals(1046.0, fu_result, 0.001);

        // Calculate f'(u(x)) using chain rule
        double dfdu_result = f.getValue(u, x, true);
        assertEquals(1232.0, dfdu_result, 0.001);
    }
}
