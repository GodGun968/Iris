package net.coderbot.iris.uniforms.custom.cached;

import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.parsing.VectorType;
import net.coderbot.iris.vendored.joml.Vector3f;
import org.lwjgl.opengl.GL21;

import java.util.function.Supplier;

public class Float3VectorCachedUniform extends VectorCachedUniform<Vector3f> {
	
	public Float3VectorCachedUniform(UniformUpdateFrequency updateFrequency, Supplier<Vector3f> supplier) {
		super(updateFrequency, supplier);
	}
	
	@Override
	protected void setFrom(Vector3f other) {
		this.cached.set(other);
	}
	
	@Override
	protected void push() {
		GL21.glUniform3f(this.getLocation(), this.cached.x, this.cached.y, this.cached.z);
	}
	
	@Override
	public VectorType getType() {
		return VectorType.VEC3;
	}
}
