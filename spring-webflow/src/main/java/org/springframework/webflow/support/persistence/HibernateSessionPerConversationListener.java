/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.support.persistence;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewSelection;

/**
 * A {@link FlowExecutionListener} that implements the Hibernate Session-per-Conversation pattern as described in Java
 * Persistence with Hibernate (chapter 11).
 * <p>
 * This implementation uses raw Hibernate APIs and binds the current session to the thread-local location identified by
 * Spring's <code>HibernateTransactionManager</code>.
 * <p>
 * This listener assumes that you are accessing Hibernate via Spring support such as HibernateTemplate or the
 * LocalSessionFactoryBean. If not, Hibernate data access code will not participate in the proper transaction.
 * <p>
 * Note that when accessing service layer methods with Spring managed transactions, those transaction should have
 * {@link Propagation#REQUIRED} semantics. Anything else defeats the purpose of holding the transaction open until the
 * end of the session.
 * 
 * @author Ben Hale
 * @since 1.1
 */
public class HibernateSessionPerConversationListener extends FlowExecutionListenerAdapter {

    private static final String HIBERNATE_SESSION_ATTRIBUTE = "hibernate.session";

    private SessionFactory sessionFactory;

    /**
     * Create a new Session-per-Conversation listener using giving Hibernate session factory.
     * @param sessionFactory the session factory to use
     */
    public HibernateSessionPerConversationListener(SessionFactory sessionFactory) {
	this.sessionFactory = sessionFactory;
    }

    public void sessionCreated(RequestContext context, FlowSession session) {
	if (session.isRoot() && session.getDefinition().getAttributes().contains("persistenceContext")) {
	    Session hibernateSession = createSession(context);
	    context.getConversationScope().put(HIBERNATE_SESSION_ATTRIBUTE, hibernateSession);
	    bind(hibernateSession, context, session);
	}
    }

    public void resumed(RequestContext context) {
	bind(getHibernateSession(context), context, context.getFlowExecutionContext().getActiveSession());
    }

    public void paused(RequestContext context, ViewSelection selectedView) {
	unbind(getHibernateSession(context), context, context.getFlowExecutionContext().getActiveSession());
    }

    public void sessionEnded(RequestContext context, FlowSession session, AttributeMap output) {
	if (session.isRoot()) {
	    Session hibernateSession = (Session) context.getConversationScope().remove(HIBERNATE_SESSION_ATTRIBUTE);
	    Boolean commitStatus = session.getState().getAttributes().getBoolean("commit");
	    if (commitStatus == null || commitStatus.equals(Boolean.TRUE)) {
		// assume a commit by default and when 'commit' attribute = true
		hibernateSession.flush();
		hibernateSession.close();
	    }
	    unbind(hibernateSession, context, session);
	}
    }

    public void exceptionThrown(RequestContext context, FlowExecutionException exception) {
	unbind(getHibernateSession(context), context, context.getFlowExecutionContext().getActiveSession());
    }

    // internal helpers

    private Session createSession(RequestContext context) {
	Session session = sessionFactory.openSession();
	session.setFlushMode(FlushMode.MANUAL);
	return session;
    }

    private Session getHibernateSession(RequestContext context) {
	return (Session) context.getConversationScope().get(HIBERNATE_SESSION_ATTRIBUTE);
    }

    private void bind(Session session, RequestContext context, FlowSession flowSession) {
	SessionHolder sessionHolder = new SessionHolder(session);
	if (flowSession.getDefinition().getAttributes().contains("transactional")) {
	    Transaction tx = session.beginTransaction();
	    sessionHolder.setTransaction(tx);
	}
	TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
    }

    private void unbind(Session session, RequestContext context, FlowSession flowSession) {
	if (flowSession.getDefinition().getAttributes().contains("transactional")) {
	    session.getTransaction().commit();
	}
	TransactionSynchronizationManager.unbindResource(sessionFactory);
    }
}