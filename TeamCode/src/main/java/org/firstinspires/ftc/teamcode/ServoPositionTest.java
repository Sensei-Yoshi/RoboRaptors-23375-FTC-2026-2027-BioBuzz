package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Finds servo positions using gamepad 1. The servo starts at 0.
 *   D-pad right - raise the target by one step
 *   D-pad left  - lower the target by one step
 *   Y           - move the servo to the target
 */
@TeleOp(name = "Servo Position Test", group = "Test")
public class ServoPositionTest extends OpMode {

    // Must match the servo's name in the active robot configuration.
    private static final String SERVO_NAME = "servo";
    private static final double SERVO_STEP = 0.01;

    private Servo servo;
    private double servoTarget = 0.6;

    @Override
    public void init() {
        servo = hardwareMap.get(Servo.class, SERVO_NAME);
        servo.setPosition(servoTarget);

        telemetry.addData("Status", "Initialized - D-pad to adjust, Y to move");
        telemetry.update();
    }

    @Override
    public void loop() {
        // D-pad only changes the target; the servo doesn't move until Y is pressed.
        if (gamepad1.dpadRightWasPressed()) {
            servoTarget = Math.min(1.0, servoTarget + SERVO_STEP);
        }

        if (gamepad1.dpadLeftWasPressed()) {
            servoTarget = Math.max(0.0, servoTarget - SERVO_STEP);
        }

        if (gamepad1.yWasPressed()) {
            servo.setPosition(servoTarget);
        }

        telemetry.addData("Servo Target", "%.2f", servoTarget);
        telemetry.addData("Servo Position", "%.2f", servo.getPosition());
        telemetry.update();
    }
}
