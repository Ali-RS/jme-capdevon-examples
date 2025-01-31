package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.List;

import com.jme3.math.Vector2f;

/**
 * Blend trees are used to blend continuously animation between their childs.
 * They can either be 1D or 2D.
 * 
 * @author capdevon
 */
public class BlendTree extends Motion {

    public enum BlendTreeType {
        Simple1D,
//        SimpleDirectional2D,      //not yet supported
//        FreeformDirectional2D,    //not yet supported
//        FreeformCartesian2D,      //not yet supported
//        Direct                    //not yet supported
    }

    //Parameter that is used to compute the blending weight of the childs in 1D blend trees or on the X axis of a 2D blend tree.
    protected String blendParameter;
    //Parameter that is used to compute the blending weight of the childs on the Y axis of a 2D blend tree. (not yet supported)
    protected String blendParameterY;
    //The Blending type can be either 1D or different types of 2D.
    protected BlendTreeType blendType = BlendTreeType.Simple1D;
    //Sets the maximum threshold that will be used by the ChildMotion.
    protected float maxThreshold = 1f;
    //Sets the minimum threshold that will be used by the ChildMotion.
    protected float minThreshold = 0f;
    //The list of the blend tree child motions.
    protected List<ChildMotion> motions = new ArrayList<>();

    /**
     * Create blend tree with the minimum and maximum threshold for the LinearBlendSpace.
     * 
     * @param minThreshold
     * @param maxThreshold
     */
    public BlendTree(int minThreshold, int maxThreshold) {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    /**
     * Utility function to remove the child of a blend tree.
     * 
     * @param index - The index of the blend tree to remove.
     */
    public void removeChild(int index) {
        motions.remove(index);
    }

    /**
     * Utility function to add a child motion to a blend trees.
     * 
     * @param animName - The motion to add as child.
     * @param position - The position of the child. When using 2D blend trees.
     */
    public ChildMotion addChild(String animName, Vector2f position) {
        return addChild(animName, position, 0f);
    }

    /**
     * Utility function to add a child motion to a blend trees.
     * 
     * @param animName  - The motion to add as child.
     * @param threshold - The threshold of the child. When using 1D blend trees.
     */
    public ChildMotion addChild(String animName, float threshold) {
        return addChild(animName, Vector2f.ZERO.clone(), threshold);
    }

    private ChildMotion addChild(String animName, Vector2f position, float threshold) {
        ChildMotion motion = new ChildMotion();
        motion.animName = animName;
        motion.position = position;
        motion.threshold = threshold;
        motions.add(motion);
        return motion;
    }

    public String[] getAnimMotionsNames() {
        String[] clips = new String[motions.size()];
        for (int i = 0; i < clips.length; i++) {
            String animName = motions.get(i).animName;
            clips[i] = animName;
        }
        return clips;
    }

    public String getBlendParameter() {
        return blendParameter;
    }

    public void setBlendParameter(String blendParameter) {
        this.blendParameter = blendParameter;
    }

    public float getMaxThreshold() {
        return maxThreshold;
    }

    public float getMinThreshold() {
        return minThreshold;
    }

}
