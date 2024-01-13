package org.firstinspires.ftc.teamcode.opmode.auto.red;

import static org.firstinspires.ftc.teamcode.util.StartPoses.leftRed;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.RunCommand;
import com.arcrobotics.ftclib.command.SelectCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
//import com.outoftheboxrobotics.photoncore.Photon;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.teamcode.commands.DropSlide;
import org.firstinspires.ftc.teamcode.commands.LiftSlideSmall;
import org.firstinspires.ftc.teamcode.commands.PushOnePixel;
import org.firstinspires.ftc.teamcode.opmode.BaseOpMode;
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.roadrunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.roadrunner.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.subsystems.RRDriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TensorflowSubsystem;
import org.firstinspires.ftc.teamcode.util.DelayedCommand;

import java.util.HashMap;

//@Photon
@Autonomous
public class LeftRed3Pixel extends BaseOpMode {
    private PropLocations location;
    private RRDriveSubsystem rrDrive;
    private double loopTime = 0.0;
//    private AprilTagSubsystem aprilTagSubsystem;

    @Override
    public void initialize() {
        CommandScheduler.getInstance().reset();
        super.initialize();

        rrDrive = new RRDriveSubsystem(new SampleMecanumDrive(hardwareMap));
        rrDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

//        aprilTagSubsystem = new AprilTagSubsystem(hardwareMap);
        TensorflowSubsystem tensorflow = new TensorflowSubsystem(hardwareMap, "Webcam 1",
                "oldred.tflite", REDLABEL);

        tensorflow.setMinConfidence(0.69);

        register(drop); // register so it runs the periodics in a loop while opmode is active

        intake.setDefaultCommand(new RunCommand(intake::stop, intake));

        // Drop ground pixel
        TrajectorySequence dropLeft = rrDrive.trajectorySequenceBuilder(leftRed)
                .lineToSplineHeading(new Pose2d(-46, -39, Math.toRadians(90)))
                .back(4)
                .build();
        TrajectorySequence dropMiddle = rrDrive.trajectorySequenceBuilder(leftRed)
                .lineToSplineHeading(new Pose2d(-43, -26, Math.toRadians(0)))
                .build();

        TrajectorySequence dropRight = rrDrive.trajectorySequenceBuilder(leftRed)
                .strafeRight(6)
                .lineToSplineHeading(new Pose2d(-32, -34, Math.toRadians(0)))
                .lineTo(new Vector2d(-46, -34))
                .build();

        // Go to stacks
        TrajectorySequence goToStacksLeft = rrDrive.trajectorySequenceBuilder(dropLeft.end())
                .strafeRight(14)
                .lineToConstantHeading(new Vector2d(-36, -10))
                .lineToSplineHeading(new Pose2d(-63, -12, Math.toRadians(180)))
                .build();

        TrajectorySequence goToStacksMiddle = rrDrive.trajectorySequenceBuilder(dropMiddle.end())
                .lineToLinearHeading(new Pose2d(-48, -12, Math.toRadians(180)),
                        SampleMecanumDrive.getVelocityConstraint(35, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(35))
                .build();

        TrajectorySequence goToStacksRight = rrDrive.trajectorySequenceBuilder(dropRight.end())
                .lineToLinearHeading(new Pose2d(-48, -12, Math.toRadians(180)),
                        SampleMecanumDrive.getVelocityConstraint(35, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(35))
                .build();

        // Cross Truss
        TrajectorySequence crossTrussLeft = rrDrive.trajectorySequenceBuilder(goToStacksLeft.end())
                .lineToSplineHeading(new Pose2d(35, 0, Math.toRadians(180)),
                        SampleMecanumDrive.getVelocityConstraint(25, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(25))
                .build();

        TrajectorySequence crossTrussMiddle = rrDrive.trajectorySequenceBuilder(goToStacksMiddle.end())
                .lineToSplineHeading(new Pose2d(35, -0, Math.toRadians(180)),
                        SampleMecanumDrive.getVelocityConstraint(25, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(25))
                .build();

        TrajectorySequence crossTrustRight = rrDrive.trajectorySequenceBuilder(goToStacksRight.end())
                .lineToSplineHeading(new Pose2d(35, -0, Math.toRadians(180)),
                        SampleMecanumDrive.getVelocityConstraint(25, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(25))
                .build();

        // Drop to backdrop
        TrajectorySequence dropToBackdropLeft = rrDrive.trajectorySequenceBuilder(crossTrussLeft.end())
                .lineToConstantHeading(new Vector2d(54, -28),
                        SampleMecanumDrive.getVelocityConstraint(23, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(23))
                .build();
        TrajectorySequence dropToBackdropMiddle = rrDrive.trajectorySequenceBuilder(crossTrussMiddle.end())
                .lineToConstantHeading(new Vector2d(54, -34),
                        SampleMecanumDrive.getVelocityConstraint(23, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(23))
                .build();
        TrajectorySequence dropToBackdropRight = rrDrive.trajectorySequenceBuilder(crossTrustRight.end())
                .lineToConstantHeading(new Vector2d(54, -39.5),
                        SampleMecanumDrive.getVelocityConstraint(23, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(23))
                .build();
        // Slows
        TrajectorySequence dropToBackdropLeftSlow = rrDrive.trajectorySequenceBuilder(dropToBackdropRight.end())
                .strafeRight(4)
                .lineToConstantHeading(new Vector2d(54, -28),
                        SampleMecanumDrive.getVelocityConstraint(14, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(14))
                .build();
        TrajectorySequence dropToBackdropMiddleSlow = rrDrive.trajectorySequenceBuilder(dropToBackdropLeft.end())
                .strafeRight(4)
                .lineToConstantHeading(new Vector2d(54, -34),
                        SampleMecanumDrive.getVelocityConstraint(14, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(14))
                .build();
        TrajectorySequence dropToBackdropRightSlow = rrDrive.trajectorySequenceBuilder(dropToBackdropLeft.end())
                .strafeRight(4)
                .lineToConstantHeading(new Vector2d(54, -39.5),
                        SampleMecanumDrive.getVelocityConstraint(14, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(14))
                .build();


        // Park
        TrajectorySequence park = rrDrive.trajectorySequenceBuilder(dropToBackdropLeft.end())
                .lineToLinearHeading(new Pose2d(47, -15, Math.toRadians(180)))
                .build();

        rrDrive.setPoseEstimate(leftRed);

        // On init --> do nothing

        while (opModeInInit()) {
            Recognition bestDetection = tensorflow.getBestDetection();
            location = PropLocations.LEFT;

            if (bestDetection != null) {
                double x = (bestDetection.getLeft() + bestDetection.getRight()) / 2;
                if (x > 365) {
                    location = PropLocations.RIGHT;
                } else if (x > 160 && x < 365) {
                    location = PropLocations.MIDDLE;
                }
            }

//            telemetry.addData("FPS", tensorflow.portal.getFps()); // remove tele except loco
            telemetry.addData("Current Location", location.toString());
//            telemetry.addData("Confidence", String.format("%.2f%%", bestDetection != null ? bestDetection.getConfidence() * 100 : 0));
            telemetry.update();
        }

        schedule(new SequentialCommandGroup(
                new SelectCommand(
                        new HashMap<Object, Command>() {{
                            put(PropLocations.LEFT, new SequentialCommandGroup(
                                    new ParallelCommandGroup(
                                            new InstantCommand(tensorflow::shutdown, tensorflow),
                                            new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropLeft)),
                                            new DelayedCommand(new InstantCommand(drop::pickupPixel, drop), 1000),
                                            new DelayedCommand(new RunCommand(intake::push, intake).raceWith(new WaitCommand(500)), 3000).andThen(new InstantCommand(intake::stop, intake))
                                    )
                            ));
                            put(PropLocations.MIDDLE, new SequentialCommandGroup(
                                    new ParallelCommandGroup(
                                            new InstantCommand(tensorflow::shutdown, tensorflow),
                                            new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropMiddle)),
                                            new DelayedCommand(new InstantCommand(drop::pickupPixel, drop), 1000),
                                            new DelayedCommand(new RunCommand(intake::push, intake).raceWith(new WaitCommand(500)), 3400).andThen(new InstantCommand(intake::stop, intake))
                                    )
                            ));
                            put(PropLocations.RIGHT, new SequentialCommandGroup(
                                    new ParallelCommandGroup(
                                            new InstantCommand(tensorflow::shutdown, tensorflow),
                                            new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropRight)),
                                            new DelayedCommand(new InstantCommand(drop::pickupPixel, drop), 1000),
                                            new DelayedCommand(new RunCommand(intake::push, intake).raceWith(new WaitCommand(600)), 4200).andThen(new InstantCommand(intake::stop, intake))
                                    )
                            ));
                        }},
                        () -> location
                )
//                new ParallelCommandGroup(
//                        new RunCommand(intake::push, intake).raceWith(new WaitCommand(400)).andThen(new InstantCommand(intake::stop, intake)),
//                        new SelectCommand(
//                                new HashMap<Object, Command>() {{
//                                    put(PropLocations.LEFT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(goToStacksLeft)));
//                                    put(PropLocations.MIDDLE, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(goToStacksMiddle)));
//                                    put(PropLocations.RIGHT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(goToStacksRight)));
//                                }},
//                                () -> location
//                        ),
//                        new SequentialCommandGroup(
//                                new DelayedCommand(new InstantCommand(drop::liftForFirstPixel, drop),650),
//                                new DelayedCommand(new InstantCommand(drop::dropForFirstPixel, drop), 1300)
//                        )
//                ),
//                new WaitUntilCommand(() -> !rrDrive.isBusy()),
//                new RunCommand(intake::grab, intake).raceWith(new WaitCommand(750)).andThen(new InstantCommand(intake::stop, intake)),
//                new ParallelCommandGroup(
//                        new SelectCommand(
//                                new HashMap<Object, Command>() {{
//                                    put(PropLocations.LEFT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(crossTrussLeft)));
//                                    put(PropLocations.MIDDLE, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(crossTrussMiddle)));
//                                    put(PropLocations.RIGHT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(crossTrustRight)));
//                                }},
//                                () -> location
//                        ),
//                        new DelayedCommand(new InstantCommand(drop::liftTray), 1000)
//                ),
//                new WaitUntilCommand(() -> !rrDrive.isBusy()),
//                new SelectCommand(
//                        new HashMap<Object, Command>() {{
//                            put(PropLocations.LEFT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropRight)));
//                            put(PropLocations.MIDDLE, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropMiddle)));
//                            put(PropLocations.RIGHT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropRight)));
//                        }},
//                        () -> location
//                ),
//                new WaitUntilCommand(() -> !rrDrive.isBusy()),
//                new LiftSlideSmall(drop),
//                new WaitUntilCommand(() -> drop.getPosition() <= 670 && drop.getPosition() >= 635),
//                new InstantCommand(drop::dropPixel, drop),
//                new DelayedCommand(new PushOnePixel(intake), 450), // drop first pixel
//                new ParallelCommandGroup(
//                        new SelectCommand(
//                                new HashMap<Object, Command>() {{
//                                    put(PropLocations.LEFT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropLeftSlow)));
//                                    put(PropLocations.MIDDLE, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropMiddle)));
//                                    put(PropLocations.RIGHT, new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(dropToBackdropRight)));
//                                }},
//                                () -> location
//                        ),
//                        new RunCommand(intake::grab, intake).raceWith(new WaitCommand(1350)).andThen(new InstantCommand(intake::stop, intake))
//                ),
//                new WaitUntilCommand(() -> !rrDrive.isBusy()),
//                new InstantCommand(drop::dropPixel, drop),
//                new DelayedCommand(new PushOnePixel(intake), 450),
//                new DropSlide(drop),
//                new WaitUntilCommand(() -> (drop.getPosition() <= 20 && drop.getPosition() >= -10)),
//                new DelayedCommand(new InstantCommand(drop::liftTray), 150),
//                new InstantCommand(() -> rrDrive.followTrajectorySequenceAsync(park))
        ));
    }

    @Override
    public void run() {
        super.run(); // since we are overriding in opmodes, this will actually run it
        rrDrive.update();
        telemetry.addData("Drive Pose", rrDrive.getPoseEstimate().toString());

//        if (aprilTagSubsystem.getDetections().size() > 0) {
//            AprilTagDetection currentDetection = aprilTagSubsystem.getDetections().get(0);
//
//            if (currentDetection.metadata != null) { // if a tag is detected
//                double poseVelo = rrDrive.getPoseVelocity().vec().norm();
//
//                if (poseVelo <= 0.25) { // and if robot velocity is <= 0.25 inches
//                    Vector2d localizedAprilTagVector = aprilTagSubsystem.getFCPosition(currentDetection, Math.toDegrees(rrDrive.getPoseEstimate().getHeading()));
//
//                    rrDrive.setPoseEstimate(localizedAprilTagVector.getX(), localizedAprilTagVector.getY(), rrDrive.getPoseEstimate().getHeading());
//                    telemetry.addData("April Tag Pose", localizedAprilTagVector + ", " + Math.toDegrees(rrDrive.getPoseEstimate().getHeading()));
//                } else {
//                    telemetry.addData("April Tag Pose", "Robot velocity too high");
//                }
//            }
//        } else {
//            telemetry.addData("April Tag Pose", "Tag not detected");
//        }

        telemetry.addData("slide pos", drop.getPosition());
        double loop = System.nanoTime();
        telemetry.addData("hz ", 1000000000 / (loop - loopTime));
        loopTime = loop;
        telemetry.update();
        bulkRead.read();
    }

    private enum PropLocations {
        LEFT,
        MIDDLE,
        RIGHT
    }
}