package org.springframework.binding.expression.el;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;

import org.springframework.binding.expression.EvaluationAttempt;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.PropertyNotFoundException;
import org.springframework.binding.expression.SetValueAttempt;
import org.springframework.util.Assert;

/**
 * Evaluates a parsed EL expression.
 * 
 * @author Jeremy Grelle
 */
public class ELExpression implements Expression {

	private ELContextFactory elContextFactory;

	private ValueExpression valueExpression;

	private boolean template;

	/**
	 * Creates a new el expression
	 * @param factory the el context factory for creating the EL context that will be used during expression evaluation
	 * @param valueExpression the value expression to evaluate
	 * @param template whether or not this expression is a template expression; if not it was parsed as an implict eval
	 * expression (without delimiters)
	 */
	public ELExpression(ELContextFactory factory, ValueExpression valueExpression, boolean template) {
		Assert.notNull(factory, "The ELContextFactory is required to evaluate EL expressions");
		Assert.notNull(valueExpression, "The EL value expression is required for evaluation");
		this.elContextFactory = factory;
		this.valueExpression = valueExpression;
		this.template = template;
	}

	public Object getValue(Object context) throws EvaluationException {
		ELContext ctx = elContextFactory.getELContext(context);
		try {
			Object result = valueExpression.getValue(ctx);
			if (result == null && !ctx.isPropertyResolved()) {
				if (getExpressionString().equals("null")) {
					// special case for handling reserved null keyword
					return null;
				} else {
					EvaluationAttempt attempt = new EvaluationAttempt(this, context);
					throw new EvaluationException(attempt, attempt
							+ " failed: the expression path did not resolve--is the base variable incorrect?", null);
				}
			}
			return result;
		} catch (javax.el.PropertyNotFoundException e) {
			throw new PropertyNotFoundException(new EvaluationAttempt(this, context), e);
		} catch (ELException e) {
			throw new EvaluationException(new EvaluationAttempt(this, context), e);
		}
	}

	public void setValue(Object context, Object value) throws EvaluationException {
		ELContext ctx = elContextFactory.getELContext(context);
		try {
			valueExpression.setValue(ctx, value);
			if (!ctx.isPropertyResolved()) {
				SetValueAttempt attempt = new SetValueAttempt(this, context, value);
				throw new EvaluationException(attempt, attempt
						+ " failed: the expression path did not resolve--is the base variable incorrect?", null);
			}
		} catch (javax.el.PropertyNotFoundException e) {
			throw new PropertyNotFoundException(new EvaluationAttempt(this, context), e);
		} catch (ELException ex) {
			throw new EvaluationException(new EvaluationAttempt(this, context), ex);
		}
	}

	public Class getValueType(Object context) {
		ELContext ctx = elContextFactory.getELContext(context);
		try {
			return valueExpression.getType(ctx);
		} catch (javax.el.PropertyNotFoundException e) {
			throw new PropertyNotFoundException(new EvaluationAttempt(this, context), e);
		} catch (ELException ex) {
			throw new EvaluationException(new EvaluationAttempt(this, context), ex);
		}
	}

	public String getExpressionString() {
		if (template) {
			return valueExpression.getExpressionString();
		} else {
			String rawExpressionString = valueExpression.getExpressionString();
			return rawExpressionString.substring("#{".length(), rawExpressionString.length() - 1);
		}
	}

	public int hashCode() {
		return valueExpression.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof ELExpression)) {
			return false;
		}
		ELExpression other = (ELExpression) o;
		return valueExpression.equals(other.valueExpression);
	}

	public String toString() {
		return valueExpression.getExpressionString();
	}

}