package org.firstinspires.ftc.teamcode.opmode.auto.red;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.RunCommand;
import com.arcrobotics.ftclib.command.SelectCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.teamcode.opmode.BaseOpMode;
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.roadrunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.roadrunner.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.subsystems.TensorflowSubsystem;
import org.firstinspires.ftc.teamcode.util.PoseStorage;
import java.util.HashMap;

@Autonomous
public class RightRed extends BaseOpMode {
    private PropLocations location;

    @Override
    public void initialize() {
        CommandScheduler.getInstance().reset();
        super.initialize();
        TensorflowSubsystem tensorflow = new TensorflowSubsystem(hardwareMap, "Webcam 1",
                "redprop.tflite", LABELS);

        tensorflow.setMinConfidence(0.75);
//        register(drop, intake);

        while (opModeInInit()) {
            Recognition bestDetection = tensorflow.getBestDetection();
            location = PropLocations.MIDDLE;

            if (bestDetection != null) {
                double x = (bestDetection.getLeft() + bestDetection.getRight()) / 2;
                if (x < 150) {
                    location = PropLocations.LEFT;
                } else if (x > 150 && x < 440) {
                    location = PropLocations.MIDDLE;
                } else {
                    location = PropLocations.RIGHT;
                }
            }

//            telemetry.addData("FPS", tensorflow.portal.getFps()); // remove tele except loco
            telemetry.addData("Current Location", location.toString());
//            telemetry.addData("Confidence", String.format("%.2f%%", bestDetection != null ? bestDetection.getConfidence() * 100 : 0));
            telemetry.update();

            // On init
//            drop.liftServo();

        }

        if (opModeInInit()) {
            drop.liftServo(); // should be working in init
        }

//        imu.reset(); todo
        Pose2d rightRedStartPos = new Pose2d(12, -66, Math.toRadians(90));
        rrDrive.setPoseEstimate(rightRedStartPos);

        // Drop ground pixel (todo: wait 0.3 after)
        TrajectorySequence dropLeft = rrDrive.trajectorySequenceBuilder(rightRedStartPos) // what does reversed do
                .splineToSplineHeading(new Pose2d(10.5, -26.5, Math.toRadians(180)), Math.toRadians(90),
                        SampleMecanumDrive.getVelocityConstraint(17, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .build();
        TrajectorySequence dropMiddle = rrDrive.trajectorySequenceBuilder(rightRedStartPos) // what does reversed do
                .lineToSplineHeading(new Pose2d(12, -32, Math.toRadians(90)),
                        SampleMecanumDrive.getVelocityConstraint(17, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .build();
        TrajectorySequence dropRight = rrDrive.trajectorySequenceBuilder(rightRedStartPos) // what does reversed do
                .splineToSplineHeading(new Pose2d(13, -26.5, Math.toRadians(0)), Math.toRadians(90),
                        SampleMecanumDrive.getVelocityConstraint(17, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .build();

        // Move away (not needed for left)
        TrajectorySequence moveAwayMiddle = rrDrive.trajectorySequenceBuilder(dropMiddle.end())
                .lineTo(new Vector2d(12, -43))
                .build();
        TrajectorySequence moveAwayRight = rrDrive.trajectorySequenceBuilder(dropRight.end())
                .back(2)
                .build();

        // Drop to backdrop
        TrajectorySequence dropToBackdropLeft = rrDrive.trajectorySequenceBuilder(dropLeft.end())
                .lineTo(new Vector2d(45, -29))
                .build();
        TrajectorySequence dropToBackdropMiddle = rrDrive.trajectorySequenceBuilder(moveAwayMiddle.end())
                .lineToSplineHeading(new Pose2d(45, -37, Math.toRadians(180)))
                .build();
        TrajectorySequence dropToBackdropRight = rrDrive.trajectorySequenceBuilder(moveAwayMiddle.end())
                .strafeRight(22)
                .lineToSplineHeading(new Pose2d(45, -42, Math.toRadians(180)))
                .build();

        // Park
        TrajectorySequence parkLeft = rrDrive.trajectorySequenceBuilder(dropToBackdropLeft.end())
                .forward(6)
                .strafeLeft(10)
                .splineToLinearHeading(new Pose2d(59, -60, Math.toRadians(180)), Math.toRadians(0))
                .build();
        TrajectorySequence parkMiddle = rrDrive.trajectorySequenceBuilder(dropToBackdropMiddle.end())
                .forward(4)
                .strafeLeft(4)
                .splineToLinearHeading(new Pose2d(59, -60, Math.toRadians(180)), Math.toRadians(0))
                .build();
        TrajectorySequence parkRight = rrDrive.trajectorySequenceBuilder(dropToBackdropRight.end())
                .forward(5)
                .strafeLeft(3)
                .splineToLinearHeading(new Pose2d(59, -60, Math.toRadians(180)), Math.toRadians(0))
                .build();

        waitForStart();

        CommandScheduler.getInstance().schedule(new SequentialCommandGroup(
                // todo: do i need to make all async as im always updating slides? (or only when they lift)?
//                new InstantCommand(tensorflow::shutdown), todo
//                 drop ground pixel
                new SelectCommand(
                        new HashMap<Object, Command>() {{
                            put(PropLocations.LEFT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropLeft)));
                            put(PropLocations.MIDDLE, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropMiddle)));
                            put(PropLocations.RIGHT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropRight)));
                        }},
                        () -> location
                ),
//
//                // reset
//                new InstantCommand(drop::pickupPixel),
//                new RunCommand(intake::pushSlow, intake).raceWith(new WaitCommand(2000)) // run for 1 sec
//                new InstantCommand(drop::liftServo),
//                new WaitCommand(500),
//
//                // move away (not for left)
                new SelectCommand(
                        new HashMap<Object, Command>() {{
                            put(PropLocations.MIDDLE, new InstantCommand(() -> rrDrive.followTrajectorySequence(moveAwayMiddle)));
                            put(PropLocations.RIGHT, new InstantCommand(() -> rrDrive.followTrajectorySequence(moveAwayRight)));
                        }},
                        () -> location
                ),
//
//                // lift servos at right time
//                new InstantCommand(() -> drop.slideGoTo(150)), // lift slides a little bit so servos don't hit
//                new WaitCommand(500),
//                new InstantCommand(drop::pickupPixel),
//                new WaitCommand(500), // wait before driving again
//
//                // go to backdrop and lift slides
                new ParallelCommandGroup(
                        // go to backdrop
                        new SelectCommand(
                                new HashMap<Object, Command>() {{
                                    put(PropLocations.LEFT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropLeft)));
                                    put(PropLocations.MIDDLE, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropMiddle)));
                                    put(PropLocations.RIGHT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropRight)));
                                }},
                                () -> location
                        ),

                        // lift slides
                        new InstantCommand(drop::slideLift)
                )// slides should be up by the time the traj ends
