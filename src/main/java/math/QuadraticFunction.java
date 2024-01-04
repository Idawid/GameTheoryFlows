package math;

public class QuadraticFunction {
    private double a; // Coefficient for x^2
    private double b; // Coefficient for x
    private double c; // Constant term

    public QuadraticFunction(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public QuadraticFunction derivative() {
        double newA = 0; // No more x^2 term
        double newB = 2 * a;
        double newC = b;

        return new QuadraticFunction(newA, newB, newC);
    }

    public double getValue(double x) {
        return (a * x * x) + (b * x) + c;
    }

    public double getValue(QuadraticFunction u, double x) {
        return getValue(u.getValue(x));
    }

    public double getValue(double x, boolean derivativeFlag) {
        if (derivativeFlag) {
            return derivative().getValue(x);
        } else {
            return getValue(x);
        }
    }

    public double getValue(QuadraticFunction u, double x, boolean derivativeFlag) {
        if (derivativeFlag) {
            double u_x = u.getValue(x);
            double u_x_derivative = u.getValue(x, true);
            return derivative().getValue(u_x) * u_x_derivative;
        } else {
            return getValue(u, x);
        }
    }

    public QuadraticFunction add(QuadraticFunction other) {
        double newA = this.a + other.a;
        double newB = this.b + other.b;
        double newC = this.c + other.c;
        return new QuadraticFunction(newA, newB, newC);
    }
}
