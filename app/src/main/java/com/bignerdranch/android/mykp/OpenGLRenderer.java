package com.bignerdranch.android.mykp;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.bignerdranch.android.mykp.objects.ObjectBuilder;
import com.bignerdranch.android.mykp.util.Geometry;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_ALWAYS;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_EQUAL;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_KEEP;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_REPLACE;
import static android.opengl.GLES20.GL_STENCIL_BUFFER_BIT;
import static android.opengl.GLES20.GL_STENCIL_TEST;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glStencilFunc;
import static android.opengl.GLES20.glStencilOp;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glLineWidth;
import static java.lang.Math.*;

public class OpenGLRenderer implements Renderer, Serializable {

    private final static int POSITION_COUNT = 3;
    private final static int FLOATS_PER_VERTEX = 3;

    private final static String TAG = "OpenGLRenderer";

    private Context context;

    private FloatBuffer vertexData;
    private int uColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;
    private int programId;
    private final static long TIME = 10000L;
    private float tetrahedronAngleMultiplier = 2.5f;
    private float sphereAngleMultiplier = 2.5f;
    private int offset = 0;
    private float verticesInCylinder[];


    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMatrix = new float[16];

    private int mWidth;
    private float mAngleX = (float) (0.5 * PI);
    private float mAngleY = (float) (0.5 * PI);


    private float centerX;
    private float centerY;
    private float centerZ;

    private float upX;
    private float upY;
    private float upZ;

    private float eyeX = 0;
    private float eyeY = 0;
    private float eyeZ = 4;
    private float cameraToAxesRadius = (float) sqrt(pow(eyeX, 2) + pow(eyeY, 2) + pow(eyeZ, 2));
    private ArrayList<Vector> vectors;

