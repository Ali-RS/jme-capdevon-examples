package com.capdevon.demo;

import com.capdevon.anim.AnimUtils;
import com.capdevon.anim.fsm.AnimatorConditionMode;
import com.capdevon.anim.fsm.AnimatorController;
import com.capdevon.anim.fsm.AnimatorControllerLayer;
import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.anim.fsm.AnimatorState;
import com.capdevon.anim.fsm.AnimatorStateMachine;
import com.capdevon.anim.fsm.AnimatorStateTransition;
import com.capdevon.physx.PhysxDebugAppState;
import com.jme3.anim.AnimComposer;
import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 * 
 * @author capdevon
 */
public class Test_AnimStateController extends SimpleApplication {

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {
        Test_AnimStateController app = new Test_AnimStateController();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        settings.setFrameRate(200);
        settings.setSamples(4);
        settings.setBitsPerPixel(32);
        settings.setGammaCorrection(true);
        settings.setVSync(false);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }
    
    private BulletAppState physics;
    private Node player;
    private PlayerMovementControl playerControl;
    
    @Override
    public void simpleInitApp() {
        
        initPhysics();
        createFloor();
        setupPlayer();
        setupChaseCamera();
        setupSky();
        setupLights();
        
        stateManager.attach(new TPSInputAppState());
    }
    
    /**
     * Initialize the physics simulation
     *
     */
    public void initPhysics() {
        physics = new BulletAppState();
        //physics.setThreadingType(ThreadingType.SEQUENTIAL);
        stateManager.attach(physics);
        stateManager.attach(new PhysxDebugAppState());

        physics.setDebugAxisLength(1);
        physics.setDebugEnabled(false);
    }
    
