package kroppeb.stareval.function;

import kroppeb.stareval.expression.Expression;

public interface I2FFunction extends TypedFunction{
	float eval(int a);
	
	@Override
	default void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn){
		params[0].evaluateTo(context, functionReturn);
		functionReturn.floatReturn = this.eval(functionReturn.intReturn);
	}
	
	@Override
	default Type getReturnType() {
		return Type.Float;
	}
	
	@Override
	default Type[] getParameterTypes() {
		return new Type[]{Type.Int};
	}
}
