package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Regional driver-controlled op mode. All controls are on gamepad 1:
 *   Left stick  - drive forward/backward, strafe side to side, and any angle in between
 *   Right stick - turn (left/right)
 *   A           - toggle intake in
 *   B           - toggle intake out
 *   X           - toggle shoot motor on/off (velocity controlled)
 *   Y           - toggle gate open/closed
 *
 * The turret servo is commanded once when the match starts and never moves after that.
 * Servos are not commanded during init, so the robot stays still until PLAY is pressed.
 */
@TeleOp(name = "Regional TeleOp", group = "Competition")
public class RegionalTeleOp extends OpMode {

    // These must match the names in the active robot configuration.
    private static final String FRONT_LEFT_NAME = "frontLeft";
    private static final String FRONT_RIGHT_NAME = "frontRight";
    private static final String BACK_LEFT_NAME = "backLeft";
    private static final String BACK_RIGHT_NAME = "backRight";
    private static final String INTAKE_NAME = "intake";
    private static final String SHOOT_NAME = "shoot";
    private static final String GATE_NAME = "servo";
    private static final String TURRET_NAME = "turret";

    private static final double INTAKE_POWER = 0.78;

    // Gate positions measured with the Motor Toggle Test op mode.
    private static final double GATE_CLOSED = 0.21;
    private static final double GATE_OPEN = 0.13;

    // TODO: set this to the position that points the turret where you want it.
    private static final double TURRET_POSITION = 0.625;

    // Shoot motor speed in encoder ticks per second.
    // TODO: tune on the robot. These assume a 6000 RPM motor with 28 ticks per
    // revolution (2800 ticks/sec at full speed). F is about 32767 / max ticks per sec.
    private static final double SHOOT_VELOCITY = 1390;
    private static final double SHOOT_P = 10.0;
    private static final double SHOOT_I = 3.0;
    private static final double SHOOT_D = 0.0;
    private static final double SHOOT_F = 11.7;

    private enum IntakeState { OFF, IN, OUT }

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private DcMotor intake;
    private DcMotorEx shoot;
    private Servo gate;
    private Servo turret;

    private IntakeState intakeState = IntakeState.OFF;
    private boolean shootOn = false;
    private boolean gateOpen = false;

    @Override
    public void init() {
        frontLeft = hardwareMap.get(DcMotor.class, FRONT_LEFT_NAME);
        frontRight = hardwareMap.get(DcMotor.class, FRONT_RIGHT_NAME);
        backLeft = hardwareMap.get(DcMotor.class, BACK_LEFT_NAME);
        backRight = hardwareMap.get(DcMotor.class, BACK_RIGHT_NAME);

        // The left side is mounted mirrored, so it runs reversed to drive forward.
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        for (DcMotor motor : new DcMotor[] {frontLeft, frontRight, backLeft, backRight}) {
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        intake = hardwareMap.get(DcMotor.class, INTAKE_NAME);
        intake.setDirection(DcMotorSimple.Direction.FORWARD);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // DcMotorEx gives access to setVelocity() and the built-in velocity PIDF loop.
        // FLOAT lets the flywheel coast down instead of braking hard.
        shoot = hardwareMap.get(DcMotorEx.class, SHOOT_NAME);
        shoot.setDirection(DcMotorSimple.Direction.FORWARD);
        shoot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shoot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shoot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shoot.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(300, 0, 5, 10));
        gate = hardwareMap.get(Servo.class, GATE_NAME);
        turret = hardwareMap.get(Servo.class, TURRET_NAME);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    @Override
    public void start() {
        // The only turret command in this op mode; the servo holds this position.
        turret.setPosition(TURRET_POSITION);
        gate.setPosition(GATE_CLOSED);

        // Discard presses from before PLAY (such as Start + A to assign the gamepad)
        // so nothing switches on by itself on the first loop.
        gamepad1.aWasPressed();
        gamepad1.bWasPressed();
        gamepad1.xWasPressed();
        gamepad1.yWasPressed();
    }

    @Override
    public void loop() {
        drive();
        updateIntake();
        updateShooter();
        updateGate();

        telemetry.addData("Intake", intakeState);
        telemetry.addData("Shooter", shootOn ? "ON" : "OFF");
        telemetry.addData("Shooter Target", "%.0f ticks/sec", shootOn ? SHOOT_VELOCITY : 0.0);
        telemetry.addData("Shooter Velocity", "%.0f ticks/sec", shoot.getVelocity());
        telemetry.addData("Gate", gateOpen ? "OPEN" : "CLOSED");
        telemetry.addData("Turret", "%.2f", turret.getPosition());
        telemetry.update();
    }

    @Override
    public void stop() {
        frontLeft.setPower(0.0);
        frontRight.setPower(0.0);
        backLeft.setPower(0.0);
        backRight.setPower(0.0);
        intake.setPower(0.0);
        shoot.setPower(0.0);
    }

    private void drive() {
        double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
        double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
        double rx = gamepad1.right_stick_x;


        // Scale everything down together so a combined move keeps its direction
        // instead of clipping at full power.

        //frontRight.setPower(1);
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        frontLeft.setPower(frontLeftPower);
        backLeft.setPower(backLeftPower);
        frontRight.setPower(frontRightPower);
        backRight.setPower(backRightPower);
    }

    private void updateIntake() {
        // Pressing the active direction again stops the intake. Pressing the other
        // direction switches straight to it.
        if (gamepad1.aWasPressed()) {
            intakeState = intakeState == IntakeState.IN ? IntakeState.OFF : IntakeState.IN;
        }
        if (gamepad1.bWasPressed()) {
            intakeState = intakeState == IntakeState.OUT ? IntakeState.OFF : IntakeState.OUT;
        }

        switch (intakeState) {
            case IN:
                intake.setPower(INTAKE_POWER);
                break;
            case OUT:
                intake.setPower(-INTAKE_POWER);
                break;
            default:
                intake.setPower(0.0);
                break;
        }
    }

    private void updateShooter() {
        if (gamepad1.xWasPressed()) {
            shootOn = !shootOn;
            if (shootOn) {
                shoot.setVelocity(SHOOT_VELOCITY);
            } else {
                shoot.setPower(0.0);
            }
        }
    }

    private void updateGate() {
        if (gamepad1.yWasPressed()) {
            gateOpen = !gateOpen;
            gate.setPosition(gateOpen ? GATE_OPEN : GATE_CLOSED);
        }
    }
}
