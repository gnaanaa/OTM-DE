/**
 * 
 */
package com.sabre.schemas.controllers;


import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemacompiler.model.TLDocumentationItem;
import com.sabre.schemas.node.DocumentationNode;
import com.sabre.schemas.node.DocumentationNode.DocumentationNodeType;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeModelTestUtils;
import com.sabre.schemas.node.Node_Tests;
import com.sabre.schemas.node.controllers.DescriptionDocItemNodeModelController;
import com.sabre.schemas.node.controllers.DocumentationNodeModelManager;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 *
 */
public class DocumentationNodeModelManager_Tests {
	ModelNode model = null;
	Node_Tests nt = new Node_Tests();
	DocumentationNodeModelManager dnmm = new DocumentationNodeModelManager();
	TLDocumentationItem di = new TLDocumentationItem();
	

	@Test
	public void dnmmTest() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();
		
		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			nt.visitAllNodes(ln);			
		}
		NodeModelTestUtils.testNodeModel();
		di.setText("DDD For adding to other documentation.");
		
		for (LibraryNode ln : model.getUserLibraries()) {
			visitLibraryDoc(ln);
		}
	}

	private void visitLibraryDoc(LibraryNode ln) {
		DocumentationNode dn = null;
		for (Node n : ln.getDescendants()) {
			if (n.getDocumentation() != null) {
				dn = dnmm.createDocumentationTreeRoot(n.getDocumentation());
				TLDocumentationItem di = dn.getDocItem();
				Assert.assertNotNull(dn.getChildren());
				
				for (DocumentationNode d : dn.getChildren()) {
					visitDocItem(d);
				}
				Assert.assertNotNull(dn.getLabel());
				Assert.assertNotNull(dn.getValue());
			}
		}
	}
	
	private void visitDocItem(DocumentationNode dn) {
		List<DocumentationNode> kids = new ArrayList<DocumentationNode>(dn.getChildren());
		Assert.assertNotNull(kids);
		Assert.assertNotNull(dn.getLabel());
		Assert.assertNotNull(dn.getValue());
		Assert.assertNotNull(dn.getType());
		// labels = truncated value
		
		// Read a value, set it, then make sure it is set.
		if (dn.getType().equals(DocumentationNodeType.DOCUMENTATION_ITEM) &&
				!dn.getValue().isEmpty()) {
			// System.out.println("Label: "+dn.getLabel());

			String doc = "AAA Now is the time for all good dogs to chase a cat.";
			dn.setValue(doc);
			Assert.assertEquals(doc, dn.getValue());
			
			if (!(dn.getParent().getModelController() instanceof DescriptionDocItemNodeModelController)) {
				int siblingCnt = dn.getParent().getChildren().size();
				DocumentationNode newDN = dnmm.createDocItemNodeForTypeRoot(dn.getParent());
				// NO - createDoc... adds it to parent. dn.getParent().addChild(newDN);
				newDN.setValue(doc);
				Assert.assertEquals(doc, newDN.getValue());
				Assert.assertEquals(siblingCnt+1, dn.getParent().getChildren().size());
				
				newDN.getParent().removeChild(newDN);
				Assert.assertEquals(siblingCnt, dn.getParent().getChildren().size());
			}
		}
		
		for (DocumentationNode d : kids) {
			visitDocItem(d);
		}

	}
	

	
}