    public OpenGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        glClearColor(0f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        int vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vert_shader);
        int fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader);
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        glUseProgram(programId);
        createViewMatrix();
        prepareData();
        bindData();
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        createProjectionMatrix(width, height);
        bindMatrix();
    }

    private void prepareData() {

        float l = 5; //длинна оси
        findCoordinatesOfBottomTetrahedron();
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createFullCylinder(new Geometry.Cylinder(new Geometry.Point(0, 0, 0), 1, 2), 50);
        float[] cylinder = createCylinder(new Geometry.Point(0, 0, 0), 0.5f, 1, 50);
        float[] vertices = {

                //Tetrahedron:
                // first triangle
                vectors.get(0).getX() - 1, -0.65f, vectors.get(0).getZ() - 1,
                0f, 2f, 0f,
                vectors.get(1).getX() - 1, -0.65f, vectors.get(1).getZ() - 1,
                // second triangle
                vectors.get(2).getX() - 1, -0.65f, vectors.get(2).getZ() - 1,
                //third triangle
                vectors.get(0).getX() - 1, -0.65f, vectors.get(0).getZ() - 1,
                //fourth triangle
                0f, 2f, 0f,

                // ось X
                -l, 0, 0,
                l, 0, 0,

                // ось Y
                0, -l, 0,
                0, l, 0,

                // ось Z
                0, 0, -l,
                0, 0, l,


        };
        float[] positions = new float[]{0, 0, 0, 0, 0, 0};
        float[] emptySphereVertices = createSphere(1f, 50, 50, positions);
        float[] torusVertices = createTorus(0.15, 0.5, 50, 50);


        int size = (vertices.length + emptySphereVertices.length
                + torusVertices.length+ cylinder.length) * 4;

        vertexData = ByteBuffer
                .allocateDirect(size)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
//        Log.i(TAG, "Number of points of a vertexData =" + vertexData.toString());
        vertexData.put(cylinder);
        vertexData.put(vertices);
        vertexData.put(emptySphereVertices);
        vertexData.put(torusVertices);

    }

    private void bindData() {
        // координаты
        aPositionLocation = glGetAttribLocation(programId, "a_Position");
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                true, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        // цвет
        uColorLocation = glGetUniformLocation(programId, "u_Color");

        // матрица
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix");
    }

    private void createProjectionMatrix(int width, int height) {
        mWidth = width;
        float ratio = 1;
        float left = -1;
        float right = 1;
        float bottom = -1;
        float top = 1;
        float near = 1;
        float far = 18;
        if (width > height) {
            ratio = (float) width / height;
            left *= ratio;
            right *= ratio;
        } else {
            ratio = (float) height / width;
            bottom *= ratio;
            top *= ratio;
        }

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    private void createViewMatrix() {
        // точка положения камеры
//        eyeX = 3;
//        eyeY = 2;
//        eyeZ = 6;

        // точка направления камеры
        centerX = 0;
        centerY = 0;
        centerZ = 0;

        // up-вектор
        upX = 0;
        upY = 1;
        upZ = 0;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    private void bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 arg0) {


        createViewMatrix();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        fillStencil();

        // Очистка
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_STENCIL_TEST); //отключение буфера трафарета

        drawAxes();
        drawCylinder();
        drawTorus();

        glEnable(GL_STENCIL_TEST);

        drawTetrahedron();

    }

    private void drawTetrahedron() {
        Matrix.setIdentityM(mModelMatrix, 0);
        float angle = (float) (SystemClock.uptimeMillis() % TIME) / TIME * 360;
        Matrix.rotateM(mModelMatrix, 0, angle * tetrahedronAngleMultiplier, 0, 1, 1);
        bindMatrix();
        glStencilFunc(GL_EQUAL, 1, 255);
        glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 0.5f);
        glDrawArrays(GL_TRIANGLE_STRIP, 206, 6);
    }

    private void findCoordinatesOfBottomTetrahedron() {
        float x0 = 1;
        float z0 = 1;
        vectors = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            vectors.add(new Vector((float) (x0 + 2 * Math.sin(2 * Math.PI * i / 3)),
                    z0 + 2 * (float) Math.cos(2 * Math.PI * i / 3)));
        }

    }

    private void drawAxes() {
        glLineWidth(10);
        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();
        glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_LINES, 212, 2);

        glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_LINES, 214, 2);

        glUniform4f(uColorLocation, 1.0f, 0.5f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 216, 2);
    }


    private void fillStencil() {
        // куб в буфер
        glStencilFunc(GL_ALWAYS, 1, 0);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLE_STRIP, 206, 6);

        // Сфера в буфер
        glStencilFunc(GL_ALWAYS, 2, 0);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glUniform4f(uColorLocation, 0, 0, 1, 1);
        glDrawArrays(GL_TRIANGLES, 218, 15000);
    }


    private void drawTorus() {
        Matrix.setIdentityM(mModelMatrix, 0);
        float angle = (float) (SystemClock.uptimeMillis() % TIME) / TIME * 360;
        Matrix.rotateM(mModelMatrix, 0, angle*sphereAngleMultiplier, 1, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, 90, 1, 0, 0);
        bindMatrix();

        // рисуем тор
        glUniform4f(uColorLocation, 1, 0, 0, 1);
        glDrawArrays(GL_TRIANGLE_STRIP, 15218, 5100);

    }


    private void drawCylinder() {
        Matrix.setIdentityM(mModelMatrix, 0);
        float angle = (float) (SystemClock.uptimeMillis() % TIME) / TIME * 360;
        Matrix.rotateM(mModelMatrix, 0, angle*sphereAngleMultiplier, 1, 0, 0);
        bindMatrix();
        glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 52);
        glDrawArrays(GL_TRIANGLE_FAN, 52, 52);
        glDrawArrays(GL_TRIANGLE_STRIP, 104, 102);
    }

    private float[] createSphere(double radius, int _lats, int _longs, float[] position) {
        int i, j;
        int lats = _lats;
        int longs = _longs;
        float[] vertices = new float[lats * longs * 6 * 3];

        int triIndex = 0;
        double sphereSize = -0.5; // -1 for half sphere
        for (i = 0; i < lats; i++) {
            double lat0 = PI * (sphereSize + (double) (i) / lats);
            double z0 = radius * sin(lat0) + position[2];
            double zr0 = cos(lat0);

            double lat1 = PI * (sphereSize + (double) (i + 1) / lats);
            double z1 = radius * sin(lat1) + position[2];
            double zr1 = cos(lat1);

            //glBegin(GL_QUAD_STRIP);
            for (j = 0; j < longs; j++) {
                double lng = 2 * PI * (double) (j - 1) / longs;
                double x = radius * cos(lng);
                double y = radius * sin(lng);

                lng = 2 * PI * (double) (j) / longs;
                double x1 = radius * cos(lng);
                double y1 = radius * sin(lng);

//                glNormal3f(x * zr0, y * zr0, z0);
//                glVertex3f(x * zr0, y * zr0, z0);
//                glNormal3f(x * zr1, y * zr1, z1);
//                glVertex3f(x * zr1, y * zr1, z1);

                /** store after calculating positions */
                float _x1 = (float) (x * zr0) + position[0];
                float _x2 = (float) (x * zr1) + position[0];
                float _x3 = (float) (x1 * zr0) + position[0];
                float _x4 = (float) (x1 * zr1) + position[0];

                float _y1 = (float) (y * zr0) + position[1];
                float _y2 = (float) (y * zr1) + position[1];
                float _y3 = (float) (y1 * zr0) + position[1];
                float _y4 = (float) (y1 * zr1) + position[1];

                // the first triangle
                vertices[triIndex * 9 + 0] = _x1;
                vertices[triIndex * 9 + 1] = _y1;
                vertices[triIndex * 9 + 2] = (float) z0;
                vertices[triIndex * 9 + 3] = _x2;
                vertices[triIndex * 9 + 4] = _y2;
                vertices[triIndex * 9 + 5] = (float) z1;
                vertices[triIndex * 9 + 6] = _x3;
                vertices[triIndex * 9 + 7] = _y3;
                vertices[triIndex * 9 + 8] = (float) z0;

                triIndex++;
                vertices[triIndex * 9 + 0] = _x3;
                vertices[triIndex * 9 + 1] = _y3;
                vertices[triIndex * 9 + 2] = (float) z0;
                vertices[triIndex * 9 + 3] = _x2;
                vertices[triIndex * 9 + 4] = _y2;
                vertices[triIndex * 9 + 5] = (float) z1;
                vertices[triIndex * 9 + 6] = _x4;
                vertices[triIndex * 9 + 7] = _y4;
                vertices[triIndex * 9 + 8] = (float) z1;

//                vertices[triIndex*9 + 0 ] = (float)(x * zr0) -1;    vertices[triIndex*9 + 1 ] = (float)(y * zr0);   vertices[triIndex*9 + 2 ] = (float) z0;
//                vertices[triIndex*9 + 3 ] = (float)(x * zr1) -1;    vertices[triIndex*9 + 4 ] = (float)(y * zr1);   vertices[triIndex*9 + 5 ] = (float) z1;
//                vertices[triIndex*9 + 6 ] = (float)(x1 * zr0) -1;   vertices[triIndex*9 + 7 ] = (float)(y1 * zr0);  vertices[triIndex*9 + 8 ] = (float) z0;
//
//                triIndex ++;
//                vertices[triIndex*9 + 0 ] = (float)(x1 * zr0) -1;   vertices[triIndex*9 + 1 ] = (float)(y1 * zr0);    vertices[triIndex*9 + 2 ] = (float) z0;
//                vertices[triIndex*9 + 3 ] = (float)(x * zr1) -1;    vertices[triIndex*9 + 4 ] = (float)(y * zr1);     vertices[triIndex*9 + 5 ] = (float) z1;
//                vertices[triIndex*9 + 6 ] = (float)(x1 * zr1) -1;    vertices[triIndex*9 + 7 ] = (float)(y1 * zr1);   vertices[triIndex*9 + 8 ] = (float) z1;

                // in this case, the normal is the same as the vertex, plus the normalization;
//                for (int kk = -9; kk<9 ; kk++) {
//                    normals[triIndex * 9 + kk] = vertices[triIndex * 9+kk];
//                    if((triIndex * 9 + kk)%3 == 2)
//                        colors[triIndex * 9 + kk] = 1;
//                    else
//                        colors[triIndex * 9 + kk] = 0;
//                }
                triIndex++;
            }
            //glEnd();
        }
        return vertices;
    }

    private float[] createTorus(double r, double R, int nsides, int rings) {
        List<Float> vert = new ArrayList<>();
        int i, j;
        float theta, phi, theta1;
        float cosTheta, sinTheta;
        float cosTheta1, sinTheta1;
        float ringDelta, sideDelta;

        ringDelta = (float) (2.0 * PI / rings);
        sideDelta = (float) (2.0 * PI / nsides);

        theta = 0.0f;
        cosTheta = 1.0f;
        sinTheta = 0.0f;
        for (i = rings - 1; i >= 0; i--) {
            theta1 = theta + ringDelta;
            cosTheta1 = (float) cos(theta1);
            sinTheta1 = (float) sin(theta1);
            phi = 0.0f;
            for (j = nsides; j >= 0; j--) {
                float cosPhi, sinPhi, dist;

                phi += sideDelta;
                cosPhi = (float) cos(phi);
                sinPhi = (float) sin(phi);
                dist = (float) (R + r * cosPhi);

                vert.add(cosTheta1 * dist);
                vert.add(-sinTheta1 * dist);
                vert.add((float) r * sinPhi);
                vert.add(cosTheta * dist);
                vert.add(-sinTheta * dist);
                vert.add((float) r * sinPhi);
//                gl.glVertex3f(cosTheta1 * dist,   -sinTheta1 * dist,   (float) r * sinPhi);
//                gl.glVertex3f(cosTheta  * dist,   -sinTheta  * dist,   (float) r * sinPhi);
            }
            theta = theta1;
            cosTheta = cosTheta1;
            sinTheta = sinTheta1;
        }
        float[] vertices = new float[vert.size()];
//        Log.e(TAG, "Length of vertices = " + vertices.length);
        for (int k = 0; k < vert.size(); k++) {
            vertices[k] = vert.get(k);
        }
        return vertices;
    }

    private float[] createCylinder(Geometry.Point center, float radius, float height, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) * 2 + sizeOfOpenCylinderInVertices(numPoints);
        verticesInCylinder = new float[size * FLOATS_PER_VERTEX];

        Geometry.Cylinder cylinder = new Geometry.Cylinder(center, radius, height);
        Geometry.Circle fullCylinderTop = new Geometry.Circle(cylinder.center.translateY(cylinder.height / 2), cylinder.radius);
        Geometry.Circle fullCylinderBottom = new Geometry.Circle(cylinder.center.translateY(-cylinder.height / 2), cylinder.radius);

        appendCircle(fullCylinderTop, numPoints);
        appendCircle(fullCylinderBottom, numPoints);
        appendOpenCylinder(cylinder, numPoints);

        return verticesInCylinder;
    }

    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }

    private static int sizeOfOpenCylinderInVertices(int numPoints) {
        return (numPoints + 1) * 2;
    }


    private void appendCircle(Geometry.Circle circle, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);

        // center point of fan
        verticesInCylinder[offset++] = circle.center.x;
        verticesInCylinder[offset++] = circle.center.y;
        verticesInCylinder[offset++] = circle.center.z;

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);

            verticesInCylinder[offset++] =
                    (float) (circle.center.x + circle.radius * Math.cos(angleInRadians));
            verticesInCylinder[offset++] = circle.center.y;
            verticesInCylinder[offset++] =
                    (float) (circle.center.z + circle.radius * Math.sin(angleInRadians));

        }
    }

    private void appendOpenCylinder(Geometry.Cylinder cylinder, int numPoints) {
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
            verticesInCylinder[offset++] = xPosition;
            verticesInCylinder[offset++] = yStart;
            verticesInCylinder[offset++] = zPosition;

            verticesInCylinder[offset++] = xPosition;
            verticesInCylinder[offset++] = yEnd;
            verticesInCylinder[offset++] = zPosition;
        }

    }

    public void setEye(float x, float y) {
        mAngleX -= (((2 * PI) / mWidth) * x / 20) % (2 * PI);
        mAngleY += (((2 * PI) / mWidth) * y / 20) % (2 * PI);
        setEye();
    }

    private void setEye() {
        float cameraX = (float) (cameraToAxesRadius * cos(mAngleX) * sin(mAngleY));
        float cameraY = (float) (cameraToAxesRadius * cos(mAngleY));
        float cameraZ = (float) (cameraToAxesRadius * sin(mAngleX) * sin(mAngleY));

        eyeX = cameraX;
        eyeY = cameraY;
        eyeZ = cameraZ;

        Log.i(TAG, "Angle = " + mAngleX);
        Log.i(TAG, "Radius = " + cameraToAxesRadius);
        Log.i(TAG, "camera X = " + cameraX);
        Log.i(TAG, "camera Z = " + cameraZ);
    }

    public void setTetrahedronAngleMultiplier(float multiplier) {
        tetrahedronAngleMultiplier = 5;
        tetrahedronAngleMultiplier *= multiplier;
    }

    public void setSphereAngleMultiplier(float multiplier) {
        sphereAngleMultiplier = 5;
        sphereAngleMultiplier *= multiplier;
    }

    public void zoomPlus() {
        cameraToAxesRadius--;
        setEye();
    }

    public void zoomMinus() {
        cameraToAxesRadius++;
        setEye();
    }

}

class Vector {
    float x, z;

    public Vector(float x, float z) {
        this.x = x;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

}
