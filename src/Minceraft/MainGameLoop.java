package Minceraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import Chunks.Chunk;
import Chunks.ChunkMesh;
import Cube.Block;
import Entities.Camera;
import Entities.Entity;
import Models.RawModel;
import Models.TexturedModel;
import RenderEngine.DisplayManager;
import RenderEngine.Loader;
import RenderEngine.MasterRenderer;
import Shaders.StaticShader;
import Textures.ModelTexture;
import Toolbox.PerlinNoiseGenerator;

public class MainGameLoop {
	
	public static Loader loader1 = null;
	public static StaticShader shader1 = null;
	
	static List<ChunkMesh> chunks = Collections.synchronizedList(new ArrayList<ChunkMesh>());
	static Vector3f camPos = new Vector3f(0, 0, 0);
	static List<Vector3f> usedPos  = new ArrayList<Vector3f>();
	
	static List<Entity> entities = new ArrayList<Entity>();
	
	static final int WORLD_SIZE = 5 * 32;
	
	static boolean closeDisplay = false;

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		
		Loader loader = new Loader();
		loader1 = loader;
		StaticShader shader = new StaticShader();
		shader1 = shader;
		MasterRenderer renderer = new MasterRenderer();
		
		ModelTexture texture = new ModelTexture(loader.loadTexture("TexturePack"));
		
		Camera camera = new Camera(new Vector3f(0, 30, 0), 0, 0, 0);
		
		PerlinNoiseGenerator generator = new PerlinNoiseGenerator();
		
		new Thread(new Runnable() {public void run() {
			
			while(!closeDisplay) {
				
				for (int x = (int) (camPos.x - WORLD_SIZE) / 32; x < (camPos.x + WORLD_SIZE) / 32; x++) {
					for (int z = (int) (camPos.z - WORLD_SIZE) / 32; z < (camPos.z + WORLD_SIZE) / 32; z++) {
						
						if (!usedPos.contains(new Vector3f(x * 32, 0 * 32, z * 32))) {
							
							List<Block> blocks = new ArrayList<Block>();
							
							for (int i = 0; i < 32; i++) {
								for (int j = 0; j < 32; j++) {
									
									blocks.add(new Block(i , (int) generator.generateHeight(i + (x * 32), j + (z * 32)), j, Block.GRASS));
									
								}
							}
							
							Chunk chunk = new Chunk(blocks, new Vector3f(x * 32, 0, z * 32));
							ChunkMesh mesh = new ChunkMesh(chunk);
							
							chunks.add(mesh);
							usedPos.add(new Vector3f(x * 32, 0 * 32, z * 32));
						}
						
					}
				}
				
			}
			
		}}).start();
		
		
		// Main game loop
		int index = 0;
		while (!Display.isCloseRequested()) {
			
			camera.move();
			camPos = camera.getPosition();
			
			if (index < chunks.size()) {
				
				RawModel model123 = loader.loadToVAO(chunks.get(index).positions, chunks.get(index).uvs);
				TexturedModel texModel123 = new TexturedModel(model123, texture);
				Entity entity = new Entity(texModel123, chunks.get(index).chunk.origin, 0, 0, 0, 1);
				entities.add(entity);
				
				chunks.get(index).positions = null;
				chunks.get(index).uvs = null;
				chunks.get(index).normals = null;
				
				index++;
				
			}
			
			for (int i = 0; i < entities.size(); i++) {
				
				Vector3f origin = entities.get(i).getPosition();
				
				int distX = (int) (camPos.x - origin.x);
				int distZ = (int) (camPos.z - origin.z);
				
				if (distX < 0) {
					distX = -distX;
				}
				if (distZ < 0) {
					distZ = -distZ;
				}
				
				if ((distX <= WORLD_SIZE) && (distZ <= WORLD_SIZE)) {
					
					renderer.addEntity(entities.get(i));
					
				}
				
			}
			
			renderer.render(camera);
			
			DisplayManager.updateDisplay();
			
		}
		
		DisplayManager.closeDisplay();

	}

}