//
//                // drop pixel
//                new ParallelCommandGroup(
//                        new InstantCommand(drop::dropLeftPixel),
//                        new InstantCommand(drop::dropRightPixel)
//                ),
//                new WaitCommand(750),
//                // reset servos and stagger the slide down
//                new InstantCommand(drop::pickupPixel),
//                new WaitCommand(500),
//                new InstantCommand(() -> drop.slideGoTo(500)),
//                new WaitCommand(1000),
//                new InstantCommand(drop::slideIdle),
//                new WaitCommand(750),
//
//                // lift for drive
//                new InstantCommand(drop::liftServo),
//                new WaitCommand(500),
//
//                // park
//                new SelectCommand(
//                        new HashMap<Object, Command>() {{
//                            put(PropLocations.LEFT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(parkLeft)));
//                            put(PropLocations.MIDDLE, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(parkMiddle)));
//                            put(PropLocations.RIGHT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(parkRight)));
//                        }},
//                        () -> location
//                )
        ));

        PoseStorage.currentPose = rrDrive.getPoseEstimate(); //send pose to tele
    }

    @Override
    public void run() {
        super.run();
        drop.periodic(); //todo do we need this for slides?
        rrDrive.update(); // since we are running some async, we gotta update while opmode is active
    }

    private enum PropLocations {
        LEFT,
        MIDDLE,
        RIGHT
    }
}