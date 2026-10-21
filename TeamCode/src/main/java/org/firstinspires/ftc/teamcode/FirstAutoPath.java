package org.firstinspires.ftc.teamcode;
import static com.pedropathing.api.Paths.*;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.pedro.Constants;


@Autonomous(name = "FirstAutoPath", group = "Autonomous")
public class FirstAutoPath extends LinearOpMode {

    private Follower follower;
    private int pathState = 0; // Tracks our current state/case in autonomous

    private final PoseFactory poseFactory = PoseFactory.degrees();

    // Your coordinates
    private final Pose start = poseFactory.of(83.8657, 132.8529, 90);
    private final Pose moveforwardStart = poseFactory.of(83.8657, 132.8529, -90);
    private final Pose moveforward = poseFactory.of(83.7781, 120.6346, 450);
    private final Pose moveRighttoget4Start = poseFactory.of(83.7781, 120.6346, 90);
    private final Pose moveRighttoget4 = poseFactory.of(133.1036, 120.1995, 75);
    private final Pose movedown = poseFactory.of(133.2366, 133.5972, 90);
    private final Pose moveback = poseFactory.of(102.2347, 22.1998, 90);
    private final Pose left = poseFactory.of(82.2864, 17.4207, 90);
    private final Pose park = poseFactory.of(126.6368, 37.6675, -155.4624);

    // Path declarations for your sequence
    private Path path1, path2, path3, path4, path5, path6, path7, path8;

    public void buildPaths() {
        // Paths
        path1 = line(start, moveforwardStart).linear(start, moveforwardStart);
        path2 = line(moveforwardStart, moveforward).linear(moveforwardStart, moveforward);
        path3 = line(moveforward, moveRighttoget4Start).linear(moveforward, moveRighttoget4Start);
        path4 = line(moveRighttoget4Start, moveRighttoget4).linear(moveRighttoget4Start, moveRighttoget4);
        path5 = line(moveRighttoget4, movedown).linear(moveRighttoget4, movedown);
        path6 = line(movedown, moveback).constant(moveback);
        path7 = line(moveback, left).constant(left);
        path8 = line(left, park).reverseTangent();
    }

    public void autonomousPathUpdate() {
        //Have to add shooting ball part here
        switch (pathState) {
            case 0: // Move forward start
                follower.follow(path1);
                setPathState(1);
                break;

            case 1: // Wait for path1, then move forward
                if (!follower.isBusy()) {
                    follower.follow(path2);
                    setPathState(2);
                }
                break;

            case 2: // Wait for path2, then target moveRighttoget4Start
                if (!follower.isBusy()) {
                    follower.follow(path3);
                    setPathState(3);
                }
                break;

            case 3: // Wait for path3, then move right to get 4
                if (!follower.isBusy()) {
                    follower.follow(path4);
                    setPathState(4);
                }
                break;

            case 4: // Wait for path4, then move down
                if (!follower.isBusy()) {
                    follower.follow(path5);
                    setPathState(5);
                }
                break;

            case 5: // Wait for path5, then move back
                if (!follower.isBusy()) {
                    follower.follow(path6);
                    setPathState(6);
                }
                break;

            case 6: // Wait for path6, then move left
                if (!follower.isBusy()) {
                    follower.follow(path7);
                    setPathState(7);
                }
                break;
                //have to add shooting ball here

            case 7: // Wait for path7, then final park
                if (!follower.isBusy()) {
                    follower.follow(path8);
                    setPathState(8);
                }
                break;

            case 8: //  Complete
                if (!follower.isBusy()) {
                    setPathState(-1);
                }
                break;
        }
    }

    public void setPathState(int state) {
        pathState = state;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        follower = Constants.create(hardwareMap);

        follower.setPose(start);

        buildPaths();

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive() && !isStopRequested()) {
            follower.update(); // Background positional tracker updates
            autonomousPathUpdate(); // Processes path cases
        }
    }
}