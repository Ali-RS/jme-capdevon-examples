package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.jme3.anim.tween.action.BlendAction;
import com.jme3.anim.tween.action.BlendableAction;
import com.jme3.math.FastMath;

/**
 * States are the basic building blocks of a state machine. Each state contains
 * a Motion (AnimClip or BlendTree) which will play while the character is
 * in that state. When an event in the game triggers a state transition, the
 * character will be left in a new state whose animation sequence will then take
 * over.
 *
 * @author capdevon
 */
public class AnimatorState {

    private static final Logger logger = Logger.getLogger(AnimatorState.class.getName());

    private AnimatorController animator;

    //The motion assigned to this state.
    protected Motion motion;
    //A name can be used to identify a state.
    protected String name;
    //The default speed of the motion.
    protected float speed = 1f;
    //The transitions that are going out of the state.
    protected List<AnimatorStateTransition> transitions = new ArrayList<>();
    //The behaviour list assigned to this state.
    protected List<StateMachineBehaviour> behaviours = new ArrayList<>();

    /**
     * Constructor.
     * 
     * @param name
     * @param animator
     */
    protected AnimatorState(String name, AnimatorController animator) {
        this.name = name;
        this.animator = animator;
    }

    /**
     * Adds a state machine behaviour class to the AnimatorState.
     * @param behaviour The state machine behaviour to add.
     */
    public void addStateMachineBehaviour(StateMachineBehaviour behaviour) {
        behaviours.add(behaviour);
    }
    
    /**
     * Removes a state machine behaviour class from the AnimatorState.
     * @param behaviour The state machine behaviour to remove.
     */
    public void removeStateMachineBehaviour(StateMachineBehaviour behaviour) {
    	behaviours.remove(behaviour);
    }

    /**
     * Utility function to remove a transition from the state.
     * @param transition Transition to remove.
     */
    public void removeTransition(AnimatorStateTransition transition) {
        transitions.remove(transition);
    }

    /**
     * Utility function to add an outgoing transition to the destination state.
     * @param destinationState The destination state.
     * @return 
     */
    public AnimatorStateTransition addTransition(AnimatorState destinationState) {
        return addTransition(destinationState, 0);
    }

    public AnimatorStateTransition addTransition(AnimatorState destinationState, float exitTime) {
        AnimatorStateTransition transition = new AnimatorStateTransition(animator);
        transition.destinationState = destinationState;
        if (exitTime > 0) {
            transition.hasExitTime = true;
            transition.exitTime = FastMath.clamp(exitTime, 0, 1);
        }

        transitions.add(transition);
        return transition;
    }

    /**
     * InternalCall.
     * @param layerName
     * @return
     */
    protected AnimatorState checkTransitions(String layerName) {
        for (AnimatorStateTransition transition: transitions) {
            if (!transition.mute && transition.checkConditions(this, layerName)) {

                // do transition
                AnimatorState nextState = transition.destinationState;
                String animName = nextState.motion.name;

                // Some states may not have an associated animation.
                if (animName != null) {
                    BlendableAction action = (BlendableAction) animator.animComposer.getAction(animName);
                    action.setSpeed(nextState.speed);
                    action.setTransitionLength(transition.duration);
                    animator.animComposer.setCurrentAction(animName, layerName);
                    animator.animComposer.setTime(transition.offset);
                } else {
                    // In this case, remove the previous state animation from the layer.
                    animator.animComposer.removeCurrentAction(layerName);
                }

                return nextState;
            }
        }
        return this;
    }

    /**
     * InternalCall
     * @param tpf
     */
    protected void update(float tpf) {

        if (motion instanceof BlendTree) {
            // Update blend value
            BlendTree blendTree = (BlendTree) motion;
            BlendAction action = (BlendAction) animator.animComposer.getAction(blendTree.name);
            float value = animator.getFloat(blendTree.blendParameter);
            action.getBlendSpace().setValue(value);

            // The order of the children is important.
            // They are supposed to be sorted in ascending order by threshold.
            for (ChildMotion childMotion: blendTree.motions) {
                if (value < childMotion.threshold) {
                    action.setSpeed(childMotion.timeScale);
                    break;
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

}
