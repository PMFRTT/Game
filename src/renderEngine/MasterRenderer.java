package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import guis.GUIShader;
import guis.GuiTexture;
import models.TexturedModel;
import shaders.StaticShader;
import shadows.ShadowMapMasterRenderer;
import terrain.Chunk;
import terrain.Terrain;
import terrain.TerrainShader;

public class MasterRenderer {
	public static final float FOV = 70;
	public static final float NEAR_PLANE = 0.1F;
	public static final float FAR_PLANE = 1000.0F;
	
	private static final float RED = 0.66F;
	private static final float GREEN =0.66F;
	private static final float BLUE = 0.66F;
	private static final float ALPHA = 1F;
	
	private Matrix4f projectionMatrix;
	
	
	private EntityRenderer entityRenderer;
	private TerrainRenderer terrainRenderer;
	private ShadowMapMasterRenderer shadowMapRenderer;
	private TerrainShader terrainShader = new TerrainShader();
	private StaticShader shader = new StaticShader();
	private GUIShader guiShader = new GUIShader();
	
	private Map<TexturedModel,List<Entity>> entities = new HashMap<TexturedModel,List<Entity>>();
	private List<Chunk> terrains = new ArrayList<Chunk>();
	private List<GuiTexture> guis = new ArrayList<GuiTexture>();
	
	
	
	public MasterRenderer(Loader loader, Camera cam)
	{
		enableCulling();
		createProjectionMatrix();
		entityRenderer = new EntityRenderer(shader,projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		this.shadowMapRenderer = new ShadowMapMasterRenderer(cam);
		
	}
	
	public static void enableCulling()
	{
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}
	
	public static void disableCulling()
	{
		GL11.glDisable(GL11.GL_CULL_FACE);
	}
	
	
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
	}
	
	public void renderScene(List<Entity> entities, List<Chunk> terrains, List<Light> lights, Camera camera, Vector4f clipPlane)
	{
		for(Chunk terrain:terrains)
		{
			processTerrain(terrain);
		}
		for(Entity entity:entities)
		{
			processEntity(entity);
		}
		render(lights, camera, clipPlane);
	}

	public void render(List<Light> lights, Camera camera, Vector4f clipPlane)
	{
		prepare();
		
		shader.start();
		shader.loadPlane(clipPlane);
		shader.loadSkyColor(RED, GREEN, BLUE);
		shader.loadLights(lights);
		shader.loadViewMatrix(camera);
		entityRenderer.render(entities);
		shader.stop();
		terrainShader.start();
		terrainShader.loadPlane(clipPlane);
		terrainShader.loadSkyColor(RED, GREEN, BLUE);
		terrainShader.loadLights(lights);
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains, shadowMapRenderer.getToShadowMapSpaceMatrix());
		terrainShader.stop();
		disableCulling();
		//guiShader.start();
		//guiRenderer.render(guis);
		//guiShader.stop();
		entities.clear();
		terrains.clear();
		guis.clear();
		
	}
	
	public void processTerrain(Chunk terrain)
	{
		terrains.add(terrain);
	}
	
	public void processEntity(Entity entity)
	{
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if(batch!=null)
		{
			batch.add(entity);
		}
		else
		{
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}
	public void processGUI(GuiTexture gui)
	{
		guis.add(gui);	
	}
	
	public void renderShadowMap(List<Entity> entityList, Light sun)
	{
		for(Entity entity:entityList)
		{
			processEntity(entity);
		}
		shadowMapRenderer.render(entities, sun);
		entities.clear();
	}
	
	public int getShadowMapTexture()
	{
		return shadowMapRenderer.getShadowMap();
	}
	
	
	public void cleanUp()
	{
		guiShader.cleanUp();
		shader.cleanUp();
		terrainShader.cleanUp();
		shadowMapRenderer.cleanUp();
	}
	
	public void prepare()
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(RED, GREEN, BLUE, ALPHA);
		GL13.glActiveTexture(GL13.GL_TEXTURE5);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, getShadowMapTexture());
		
	}
	
	private void createProjectionMatrix(){
    	projectionMatrix = new Matrix4f();
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
    }

}
