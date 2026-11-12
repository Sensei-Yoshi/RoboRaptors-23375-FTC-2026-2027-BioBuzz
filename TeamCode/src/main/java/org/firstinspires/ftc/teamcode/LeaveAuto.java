package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Shoot-then-leave autonomous:
 *   1. Spin the shoot motor up to the same velocity RegionalTeleOp uses.
 *   2. Open the gate and run the intake at INTAKE_POWER for FEED_TIME_SECONDS to feed.
 *   3. Stop the shooter and intake, close the gate.
 *   4. Turn right a little, then drive straight forward to leave.
 */
@Autonomous(name = "LeaveAuto", group = "Competition")
public class LeaveAuto extends LinearOpMode {

    // These must match the names in the active robot configuration.
    private static final String FRONT_LEFT_NAME = "frontLeft";
    private static final String FRONT_RIGHT_NAME = "frontRight";
    private static final String BACK_LEFT_NAME = "backLeft";
    private static final String BACK_RIGHT_NAME = "backRight";
    private static final String INTAKE_NAME = "intake";
    private static final String SHOOT_NAME = "shoot";
    private static final String GATE_NAME = "servo";
    private static final String TURRET_NAME = "turret";

    // Same values as RegionalTeleOp.
    private static final double GATE_CLOSED = 0.21;
    private static final double GATE_OPEN = 0.13;
    private static final double TURRET_POSITION = 0.625;
    private static final double SHOOT_VELOCITY = 1378; // ticks/sec

    // Shooting.
    private static final double INTAKE_POWER = 0.72;
    private static final double FEED_TIME_SECONDS = 5.0;
    // How long to let the flywheel spin up before feeding.
    private static final double SPIN_UP_SECONDS = 3.0;
    // Pause after shooting, before the robot starts moving.
    private static final double PAUSE_AFTER_SHOOT_SECONDS = 3.0;

    // Driving. TODO: tune on the robot.
    private static final double TURN_TIME_SECONDS = 0.35;
    private static final double TURN_POWER = -0.4;
    private static final double DRIVE_TIME_SECONDS = 4.0;
    private static final double DRIVE_POWER = 0.5;

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    @Override
    public void runOpMode() {
        frontLeft = hardwareMap.get(DcMotor.class, FRONT_LEFT_NAME);
        frontRight = hardwareMap.get(DcMotor.class, FRONT_RIGHT_NAME);
        backLeft = hardwareMap.get(DcMotor.class, BACK_LEFT_NAME);
        backRight = hardwareMap.get(DcMotor.class, BACK_RIGHT_NAME);

        // Same directions as RegionalTeleOp so positive power drives forward.
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        for (DcMotor motor : new DcMotor[] {frontLeft, frontRight, backLeft, backRight}) {
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        DcMotor intake = hardwareMap.get(DcMotor.class, INTAKE_NAME);
        intake.setDirection(DcMotorSimple.Direction.FORWARD);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Same shooter setup as RegionalTeleOp.
        DcMotorEx shoot = hardwareMap.get(DcMotorEx.class, SHOOT_NAME);
        shoot.setDirection(DcMotorSimple.Direction.FORWARD);
        shoot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shoot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shoot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shoot.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(300, 0, 5, 10));

        Servo gate = hardwareMap.get(Servo.class, GATE_NAME);
        Servo turret = hardwareMap.get(Servo.class, TURRET_NAME);

        // Close the gate during init so nothing can feed before the shot.
        gate.setPosition(GATE_OPEN);

        telemetry.addData("Status", "Ready - shoot, turn right, drive forward");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        turret.setPosition(TURRET_POSITION);

        // 1. Spin up the shooter.
        shoot.setVelocity(SHOOT_VELOCITY);
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < SPIN_UP_SECONDS) {
            telemetry.addData("Step", "Spinning up %.1f / %.1f s", timer.seconds(), SPIN_UP_SECONDS);
            telemetry.addData("Shooter Velocity", "%.0f / %.0f", shoot.getVelocity(), SHOOT_VELOCITY);
            telemetry.update();
            idle();
        }

        // 2. Open the gate and feed with the intake.
        gate.setPosition(GATE_CLOSED);
        intake.setPower(INTAKE_POWER);
        timer.reset();
        while (opModeIsActive() && timer.seconds() < FEED_TIME_SECONDS) {
            telemetry.addData("Step", "Shooting %.1f / %.1f s", timer.seconds(), FEED_TIME_SECONDS);
            telemetry.addData("Shooter Velocity", "%.0f / %.0f", shoot.getVelocity(), SHOOT_VELOCITY);
            telemetry.update();
            idle();
        }

        // 3. Shut down the shooting mechanism.
        intake.setPower(0.0);
        shoot.setPower(0.0);
        gate.setPosition(GATE_OPEN);

        // Pause before moving.
        timer.reset();
        while (opModeIsActive() && timer.seconds() < PAUSE_AFTER_SHOOT_SECONDS) {
            telemetry.addData("Step", "Pausing %.1f / %.1f s", timer.seconds(), PAUSE_AFTER_SHOOT_SECONDS);
            telemetry.update();
            idle();
        }

        // 4. Turn right a little (same signs as a positive right stick in TeleOp).
        runDrive(TURN_POWER, TURN_POWER, -TURN_POWER, -TURN_POWER, TURN_TIME_SECONDS, "Turning right");

        // 5. Drive straight forward.
        runDrive(DRIVE_POWER, DRIVE_POWER, DRIVE_POWER, DRIVE_POWER, DRIVE_TIME_SECONDS, "Driving forward");

        telemetry.addData("Step", "Done");
        telemetry.update();
    }

    /** Sets the four drive powers for a fixed time, then stops the drive. */
    private void runDrive(double fl, double bl, double fr, double br, double seconds, String label) {
        frontLeft.setPower(fl);
        backLeft.setPower(bl);
        frontRight.setPower(fr);
        backRight.setPower(br);

        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < seconds) {
            telemetry.addData("Step", "%s %.1f / %.1f s", label, timer.seconds(), seconds);
            telemetry.update();
            idle();
        }

        frontLeft.setPower(0.0);
        backLeft.setPower(0.0);
        frontRight.setPower(0.0);
        backRight.setPower(0.0);
    }
}
