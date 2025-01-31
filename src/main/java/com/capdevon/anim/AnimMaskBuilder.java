package com.capdevon.anim;

import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.anim.AnimationMask;
import com.jme3.anim.Armature;
import com.jme3.anim.Joint;

/**
 *
 * @author capdevon
 */
public class AnimMaskBuilder implements AnimationMask {

    private static final Logger logger = Logger.getLogger(AnimMaskBuilder.class.getName());

    private final BitSet affectedJoints;
    private final Armature armature;

    /**
     * Instantiate a builder with Armature.
     *
     * @param armature
     */
    public AnimMaskBuilder(Armature armature) {
        this.armature = armature;
        this.affectedJoints = new BitSet(armature.getJointCount());
        logger.log(Level.INFO, "Joint count: {0}", armature.getJointCount());
    }

    /**
     * Add all the bones of the model's armature to be influenced by this
     * animation mask.
     *
     * @return AnimMaskBuilder
     */
    public AnimMaskBuilder addAllJoints() {
        int numJoints = armature.getJointCount();
        affectedJoints.set(0, numJoints);
        return this;
    }

    /**
     * Add joints to be influenced by this animation mask.
     *
     * @param jointNames
     * @return AnimMaskBuilder
     */
    public AnimMaskBuilder addJoints(String...jointNames) {
        for (String jointName: jointNames) {
            Joint joint = findJoint(jointName);
            affectedJoints.set(joint.getId());
        }
        return this;
    }

    private Joint findJoint(String jointName) {
        Joint joint = armature.getJoint(jointName);
        if (joint == null) {
            throw new IllegalArgumentException("Cannot find joint " + jointName);
        }
        return joint;
    }

    /**
     * Add a joint and all its sub armature joints to be influenced by this
     * animation mask.
     *
     * @param jointName the starting point (may be null, unaffected)
     * @return AnimMaskBuilder
     */
    public AnimMaskBuilder addFromJoint(String jointName) {
        Joint joint = findJoint(jointName);
        addFromJoint(joint);
        return this;
    }

    private void addFromJoint(Joint joint) {
        affectedJoints.set(joint.getId());
        for (Joint j: joint.getChildren()) {
            addFromJoint(j);
        }
    }

    /**
     * Remove a joint and all its sub armature joints to be influenced by this
     * animation mask.
     *
     * @param jointName the starting point (may be null, unaffected)
     * @return AnimMaskBuilder
     */
    public AnimMaskBuilder removeFromJoint(String jointName) {
        Joint joint = findJoint(jointName);
        removeFromJoint(joint);
        return this;
    }

    private void removeFromJoint(Joint joint) {
        affectedJoints.clear(joint.getId());
        for (Joint j: joint.getChildren()) {
            removeFromJoint(j);
        }
    }

    /**
     * Add the specified Joint and all its ancestors.
     *
     * @param jointName the starting point (may be null, unaffected)
     * @return AnimMaskBuilder
     */
    public AnimMaskBuilder addAncestors(String jointName) {
        Joint joint = findJoint(jointName);
        addAncestors(joint);
        return this;
    }

    private void addAncestors(Joint start) {
        for (Joint joint = start; joint != null; joint = joint.getParent()) {
            affectedJoints.set(joint.getId());
        }
    }

    /**
     * Remove the specified Joint and all its ancestors.
     *
     * @param jointName the starting point (may be null, unaffected)
     * @return AnimMaskBuilder
     */
    public AnimMaskBuilder removeAncestors(String jointName) {
        Joint joint = findJoint(jointName);
        removeAncestors(joint);
        return this;
    }

    private void removeAncestors(Joint start) {
        for (Joint joint = start; joint != null; joint = joint.getParent()) {
            affectedJoints.clear(joint.getId());
        }
    }

    /**
     * Remove the named joints.
     *
     * @param jointNames the names of the joints to be removed
     * @return AnimMaskBuilder
     */
    public AnimMaskBuilder removeJoints(String...jointNames) {
        for (String jointName: jointNames) {
            Joint joint = findJoint(jointName);
            affectedJoints.clear(joint.getId());
        }

        return this;
    }

    @Override
    public boolean contains(Object target) {
        return affectedJoints.get(((Joint) target).getId());
    }

}
