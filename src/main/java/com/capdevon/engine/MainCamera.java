/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.engine;

import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author capdevon
 */
public class MainCamera {
    
    private Camera cam;
    private float fieldOfView;
    private float near;
    private float far;
    
    /**
     * Creates a camera state that will initialize the application camera to a
     * 45 degree fov, 0.1 near plane, and 1000 far plane.
     * 
     * @param cam
     */
    public MainCamera(Camera cam) {
        this(cam, 45, 0.1f, 1000); // 45 is the default JME fov
    }

    /**
     * Creates a camera state that will initialize the specified camera to the
     * specified parameters. If the specified camera is null then the
     * application's main camera will be used.
     * 
     * @param cam
     * @param fov
     * @param near
     * @param far
     */
    public MainCamera(Camera cam, float fov, float near, float far) {
        this.cam = cam;
        this.fieldOfView = fov;
        this.near = near;
        this.far = far;
        resetCamera();
    }
    
    public void setFieldOfView(float f) {
        if (this.fieldOfView == f) {
            return;
        }
        this.fieldOfView = f;
        resetCamera();
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public void setNear(float f) {
        if (this.near == f) {
            return;
        }
        this.near = f;
        resetCamera();
    }

    public float getNear() {
        return near;
    }

    public void setFar(float f) {
        if (this.far == f) {
            return;
        }
        this.far = f;
        resetCamera();
    }

    public float getFar() {
        return far;
    }
    
    private void resetCamera() {
        float aspect = (float) cam.getWidth() / (float) cam.getHeight();
        cam.setFrustumPerspective(fieldOfView, aspect, near, far);
    }
    
    public Ray screenPointToRay(Vector2f click2d) {
        // Convert screen click to 3d position
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d), 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d), 1f).subtractLocal(click3d);
        // Aim the ray from the clicked spot forwards.
        Ray ray = new Ray(click3d, dir);
        return ray;
    }
    
}
