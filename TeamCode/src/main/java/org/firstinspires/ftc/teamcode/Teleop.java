package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="Mecanum Linear TeleOp", group="Linear Opmode")
public class Teleop extends LinearOpMode {

    @Override
    public void runOpMode() {
        // Declare and initialize our motors from the hardware map
        DcMotor frontLeftMotor  = hardwareMap.get(DcMotor.class, "frontLeft");
        DcMotor backLeftMotor   = hardwareMap.get(DcMotor.class, "backLeft");
        DcMotor frontRightMotor = hardwareMap.get(DcMotor.class, "frontRight");
        DcMotor backRightMotor  = hardwareMap.get(DcMotor.class, "backRight");

        // Reverse the right side motors so they spin forward together
        frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        // Tell the driver that initialization is complete
        telemetry.addData("Status", "Initialized. Ready to test!");
        telemetry.update();

        // Wait for the driver to press PLAY on the Driver Station
        waitForStart();

        if (isStopRequested()) return;

        // Run while the match is active
        while (opModeIsActive()) {
            // Read gamepad inputs
            double y = -gamepad1.left_stick_y;  // Forward / Backward
            double x = gamepad1.left_stick_x;   // Strafe Left / Right
            double rx = gamepad1.right_stick_x; // Turn Left / Right

            // Denominator ensures no motor power exceeds 1.0 while preserving movement ratios
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

            double frontLeftPower  = (y + x + rx) / denominator;
            double backLeftPower   = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower  = (y + x - rx) / denominator;

            // Apply calculated power to the motors
            frontLeftMotor.setPower(frontLeftPower);
            backLeftMotor.setPower(backLeftPower);
            frontRightMotor.setPower(frontRightPower);
            backRightMotor.setPower(backRightPower);

            // Send live telemetry data to the Driver Station for testing
            telemetry.addData("Front Left Pwr", "%.2f", frontLeftPower);
            telemetry.addData("Back Left Pwr", "%.2f", backLeftPower);
            telemetry.addData("Front Right Pwr", "%.2f", frontRightPower);
            telemetry.addData("Back Right Pwr", "%.2f", backRightPower);
            telemetry.update();
        }
    }
}
