package org.springframework.webflow.execution.repository.impl;

import java.io.Serializable;

import junit.framework.TestCase;

import org.springframework.webflow.execution.repository.snapshot.FlowExecutionSnapshot;
import org.springframework.webflow.execution.repository.snapshot.SnapshotNotFoundException;

public class SimpleFlowExecutionSnapshotGroupTests extends TestCase {

	private SimpleFlowExecutionSnapshotGroup group = new SimpleFlowExecutionSnapshotGroup();

	private FlowExecutionSnapshot snapshot = new FlowExecutionSnapshot() {

	};

	private FlowExecutionSnapshot snapshot2 = new FlowExecutionSnapshot() {

	};

	private FlowExecutionSnapshot snapshot3 = new FlowExecutionSnapshot() {

	};

	public void testInitialState() {
		assertEquals(0, group.getSnapshotCount());
		assertEquals(-1, group.getMaxSnapshots());
		assertEquals(new Integer(1), group.nextSnapshotId());
	}

	public void testGetSnapshot() {
		Serializable id = group.nextSnapshotId();
		group.addSnapshot(id, snapshot);
		assertSame(snapshot, group.getSnapshot(id));
	}

	public void testGetSnapshotNotFound() {
		try {
			group.getSnapshot(group.nextSnapshotId());
			fail("Should have failed");
		} catch (SnapshotNotFoundException e) {

		}
	}

	public void testNextSnapshotId() {
		assertEquals(new Integer(1), group.nextSnapshotId());
		assertEquals(new Integer(2), group.nextSnapshotId());
	}

	public void testAddMaximumReached() {
		group.setMaxSnapshots(2);
		group.addSnapshot(group.nextSnapshotId(), snapshot);
		group.addSnapshot(group.nextSnapshotId(), snapshot2);
		group.addSnapshot(group.nextSnapshotId(), snapshot3);
		assertEquals(2, group.getSnapshotCount());
		try {
			group.getSnapshot(1);
			fail("Should have failed");
		} catch (SnapshotNotFoundException e) {

		}
	}

	public void testRemoveSnapshot() {
		group.addSnapshot(group.nextSnapshotId(), snapshot);
		group.addSnapshot(group.nextSnapshotId(), snapshot2);
		assertEquals(2, group.getSnapshotCount());
		group.removeSnapshot(1);
		assertEquals(1, group.getSnapshotCount());
		try {
			group.getSnapshot(1);
			fail("Should have failed");
		} catch (SnapshotNotFoundException e) {

		}
	}

	public void testRemoveAllSnapshots() {
		group.addSnapshot(group.nextSnapshotId(), snapshot);
		group.addSnapshot(group.nextSnapshotId(), snapshot2);
		assertEquals(2, group.getSnapshotCount());
		group.removeAllSnapshots();
		assertEquals(0, group.getSnapshotCount());
	}

	public void testUpdateSnapshot() {
		group.addSnapshot(group.nextSnapshotId(), snapshot);
		group.updateSnapshot(1, snapshot2);
		assertSame(snapshot2, group.getSnapshot(1));
	}

	public void testRemoveSnapshotDoesNotExist() {
		group.addSnapshot(group.nextSnapshotId(), snapshot);
		group.removeSnapshot(1);
		assertEquals(0, group.getSnapshotCount());
		group.removeSnapshot(1);
		assertEquals(0, group.getSnapshotCount());
	}

	public void testRemoveSnapshotsDoesNotExist() {
		group.addSnapshot(group.nextSnapshotId(), snapshot);
		group.removeAllSnapshots();
		assertEquals(0, group.getSnapshotCount());
		group.removeAllSnapshots();
		assertEquals(0, group.getSnapshotCount());
	}

	public void testUpdateSnapshotDoesNotExist() {
		assertEquals(0, group.getSnapshotCount());
		group.updateSnapshot(1, snapshot2);
		assertEquals(0, group.getSnapshotCount());
	}

}
