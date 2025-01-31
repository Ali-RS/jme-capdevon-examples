package com.capdevon.physx;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.math.Vector3f;

public class RaycastHit {

    public PhysicsCollisionObject rigidBody;
    public CollisionShape collider;
    public Object gameObject;
    public float distance;
    public Vector3f normal = new Vector3f();
    public Vector3f point = new Vector3f();

    public void clear() {
        rigidBody = null;
        collider = null;
        gameObject = null;
        distance = Float.NaN;
        point.set(Vector3f.NAN);
        normal.set(Vector3f.NAN);
    }

    @Override
    public String toString() {
        return "RaycastHit [rigidbody=" + toHexString(rigidBody) +
            ", collider=" + toHexString(collider) +
            ", gameObject=" + toHexString(gameObject) +
            ", distance=" + distance +
            ", normal=" + normal +
            ", point=" + point +
            "]";
    }

    private String toHexString(Object obj) {
        if (obj != null)
            return obj.getClass().getSimpleName() + '@' + Integer.toHexString(obj.hashCode());
        return null;
    }

}
