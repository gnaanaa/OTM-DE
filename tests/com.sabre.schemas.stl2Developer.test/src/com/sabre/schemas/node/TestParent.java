package com.sabre.schemas.node;

import junit.framework.Assert;

import org.junit.Test;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * Make sure all nodes can trace up the tree to the model node.
 * 
 * @author Dave Hollander
 *
 */
public class TestParent {
	LoadFiles lf = new LoadFiles();
	Node_Tests nt = new Node_Tests();
	LibraryTests lt = new LibraryTests();


	@Test
	public void testGetParent() throws Exception {
		MainController mc = new MainController();
		
		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			for (Node n : ln.getDescendants_NamedTypes())
				parentVisitor(n);
		}
		
	}

	private void parentVisitor(Node target){
		Node testNode = null;
		Node parent = target;
		do {
			testNode = parent;
			parent = parent.getParent();
		} while (parent != null);
		Assert.assertTrue(testNode instanceof ModelNode);
	}
}