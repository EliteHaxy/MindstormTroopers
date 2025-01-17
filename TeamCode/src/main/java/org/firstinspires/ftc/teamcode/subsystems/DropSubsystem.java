package org.firstinspires.ftc.teamcode.subsystems;

import static org.firstinspires.ftc.teamcode.tuning.PIDF_Slide.d;
import static org.firstinspires.ftc.teamcode.tuning.PIDF_Slide.f;
import static org.firstinspires.ftc.teamcode.tuning.PIDF_Slide.i;
import static org.firstinspires.ftc.teamcode.tuning.PIDF_Slide.p;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

//import org.firstinspires.ftc.teamcode.util.ServoLocation;

public class DropSubsystem extends SubsystemBase {
    private final DcMotorEx leftSlide;
    private final DcMotorEx rightSlide;
    private final Servo tray;
    private final Servo leftServo;
    private final Servo rightServo;
    private final double liftedTrayPos = 0.301, leftParallel = 0.333, rightParallel = 0;
    private double slidePower = 0;
    private int target = 0;
    private PIDController controller;
    private boolean runPID = true;

//    private final RunMotionProfile profile = new RunMotionProfile(
//            60000, 70000, 80000,
//            0.1, 1, 0, 0.2, 1
//    ); // todo

    public DropSubsystem(DcMotorEx leftSlide, DcMotorEx rightSlide, Servo leftServo, Servo rightServo, Servo tray) {
        runPID = true;
        controller = new PIDController(p, i, d);
        controller.setIntegrationBounds(0.05, 0.25);
        this.leftSlide = leftSlide;
        this.rightSlide = rightSlide;
        this.tray = tray;
        this.leftServo = leftServo;
        this.rightServo = rightServo;
        target = 0; // todo: for mp, set target/goal to 0 at start
    }

    // PIDF Loop
    @Override
    public void periodic() { // Runs in a loop while op mode is active (in the run method of scheduler class)
        if (runPID) {
            slidePower = returnPower(leftSlide.getCurrentPosition(), target);

            leftSlide.setPower(slidePower);
            rightSlide.setPower(slidePower);
            super.periodic();
        } else {
            super.periodic();
        }
    }

    // PID
    public void turnOffPID() {
        runPID = false;
    }

    public void turnOnPID() {
        runPID = true;
    }

    // Hang
    public void goToHang() {
        if (runPID) { this.target = 769; }
    }

    public void hang() {
        if (!runPID) {
            leftSlide.setPower(-0.569); // we don't know directions
            rightSlide.setPower(-0.569);
        }
    }

    // Servos
    public void goParallel() {
        leftServo.setPosition(leftParallel);
        rightServo.setPosition(rightParallel);
    }

    public void liftTray() {
        leftServo.setPosition(leftParallel);
        rightServo.setPosition(rightParallel);
        tray.setPosition(liftedTrayPos);
//        ServoLocation.setServoLocation(ServoLocation.ServoLocationState.LIFTED);
    }

    public void semiLiftTrayForDrop() {
        leftServo.setPosition(leftParallel);
        rightServo.setPosition(rightParallel);
        tray.setPosition(0.23);
    }

    public void dropPixel() {
        leftServo.setPosition(0.7);
        rightServo.setPosition(0.321);
        tray.setPosition(liftedTrayPos);
//        ServoLocation.setServoLocation(ServoLocation.ServoLocationState.DROP);
    }

    public void pickupPixel() {
        leftServo.setPosition(0.5);
        rightServo.setPosition(0.2);
        tray.setPosition(0.205);
//        ServoLocation.setServoLocation(ServoLocation.ServoLocationState.PICKUP);
    }

    public void setupTrayForSlide() {
        leftServo.setPosition(0);
        rightServo.setPosition(0);
        tray.setPosition(0.07);
    }

    public void setForFirstPixel() {
        leftServo.setPosition(0);
        rightServo.setPosition(0);
        tray.setPosition(0.252);
    }

    public void setForSecondPixel() {
        leftServo.setPosition(0);
        rightServo.setPosition(0);
        tray.setPosition(0.218);
    }

    // Slide
    public void slideHigh() {
        if (runPID) { this.target = 980; }
    }

    public void slideMed() {
        if (runPID) { this.target = 900; }
    }

    public void slideLow() {
        if (runPID) { this.target = 750; }
    }

    public void slideSmall() {
        if (runPID) { this.target = 620; }
    }

    public void slidePoint() {
        if (runPID) { this.target = 200; }
    }

    public void slideIdle() {
        if (runPID) { this.target = 0; }
    }

    public void slideGoTo(int target) {
        if (runPID) { this.target = target; }
    }

    public int getPosition() {
        return leftSlide.getCurrentPosition();
    }

    public double returnPower(int pos, int target) {
        if (runPID) {
            double pid = controller.calculate(pos, target);
            return pid + f;
        } else {
            return 0.0;
        }
    }

//    public double getError() {
//        return target - getPosition();
//    }
//
//    public boolean isTimeDone() {
//        return profile.getProfileDuration() < profile.getCurrentTime();
//    }
//
//    public boolean isPositionDone() {
//        return Math.abs(getError()) < 10;
//    }
//
//    public void setMotionConstraints(double maxVel, double maxAccel, double maxJerk) {
//        profile.setMotionConstraints(maxVel, maxAccel, maxJerk);
//    }
//
//    public void setPIDFcoeffs(double Kp, double Ki, double Kd, double Kf, double limit) {
//        profile.setPIDFcoeffs(Kp, Ki, Kd, Kf, limit);
//    }
//
//    public double getMotionTarget() {
//        return -profile.getMotionTarget();
//    }
//
//    public double getMotionTime() {
//        return profile.getCurrentTime();
//    }
}