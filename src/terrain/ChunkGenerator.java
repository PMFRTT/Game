package terrain;

import engineTester.MainGameLoop;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.TerrainRenderer;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import utils.Vector2I;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class ChunkGenerator {

    private final HashMap<String, Chunk> chunks = new HashMap<String, Chunk>();

    public static final int SIZE = 128;

    private TerrainTexture texture;
    private TerrainTexturePack texturePack;
    private int seed;

    private Loader loader;

    public ChunkGenerator(TerrainTexture texture, TerrainTexturePack texturePack, Loader loader) {
        Random random = new Random();

        this.texture = texture;
        this.texturePack = texturePack;
        this.loader = loader;
        this.seed = random.nextInt(Integer.MAX_VALUE);
    }

    public void generateChunks(Vector3f pos) {
        String currentGridPos = getChunkCoordinates(pos);
        Vector2I currentGridPosInt = getChunkCoordinatesInt(pos);
        for (int i = -1; i < 1; i++) {
            for (int j = -1; j < 1; j++) {
                currentGridPos = (getChunkCoordinatesInt(pos).x + i) + " " + (getChunkCoordinatesInt(pos).y + j);
                currentGridPosInt = new Vector2I(getChunkCoordinatesInt(pos).x + i, getChunkCoordinatesInt(pos).y + j);
                if (!chunks.containsKey(currentGridPos)) {
                    Chunk chunk = new Chunk(currentGridPosInt.x, currentGridPosInt.y, this.loader, texturePack, texture, seed);
                    chunks.put(currentGridPos, chunk);
                }
            }
        }


    }

    public String getChunkCoordinates(Vector3f pos) {

        float gridX = pos.x / SIZE;
        int gridXReturn = 0;
        if (gridX < 0) {
            gridXReturn = (int) gridX - 1;
        } else if (gridX > 0) {
            gridXReturn = (int) gridX;
        }

        float gridZ = pos.z / SIZE;
        int gridZReturn = 0;
        if (gridZ < 0) {
            gridZReturn = (int) gridZ - 1;
        } else if (gridZ > 0) {
            gridZReturn = (int) gridZ;
        }
        System.out.println(gridXReturn + " " + gridZReturn);
        return gridXReturn + " " + gridZReturn;
    }

    public Vector2I getChunkCoordinatesInt(Vector3f pos) {

        float gridX = pos.x / SIZE;
        int gridXReturn = 0;
        if (gridX < 0) {
            gridXReturn = (int) gridX - 1;
        } else if (gridX > 0) {
            gridXReturn = (int) gridX;
        }

        float gridZ = pos.z / SIZE;
        int gridZReturn = 0;
        if (gridZ < 0) {
            gridZReturn = (int) gridZ - 1;
        } else if (gridZ > 0) {
            gridZReturn = (int) gridZ;
        }
        return new Vector2I(gridXReturn, gridZReturn);
    }

    public Chunk getCurrentChunk(Vector3f pos) {
        return chunks.get(getChunkCoordinates(pos));
    }

    public List<Chunk> getChunks() {
        return chunks.values().stream().toList();
    }

    public float getHeightOfTerrain(String chunkPos, Vector2f pos) {
        if (chunks.containsKey(chunkPos)) {
            return chunks.get(chunkPos).getHeightOfTerrain(pos.x, pos.y);
        }
        return 0;
    }
}
