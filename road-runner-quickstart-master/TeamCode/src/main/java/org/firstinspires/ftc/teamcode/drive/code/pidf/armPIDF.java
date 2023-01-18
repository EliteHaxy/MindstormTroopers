package org.firstinspires.ftc.teamcode.drive.code.pidf;

import com.arcrobotics.ftclib.controller.PIDController;

public class armPIDF {
    public static double returnPower(double state, double target) {
        double p = 0.0055, i = 0.045, d = 0.00035, f = 0.000000001;
        double divide = 1.1;
        final double ticks_in_degrees = 537.7 / 360.0;

        PIDController controller = new PIDController(p, i, d);
        controller.setPID(p, i, d);

        double pid = controller.calculate(state, target);
        double ff = Math.cos(Math.toRadians(target / ticks_in_degrees)) * f;
        double power = pid + ff;
        power /= divide;
        return power;
    }
}
