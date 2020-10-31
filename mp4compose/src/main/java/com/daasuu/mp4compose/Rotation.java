package com.daasuu.mp4compose;

/**
 * Created by sudamasayuki on 2017/11/15.
 */

public enum Rotation {
    NORMAL(0),
    ROTATION_90(90),
    ROTATION_180(180),
    ROTATION_270(270);

    private final int rotation;

    Rotation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

    public static Rotation fromInt(int rotate) {
        int calcRotate = rotate;
        if (calcRotate > 360) {
            calcRotate -= 360;
        }

        for (Rotation rotation : Rotation.values()) {
            if (calcRotate == rotation.getRotation()) return rotation;
        }

        return NORMAL;
    }

}
