package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Test op mode for gamepad 1:
 *   B          - toggle motor 1 between full power (1.0) and stopped (0.0)
 *   A          - toggle motor 2 on and off
 *   D-pad up   - raise motor 2 speed by one step
 *   D-pad down - lower motor 2 speed by one step
 *   D-pad right - raise servo target by one step
 *   D-pad left  - lower servo target by one step
 *   Y          - move the servo to the target
 */
@TeleOp(name = "Motor Toggle Test", group = "Test")
public class intaketests extends OpMode {

    // Must match the motor's name in the active robot configuration.
    private static final String MOTOR_NAME = "intake";
    private static final String MOTOR_NAME2 = "shoot";
    private static final double MOTOR_POWER = 1.0;
    private static final double MOTOR2_POWER_STEP = 0.02;
    private static final String SERVO_NAME = "servo";
    private static final double SERVO_STEP = 0.05;

    private DcMotor motor;
    private DcMotor motor2;
    private Servo servo;
    private boolean motorOn = false;
    private boolean motorOn2 = false;
    private double motor2Power = MOTOR_POWER;
    private double servoClose = 0.22;
    private double servoOpen = 0.13;

    //Close: 0.22
    //Open: 0.13

    @Override
    public void init() {
        motor = hardwareMap.get(DcMotor.class, MOTOR_NAME);
        motor2 = hardwareMap.get(DcMotor.class, MOTOR_NAME2);
        motor.setDirection(DcMotorSimple.Direction.FORWARD);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0.0);
        motor2.setDirection(DcMotorSimple.Direction.FORWARD);
        motor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor2.setPower(0.0);
        servo = hardwareMap.get(Servo.class, SERVO_NAME);
        servo.setPosition(servoClose);

        telemetry.addData("Status", "Initialized - press B to toggle " + MOTOR_NAME);
        telemetry.update();
    }

    @Override
    public void loop() {
        // bWasPressed() only returns true on the rising edge, so one press
        // flips the state exactly once no matter how long B is held.
        if (gamepad1.bWasPressed()) {
            motorOn = !motorOn;

        }

        if (gamepad1.aWasPressed()) {
            motorOn2 = !motorOn2;
        }

        // One step per press, clamped to the valid 0.0 - 1.0 range.
        if (gamepad1.dpadUpWasPressed()) {
            motor2Power = Math.min(1.0, motor2Power + MOTOR2_POWER_STEP);
        }

        if (gamepad1.dpadDownWasPressed()) {
            motor2Power = Math.max(-1.0, motor2Power - MOTOR2_POWER_STEP);
        }

        // D-pad only changes the target; the servo doesn't move until Y is pressed.
        if (gamepad1.x) {
            servo.setPosition(servoClose);
        }

        if (gamepad1.yWasPressed()) {
            servo.setPosition(servoOpen);
        }

        motor.setPower(motorOn ? MOTOR_POWER : 0.0);
        motor2.setPower(motorOn2 ? motor2Power : 0.0);

        telemetry.addData("Motor", motorOn ? "ON" : "OFF");
        telemetry.addData("Power", motor.getPower());
        telemetry.addData("Motor 2", motorOn2 ? "ON" : "OFF");
        telemetry.addData("Motor 2 Speed", "%.2f", motor2Power);
        //telemetry.addData("Servo Target", "%.2f", servoTarget);
        telemetry.addData("Servo Position", "%.2f", servo.getPosition());
        telemetry.update();
    }

    @Override
    public void stop() {
        motor.setPower(0.0);
        motor2.setPower(0.0);
    }
}
