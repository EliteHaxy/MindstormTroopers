package org.firstinspires.ftc.teamcode.opmode.tele;

import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.A;
import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.B;
import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.DPAD_DOWN;
import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.DPAD_RIGHT;
import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.DPAD_UP;
import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.LEFT_BUMPER;
import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.RIGHT_BUMPER;
import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.X;
import static com.arcrobotics.ftclib.gamepad.GamepadKeys.Button.Y;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.roadrunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.util.PIDFController;
import org.firstinspires.ftc.teamcode.util.PoseStorage;
import org.firstinspires.ftc.teamcode.util.TriggerGamepadEx;


@TeleOp(name = "W TELE")
public class tele extends LinearOpMode {
    public DcMotorEx intake, leftSlideMotor, rightSlideMotor;
    public Servo lS, rS;
    private double loopTime;
    private int target = 0;
    private boolean slowMode = false;

    GamepadEx driver1 = new GamepadEx(gamepad1);
    GamepadEx driver2 = new GamepadEx(gamepad2);
    TriggerGamepadEx t1 = new TriggerGamepadEx(gamepad1, driver1);

    @Override
    public void runOpMode() {
//        PhotonCore.CONTROL_HUB.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
//        PhotonCore.EXPANSION_HUB.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
//        PhotonCore.enable();
        SampleMecanumDrive drive = new SampleMecanumDrive(true, hardwareMap);
        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        drive.setPoseEstimate(PoseStorage.currentPose);

        intake = hardwareMap.get(DcMotorEx.class, "intake");
        leftSlideMotor = hardwareMap.get(DcMotorEx.class, "leftSlide");
        rightSlideMotor = hardwareMap.get(DcMotorEx.class, "rightSlide");
        lS = hardwareMap.get(Servo.class, "leftServo");
        rS = hardwareMap.get(Servo.class, "rightServo");

        lS.setDirection(Servo.Direction.FORWARD);
        rS.setDirection(Servo.Direction.REVERSE);
        //set modes and reset encoders here
        leftSlideMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        leftSlideMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        leftSlideMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        rightSlideMotor.setDirection(DcMotorEx.Direction.REVERSE);
        rightSlideMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        rightSlideMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightSlideMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        // lift on init
        lS.setPosition(0.455);
        rS.setPosition(0.085);
        waitForStart();

        if (isStopRequested()) return;
        while (opModeIsActive() && !isStopRequested()) {
            drive.update();

            Pose2d poseEstimate = drive.getPoseEstimate();
            leftSlideMotor.setPower(PIDFController.returnPower(leftSlideMotor.getCurrentPosition(), target));
            rightSlideMotor.setPower(PIDFController.returnPower(leftSlideMotor.getCurrentPosition(), target));

            double loop = System.nanoTime();
            telemetry.addData("hz ", 1000000000 / (loop - loopTime));
            loopTime = loop;
            telemetry.update();
            Vector2d input = new Vector2d(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x
            ).rotated(-poseEstimate.getHeading());

            if (slowMode) {
                drive.setWeightedSlowDrivePower(
                        new Pose2d(
                                input.getX(),
                                input.getY(),
                                -gamepad1.right_stick_x
                        ),
                        3
                );
            } else {
                drive.setWeightedDrivePower(
                        new Pose2d(
                                input.getX(),
                                input.getY(),
                                -gamepad1.right_stick_x
                        )
                );
            }

            drive.update();

            // Slow mode
            if (t1.isDown(GamepadKeys.Trigger.LEFT_TRIGGER)) {
                slowMode = true;
            } else if (t1.wasJustReleased(GamepadKeys.Trigger.LEFT_TRIGGER)) {
                slowMode = false;
            }

            // intake
            if (driver1.isDown(RIGHT_BUMPER)) { //push
                intake.setDirection(DcMotorEx.Direction.FORWARD);
                intake.setPower(1);
            } else if (driver1.wasJustReleased(RIGHT_BUMPER)) {
                intake.setPower(0);
            }
            if (driver1.isDown(LEFT_BUMPER)) { //grab
                intake.setDirection(DcMotorEx.Direction.REVERSE);
                intake.setPower(1);
            } else if (driver1.wasJustReleased(RIGHT_BUMPER)) {
                intake.setPower(0);
            }

            //servos
            if (driver1.wasJustPressed(Y) || driver2.wasJustPressed(Y)) { // lift
                lS.setPosition(0.455);
                rS.setPosition(0.085);
            }
            if (driver1.wasJustPressed(A) || driver2.wasJustPressed(A)) { //pickup Pixel
                lS.setPosition(0.5);
                rS.setPosition(0.125);
            }
            if (driver1.wasJustPressed(X)) { // drop left pixel
                rS.setPosition(0.53);
            }
            if (driver1.wasJustPressed(B)) { // drop right pixel
                lS.setPosition(0.9);
            }

            //slides
            if (driver1.wasJustPressed(DPAD_UP)) {
                target = 1165;
            }
            if (driver1.wasJustPressed(DPAD_RIGHT)) {
                target = 500;
            }
            if (driver1.wasJustPressed(DPAD_DOWN)) {
                target = 0;
            }


        }
    }
}