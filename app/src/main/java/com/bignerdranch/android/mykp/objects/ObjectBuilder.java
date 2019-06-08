package com.bignerdranch.android.mykp.objects;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static com.bignerdranch.android.mykp.util.Geometry.*;

public class ObjectBuilder {

    private static final int FLOATS_PER_VERTEX = 3;

    private final float[] vertexData;
    private final List<DrawCommand> drawList = new ArrayList<DrawCommand>();

    private int offset = 0;

    private ObjectBuilder(int sizeInVertices) {
        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    }

    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }

    private static int sizeOfOpenCylinderInVertices(int numPoints) {
        return (numPoints + 1) * 2;
    }

    public static GeneratedData createFullCylinder(Cylinder cylinder, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) * 2 + sizeOfOpenCylinderInVertices(numPoints);
        ObjectBuilder builder = new ObjectBuilder(size);

        Circle fullCylinderTop = new Circle(cylinder.center.translateY(cylinder.height / 2), cylinder.radius);
        Circle fullCylinderBottom = new Circle(cylinder.center.translateY(-cylinder.height / 2), cylinder.radius);

        builder.appendCircle(fullCylinderTop, numPoints);
//        builder.appendCircle(fullCylinderBottom, numPoints);
        builder.appendOpenCylinder(cylinder, numPoints);

        return builder.build();

    } // createPuck()

    private GeneratedData build() {
        return new GeneratedData(vertexData, drawList);
    }

    private void appendCircle(Circle circle, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);

        // center point of fan
        vertexData[offset++] = circle.center.x;
        vertexData[offset++] = circle.center.y;
        vertexData[offset++] = circle.center.z;

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);

            vertexData[offset++] =
                    (float) (circle.center.x + circle.radius * Math.cos(angleInRadians));
            vertexData[offset++] = circle.center.y;
            vertexData[offset++] =
                    (float) (circle.center.z + circle.radius * Math.sin(angleInRadians));


        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });
    }

    private void appendOpenCylinder(Cylinder cylinder, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfOpenCylinderInVertices(numPoints);
        final float yStart = cylinder.center.y - (cylinder.height / 2f);
        final float yEnd = cylinder.center.y + (cylinder.height / 2f);

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);
            float xPosition =
                    (float) (cylinder.center.x
                            + cylinder.radius * Math.cos(angleInRadians));
            float zPosition =
                    (float) (cylinder.center.z
                            + cylinder.radius * Math.sin(angleInRadians));
            vertexData[offset++] = xPosition;
            vertexData[offset++] = yStart;
            vertexData[offset++] = zPosition;

            vertexData[offset++] = xPosition;
            vertexData[offset++] = yEnd;
            vertexData[offset++] = zPosition;
        }
        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }


    public static interface DrawCommand {
        void draw();
    }

    public static class GeneratedData {
        public final float[] vertexData;
        public final List<DrawCommand> drawList;

        GeneratedData(float[] vertexData, List<DrawCommand> drawList) {
            this.vertexData = vertexData;
            this.drawList = drawList;
        }
    }

}
