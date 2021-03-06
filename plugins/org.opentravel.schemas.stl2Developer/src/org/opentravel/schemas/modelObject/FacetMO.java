/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.controllers.DefaultModelController;
import org.opentravel.schemas.modelObject.events.OwnershipEventListener;
import org.opentravel.schemas.modelObject.events.ValueChangeEventListener;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.utils.StringComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetMO extends ModelObject<TLFacet> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetMO.class);
	private ValueChangeEventListener<TLFacet, String> inheritedFacetListener;
	private OwnershipEventListener<TLFacetOwner, TLFacet> ownershipListener;

	public FacetMO(final TLFacet obj) {
		super(obj);
		addBaseListener();
	}

	// 3/4/2017 - dmh
	// TODO - the listeners have been commented out prior to deletion. Testing without them is Good.
	// TODO - Delete in next major release.

	// public void attachInheritanceListener() {
	// removeBaseListener();
	// addBaseListener();
	// }

	private void addBaseListener() {
		List<TLFacetType> inheritedTypes = Arrays.asList(TLFacetType.CUSTOM, TLFacetType.QUERY);
		if (getTLModelObj().getOwningEntity() instanceof TLBusinessObject
				&& inheritedTypes.contains(getTLModelObj().getFacetType())) {
			TLBusinessObject owner = (TLBusinessObject) getTLModelObj().getOwningEntity();
			if (owner.getExtension() != null) {
				TLFacet inheritedFacet = findGhostFacets(getTLModelObj().getOwningEntity(), getTLModelObj());
				if (inheritedFacet != null) {
					inheritedFacetListener = listenTo(inheritedFacet);
					ownershipListener = listenToOwner(inheritedFacet.getOwningEntity());
				}
			}
		}
	}

	// // Was USED via NodeChecker in label providers, node tester
	// public boolean isInherited() {
	// return inheritedFacetListener != null;
	// }

	private void removeBaseListener() {
		if (inheritedFacetListener != null) {
			// DefaultModelController modelC = (DefaultModelController) OtmRegistry.getMainController()
			// .getModelController();
			// modelC.removeSourceListener(inheritedFacetListener);
			// inheritedFacetListener = null;
			// modelC.removeSourceListener(ownershipListener);
			// ownershipListener = null;
		}
	}

	public ValueChangeEventListener<TLFacet, String> listenTo(TLFacet affectedItem) {
		DefaultModelController modelC = (DefaultModelController) OtmRegistry.getMainController().getModelController();
		ValueChangeEventListener<TLFacet, String> listener = new ValueChangeEventListener<TLFacet, String>(affectedItem) {

			@Override
			public void processModelEvent(ValueChangeEvent<TLFacet, String> event) {
				// setName(event.getNewValue());
			}

			@Override
			public boolean supported(ModelEventType type) {
				// return ModelEventType.LABEL_MODIFIED.equals(type);
				return false;
			}

		};
		modelC.addSourceListener(listener);
		return listener;
	}

	public OwnershipEventListener<TLFacetOwner, TLFacet> listenToOwner(TLFacetOwner owner) {
		DefaultModelController modelC = (DefaultModelController) OtmRegistry.getMainController().getModelController();
		OwnershipEventListener<TLFacetOwner, TLFacet> listener = new OwnershipEventListener<TLFacetOwner, TLFacet>(
				owner) {

			@Override
			public void processModelEvent(OwnershipEvent<TLFacetOwner, TLFacet> event) {
				// List<ModelEventType> events = Arrays.asList(ModelEventType.CUSTOM_FACET_REMOVED,
				// ModelEventType.QUERY_FACET_REMOVED);
				// // it can be deleted from delete() code.
				// if (inheritedFacetListener != null) {
				// if (events.contains(event.getType())
				// && event.getAffectedItem() == inheritedFacetListener.getSource()) {
				// removeBaseListener();
				// }
				// }
			}

		};
		modelC.addSourceListener(listener);
		return listener;
	}

	@Override
	public boolean addChild(final TLModelElement child) {
		if (child instanceof TLProperty) {
			getTLModelObj().addElement((TLProperty) child);
		} else if (child instanceof TLAttribute) {
			getTLModelObj().addAttribute((TLAttribute) child);
		} else if (child instanceof TLIndicator) {
			getTLModelObj().addIndicator((TLIndicator) child);
		} else
			return false;
		return true;
	}

	@Override
	public void delete() {
		removeBaseListener();

		// Contextual facets handled in ContextualFacetNode
		if (getTLModelObj() instanceof TLContextualFacet)
			return;

		if (getTLModelObj().getOwningEntity() == null) {
			LOGGER.error("Tried to delete a facet MO with no ownining entity.");
			return;
		}
		getTLModelObj().clearFacet();
	}

	@Override
	public List<?> getChildren() {
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.addAll(getTLModelObj().getAttributes());
		kids.addAll(getTLModelObj().getIndicators());
		kids.addAll(getTLModelObj().getElements());
		kids.addAll(getTLModelObj().getAliases());
		if (getTLModelObj() instanceof TLContextualFacet)
			kids.addAll(((TLContextualFacet) getTLModelObj()).getChildFacets());
		return kids;
	}

	/**
	 * @see org.opentravel.schemas.modelObject.ModelObject#getInheritedChildren()
	 */
	@Override
	public List<?> getInheritedChildren() {
		final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
		final List<?> declaredKids = getChildren();

		for (TLAttribute attribute : PropertyCodegenUtils.getInheritedFacetAttributes(getTLModelObj())) {
			if (!declaredKids.contains(attribute)) {
				inheritedKids.add(attribute);
			}
		}
		for (TLIndicator indicator : PropertyCodegenUtils.getInheritedFacetIndicators(getTLModelObj())) {
			if (!declaredKids.contains(indicator)) {
				inheritedKids.add(indicator);
			}
		}
		for (TLProperty element : PropertyCodegenUtils.getInheritedFacetProperties(getTLModelObj())) {
			if (!declaredKids.contains(element)) {
				inheritedKids.add(element);
			}
		}
		return inheritedKids;
	}

	@Override
	public TLFacet getTLModelObj() {
		return srcObj;
	}

	// // FIXME - used by aliasNode rename visitor. Is that needed anymore???
	// @Deprecated
	// @Override
	// public boolean setName(final String name) {
	// // Only custom and query facets can be named.
	// if (getTLModelObj().getFacetType() == TLFacetType.CUSTOM || getTLModelObj().getFacetType() == TLFacetType.QUERY)
	// {
	// getTLModelObj().setLabel(name);
	// return true;
	// }
	// return false;
	// }

	@Override
	public void sort() {
		TLFacet f = getTLModelObj();
		f.sortElements(new StringComparator<TLProperty>() {

			@Override
			protected String getString(TLProperty object) {
				return object.getName();
			}
		});
		f.sortAttributes(new StringComparator<TLAttribute>() {

			@Override
			protected String getString(TLAttribute object) {
				return object.getName();
			}
		});
		f.sortIndicators(new StringComparator<TLIndicator>() {

			@Override
			protected String getString(TLIndicator object) {
				return object.getName();
			}
		});
		// f.sortAliases(new StringComparator<TLAlias>() {
		// @Override
		// protected String getString(TLAlias object) {
		// return object.getName();
		// }
		// });
	}

	// TODO - remove when removing listeners
	@Deprecated
	private static TLFacet findGhostFacets(TLFacetOwner facetOwner, TLFacet obj) {
		TLFacetOwner extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
		Set<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();
		TLFacet inherited = null;

		// Find all of the inherited facets of the specified facet type
		String indentityName = obj.getFacetType().getIdentityName(obj.getContext(), obj.getLabel());
		while (extendedOwner != null) {
			List<TLFacet> facetList = FacetCodegenUtils.getAllFacetsOfType(extendedOwner, obj.getFacetType());

			for (TLFacet facet : facetList) {
				if (indentityName.equals(facet.getFacetType().getIdentityName(facet.getContext(), facet.getLabel()))) {
					return facet;
				}

			}
			visitedOwners.add(extendedOwner);
			extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(extendedOwner);

			if (visitedOwners.contains(extendedOwner)) {
				break; // exit if we encounter a circular reference
			}
		}
		return inherited;
	}

}