    /**
     * An ambient light and a directional sun light
     */
    private void setupLights() {
    	DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.2f, -1, -0.3f).normalizeLocal());
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.25f, 0.25f, 0.25f, 1));
        rootNode.addLight(ambient);
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        
        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 2_048, 3);
        shadowFilter.setLight(sun);
        shadowFilter.setShadowIntensity(0.4f);
        shadowFilter.setShadowZExtend(256);
        fpp.addFilter(shadowFilter);
    }
    
    /**
     * a sky as background
     */
    private void setupSky() {
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", SkyFactory.EnvMapType.CubeMap);
        sky.setShadowMode(RenderQueue.ShadowMode.Off);
        rootNode.attachChild(sky);
    }
    
    private void createFloor() {
        rootNode.setShadowMode(ShadowMode.CastAndReceive);
        
        Box box = new Box(20, .1f, 20);
        box.scaleTextureCoordinates(new Vector2f(20, 20));
        Geometry floorGeo = new Geometry("Floor.GeoMesh", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/white_grid.jpg");
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", tex);
        floorGeo.setMaterial(mat);
        rootNode.attachChild(floorGeo);

        CollisionShape collShape = CollisionShapeFactory.createMeshShape(floorGeo);
        RigidBodyControl rBody = new RigidBodyControl(collShape, 0f);
        floorGeo.addControl(rBody);
        physics.getPhysicsSpace().add(rBody);
    }
    
    private void setupPlayer() {
    	// setup Player model
        player = (Node) assetManager.loadModel(AnimDefs.MODEL);
        player.setName("Player.Character");
        rootNode.attachChild(player);
        
        // setup flashlight
        PointLight pl = new PointLight(new Vector3f(0, 2.5f, 0), ColorRGBA.Yellow, 4f);
        rootNode.addLight(pl);
        player.addControl(new LightControl(pl, LightControl.ControlDirection.SpatialToLight));
        
        // setup Physics Character
        BetterCharacterControl bcc = new BetterCharacterControl(.4f, 1.8f, 10f);
        player.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);
        
        // Create the controller and the parameters
        AnimatorController animator = new AnimatorController(AnimUtils.getAnimControl(player));
        animator.addParameter("isRunning", AnimatorControllerParameterType.Bool);
        animator.addParameter("isReloading", AnimatorControllerParameterType.Trigger);
        player.addControl(animator);
        
        // Define states for animations.
        AnimatorControllerLayer layer0 = animator.getLayer(AnimComposer.DEFAULT_LAYER);
        AnimatorStateMachine sm = layer0.getStateMachine();
        AnimatorState idle = sm.addState("Idle", AnimDefs.RifleIdle);
        AnimatorState walk = sm.addState("Run", AnimDefs.RifleRun);
        AnimatorState reload = sm.addState("Reload", AnimDefs.Reloading);
        
        AnimatorStateTransition idleToWalk = idle.addTransition(walk);
        idleToWalk.addCondition(AnimatorConditionMode.If, 0f, "isRunning");
        
        AnimatorStateTransition walkToIdle = walk.addTransition(idle);
        walkToIdle.addCondition(AnimatorConditionMode.IfNot, 0f, "isRunning");
        
        AnimatorStateTransition idleToReload = idle.addTransition(reload);
        idleToReload.addCondition(AnimatorConditionMode.If, 0f, "isReloading");
        
        // execute 95% of the reloading animation before returning to idle state
        AnimatorStateTransition reloadToIdle = reload.addTransition(idle, 0.95f);
        
        // set the initial state.
        sm.setDefaultState(idle);
        
        // setup Player Movement Control
        playerControl = new PlayerMovementControl(this);
        player.addControl(playerControl);
    }
    
    private void setupChaseCamera() {
        // disable the default 1st-person flyCam!
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        flyCam.setEnabled(false);
        
        ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
        chaseCam.setLookAtOffset(Vector3f.UNIT_Y.mult(1.5f));
        chaseCam.setMinDistance(5);
        chaseCam.setMaxDistance(8);
        chaseCam.setRotationSpeed(2f);
        chaseCam.setMinVerticalRotation(-FastMath.QUARTER_PI);
        chaseCam.setMaxVerticalRotation(FastMath.QUARTER_PI);
        chaseCam.setDownRotateOnCloseViewOnly(false);
        
        chaseCam.setDefaultDistance(chaseCam.getMinDistance());
    }
    
    private interface AnimDefs {

        final String MODEL = "Models/Rifle/rifle.glb";
        final String RifleIdle = "RifleIdle";
        final String RifleWalk = "RifleWalk";
        final String RifleRun = "RifleRun";
        final String WalkWithRifle = "WalkWithRifle";
        final String ThrowGrenade = "ThrowGrenade";
        final String Reloading = "Reloading";
        final String RifleAimingIdle = "RifleAimingIdle";
        final String FiringRifleSingle = "FiringRifleSingle";
        final String FiringRifleAuto = "FiringRifleAuto";
        final String DeathFromRight = "DeathFromRight";
        final String DeathFromHeadshot = "DeathFromHeadshot";
        final String TPose = "TPose";

    }
    
    private class PlayerMovementControl extends AbstractControl implements ActionListener, AnalogListener {

        public float m_MoveSpeed = 4.5f;
        public float m_TurnSpeed = 10f;
        
        private Camera camera;
        private AnimatorController animator;
        private BetterCharacterControl bcc;
        
        private final Quaternion dr = new Quaternion();
        private final Vector3f cameraDir = new Vector3f();
        private final Vector3f cameraLeft = new Vector3f();
        private final Vector3f walkDirection = new Vector3f();
        private boolean _MoveForward, _MoveBackward, _TurnLeft, _TurnRight;
        
        public PlayerMovementControl(Application app) {
            this.camera = app.getCamera();
        }
        
        @Override
        public void setSpatial(Spatial sp) {
            super.setSpatial(sp);
            if (spatial != null) {
                this.animator = spatial.getControl(AnimatorController.class);
                this.bcc = spatial.getControl(BetterCharacterControl.class);
            }
        }

        @Override
        public void onAnalog(String name, float value, float tpf) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            //To change body of generated methods, choose Tools | Templates.
            if (name.equals(InputMapping.MOVE_FORWARD)) {
                _MoveForward = isPressed;
            } else if (name.equals(InputMapping.MOVE_BACKWARD)) {
                _MoveBackward = isPressed;
            } else if (name.equals(InputMapping.MOVE_LEFT)) {
                _TurnLeft = isPressed;
            } else if (name.equals(InputMapping.MOVE_RIGHT)) {
                _TurnRight = isPressed;
            } else if (name.equals(InputMapping.RELOAD) && isPressed) {
                animator.setTrigger("isReloading");
            }
        }

        @Override
        public void controlUpdate(float tpf) {

            camera.getDirection(cameraDir).setY(0);
            camera.getLeft(cameraLeft).setY(0);

            walkDirection.set(0, 0, 0);

            if (_MoveForward) {
                walkDirection.addLocal(cameraDir);
            } else if (_MoveBackward) {
                walkDirection.subtractLocal(cameraDir);
            }
            if (_TurnLeft) {
                walkDirection.addLocal(cameraLeft);
            } else if (_TurnRight) {
                walkDirection.subtractLocal(cameraLeft);
            }

            walkDirection.normalizeLocal();
            boolean isMoving = walkDirection.lengthSquared() > 0;

            if (isMoving) {
            	// smooth rotation
            	float angle = FastMath.atan2(walkDirection.x, walkDirection.z);
                dr.fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
                spatial.getWorldRotation().slerp(dr, m_TurnSpeed * tpf);
                bcc.setViewDirection(spatial.getWorldRotation().mult(Vector3f.UNIT_Z));
            }
            
            bcc.setWalkDirection(walkDirection.multLocal(m_MoveSpeed));
            
            animator.setBool("isRunning", isMoving);
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
            //To change body of generated methods, choose Tools | Templates.
        }

    }
	
    private interface InputMapping {

        final String MOVE_LEFT 		= "MOVE_LEFT";
        final String MOVE_RIGHT 	= "MOVE_RIGHT";
        final String MOVE_FORWARD 	= "MOVE_FORWARD";
        final String MOVE_BACKWARD 	= "MOVE_BACKWARD";
        final String RUNNING 		= "RUNNING";
        final String RELOAD		= "RELOAD";
        final String FIRE 		= "FIRE";
    }
	
    private class TPSInputAppState extends BaseAppState implements AnalogListener, ActionListener {

        private InputManager inputManager;
        private PlayerMovementControl playerControl;

        @Override
        protected void initialize(Application app) {
            this.inputManager = app.getInputManager();
            Spatial player = getRootNode().getChild("Player.Character");
            playerControl = player.getControl(PlayerMovementControl.class);
            addInputMappings();
        }
        
        public Node getRootNode() {
            return ((SimpleApplication) getApplication()).getRootNode();
        }

        public Node getGuiNode() {
            return ((SimpleApplication) getApplication()).getGuiNode();
        }

        @Override
        protected void cleanup(Application app) {
        }

        @Override
        protected void onEnable() {
        }

        @Override
        protected void onDisable() {
        }

        private void addInputMappings() {

            addMapping(InputMapping.MOVE_FORWARD, 	new KeyTrigger(KeyInput.KEY_W));
            addMapping(InputMapping.MOVE_BACKWARD, 	new KeyTrigger(KeyInput.KEY_S));
            addMapping(InputMapping.MOVE_LEFT, 		new KeyTrigger(KeyInput.KEY_A));
            addMapping(InputMapping.MOVE_RIGHT, 	new KeyTrigger(KeyInput.KEY_D));
            addMapping(InputMapping.RUNNING, 		new KeyTrigger(KeyInput.KEY_SPACE));
            addMapping(InputMapping.FIRE, 		new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
            addMapping(InputMapping.RELOAD, 		new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        }

        private void addMapping(String bindingName, Trigger...triggers) {
            inputManager.addMapping(bindingName, triggers);
            inputManager.addListener(this, bindingName);
        }

        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (isEnabled() && playerControl != null) {
                playerControl.onAnalog(name, value, tpf);
            }
        }

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isEnabled() && playerControl != null) {
                playerControl.onAction(name, isPressed, tpf);
            }
        }

        public void setPlayerControl(PlayerMovementControl playerControl) {
            this.playerControl = playerControl;
        }
    }

}
