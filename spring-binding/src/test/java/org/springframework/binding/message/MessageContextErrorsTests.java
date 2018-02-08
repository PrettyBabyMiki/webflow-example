package org.springframework.binding.message;

import java.util.HashMap;
import java.util.Locale;

import junit.framework.TestCase;

import org.springframework.binding.expression.spel.SpringELExpressionParser;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MapBindingResult;

public class MessageContextErrorsTests extends TestCase {

	private DefaultMessageContext context;
	private MessageContextErrors errors;

	@Override
	protected void setUp() throws Exception {
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage("foo", Locale.getDefault(), "bar");
		messageSource.addMessage("bar", Locale.getDefault(), "{0}");

		context = new DefaultMessageContext(messageSource);

		SpringELExpressionParser parser = new SpringELExpressionParser(new SpelExpressionParser());
		DefaultMessageCodesResolver resolver = new DefaultMessageCodesResolver();
		errors = new MessageContextErrors(context, "object", new Object(), parser, resolver, null);
	}

	public void testReject() {
		errors.reject("foo");
		errors.reject("bogus", "baz");
		errors.reject("bar", new Object[] { "boop" }, null);

		Message msg = context.getAllMessages()[0];
		assertEquals(null, msg.getSource());
		assertEquals("bar", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());

		msg = context.getAllMessages()[1];
		assertEquals(null, msg.getSource());
		assertEquals("baz", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());

		msg = context.getAllMessages()[2];
		assertEquals(null, msg.getSource());
		assertEquals("boop", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());
	}

	public void testRejectValue() {
		errors.rejectValue("class", "foo");
		errors.rejectValue("class", "bogus", "baz");
		errors.rejectValue("class", "bar", new Object[] { "boop" }, null);

		Message msg = context.getAllMessages()[0];
		assertEquals("class", msg.getSource());
		assertEquals("bar", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());

		msg = context.getAllMessages()[1];
		assertEquals("class", msg.getSource());
		assertEquals("baz", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());

		msg = context.getAllMessages()[2];
		assertEquals("class", msg.getSource());
		assertEquals("boop", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());
	}

	public void testGlobalError() {
		errors.rejectValue(null, "bar", new Object[] { "boop" }, null);
		Message msg = context.getAllMessages()[0];
		assertEquals("", msg.getSource());
		assertEquals("boop", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());
	}

	public void testAddAllErrors() {
		MapBindingResult result = new MapBindingResult(new HashMap<>(), "object");
		result.reject("bar", new Object[] { "boop" }, null);
		result.rejectValue("field", "bar", new Object[] { "boop" }, null);
		errors.addAllErrors(result);

		Message msg = context.getAllMessages()[0];
		assertEquals(null, msg.getSource());
		assertEquals("boop", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());

		msg = context.getAllMessages()[1];
		assertEquals("field", msg.getSource());
		assertEquals("boop", msg.getText());
		assertEquals(Severity.ERROR, msg.getSeverity());
	}

	public void testGetGlobalErrors() {

	}

	public void testGetFieldErrors() {

	}

	public void testGetFieldValue() {

	}

}
