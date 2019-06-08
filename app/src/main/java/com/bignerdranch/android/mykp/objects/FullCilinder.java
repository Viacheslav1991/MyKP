package com.bignerdranch.android.mykp.objects;


import com.bignerdranch.android.mykp.data.VertexArray;

import java.util.List;

import static com.bignerdranch.android.mykp.util.Geometry.*;

public class FullCilinder {//as Puck
    private static final int POSITION_COMPONENT_COUNT = 3;
    public final float radius, height;
    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;
    public FullCilinder(float radius, float height, int numPointsAroundCylinder) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createFullCylinder(new Cylinder(
                new Point(0f, 0f, 0f), radius, height), numPointsAroundCylinder);
        this.radius = radius;
        this.height = height;
        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

//    public void bindData(ColorShaderProgram colorShaderProgram) {
//        vertexArray.setVertexAttribPointer(0,colorShaderProgram,);
//    }
}
