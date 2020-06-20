package Shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public abstract class ShaderProgram {
	
	int programID;
	int vertexShaderID;
	int fragmentShaderID;
	
	FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	public ShaderProgram(String vertexFile, String fragmentFile) {
		
		programID = GL20.glCreateProgram();
		vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
		
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		
		getAllUniformLocations();
		
	}
	
	protected abstract void getAllUniformLocations();
	
	protected int getUniformLocation(String varName) {
		return GL20.glGetUniformLocation(programID, varName);
	}
	
	protected void loadFloat(int location, float value) {
		GL20.glUniform1f(location, value);
	}
	
	protected void load2DVector(int location, Vector2f vector) {
		GL20.glUniform2f(location, vector.x, vector.y);
	}
	
	protected void loadVector3f(int location, Vector3f vector) {
		GL20.glUniform3f(location, vector.x, vector.y, vector.z);
	}
	
	protected void loadBoolean(int location, boolean bool) {
		
		float value = 0;
		
		if (bool) {
			value = 1;
		}
		
		GL20.glUniform1f(location, value);
		
	}
	
	protected void loadMatrix(int location, Matrix4f mat) {
		
		mat.store(matrixBuffer);
		matrixBuffer.flip();
		
		GL20.glUniformMatrix4(location, false, matrixBuffer);
		
	}
		
	protected abstract void bindAttributes();
	
	protected void bindAttribute(String variableName, int attribute) {
		GL20.glBindAttribLocation(programID, attribute, variableName);
	}
	
	public void start() {
		GL20.glUseProgram(programID);
	}
	
	public void stop() {
		GL20.glUseProgram(0);
	}
	
	public void cleanUp() {
		
		stop();
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
		
	}
	
	private int loadShader(String file, int type) {
		
		StringBuilder shaderSource = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine())!=null) {
				shaderSource.append(line).append("//\n");
				
			}
			reader.close();
			
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		
		if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 1000));
			System.err.println("Could not compile shader!");
			System.exit(-1);
			
		}
		
		return shaderID;
		
	}

}