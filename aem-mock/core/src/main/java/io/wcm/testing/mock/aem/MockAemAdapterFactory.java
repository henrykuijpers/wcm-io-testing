/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
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
 * #L%
 */
package io.wcm.testing.mock.aem;

import static com.day.cq.tagging.TagConstants.NT_TAG;
import static com.day.cq.wcm.api.NameConstants.NT_PAGE;
import static com.day.cq.wcm.api.NameConstants.NT_TEMPLATE;
import static io.wcm.testing.mock.aem.MockContentPolicyStorage.PN_POLICY;
import static io.wcm.testing.mock.aem.MockContentPolicyStorage.RT_CONTENTPOLICY;
import static io.wcm.testing.mock.aem.MockContentPolicyStorage.RT_CONTENT_POLICY_MAPPING;
import static io.wcm.testing.mock.aem.MockContentPolicyStorage.RT_CONTENT_POLICY_MAPPINGS;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.annotations.Component;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import com.day.cq.wcm.api.policies.ContentPolicyMapping;

/**
 * Mock adapter factory for AEM-related adaptions.
 */
@Component(service = AdapterFactory.class,
    property = {
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.ResourceResolver",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.Page",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.PageManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.Template",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.components.ComponentManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.tagging.TagManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.tagging.Tag",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.designer.Designer",
        AdapterFactory.ADAPTER_CLASSES + "=com.adobe.cq.dam.cfm.ContentFragment",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.policies.ContentPolicy",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.policies.ContentPolicyMapping",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.wcm.api.policies.ContentPolicyManager"
    })
@ProviderType
public final class MockAemAdapterFactory implements AdapterFactory {

  @Override
  public @Nullable <AdapterType> AdapterType getAdapter(final @NotNull Object adaptable, final @NotNull Class<AdapterType> type) {
    if (adaptable instanceof Resource) {
      return getAdapter((Resource)adaptable, type);
    }
    if (adaptable instanceof ResourceResolver) {
      return getAdapter((ResourceResolver)adaptable, type);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private @Nullable <AdapterType> AdapterType getAdapter(@NotNull final Resource resource, @NotNull final Class<AdapterType> type) {
    if (type == Page.class && isPrimaryType(resource, NT_PAGE)) {
      return (AdapterType)new MockPage(resource);
    }
    if (type == Template.class && isPrimaryType(resource, NT_TEMPLATE)) {
      return (AdapterType)new MockTemplate(resource);
    }
    if (type == Tag.class && isPrimaryType(resource, NT_TAG)) {
      return (AdapterType)new MockTag(resource);
    }
    if (type == ContentFragment.class && DamUtil.isAsset(resource)) {
      return (AdapterType)new MockContentFragment(resource);
    }
    if (type == ContentPolicy.class && resource.isResourceType(RT_CONTENTPOLICY)) {
      return (AdapterType)new MockContentPolicy(resource);
    }
    if (type == ContentPolicyMapping.class
        && (resource.isResourceType(RT_CONTENT_POLICY_MAPPING) || resource.isResourceType(RT_CONTENT_POLICY_MAPPINGS))
        && resource.getValueMap().containsKey(PN_POLICY)) {
      return (AdapterType)new MockContentPolicyMapping(resource);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private @Nullable <AdapterType> AdapterType getAdapter(@NotNull final ResourceResolver resolver, @NotNull final Class<AdapterType> type) {
    if (type == PageManager.class) {
      return (AdapterType)new MockPageManager(resolver);
    }
    if (type == ComponentManager.class) {
      return (AdapterType)new MockComponentManager(resolver);
    }
    if (type == TagManager.class) {
      return (AdapterType)new MockTagManager(resolver);
    }
    if (type == Designer.class) {
      return (AdapterType)new MockDesigner(resolver);
    }
    if (type == ContentPolicyManager.class) {
      return (AdapterType)new MockContentPolicyManager(resolver);
    }
    return null;
  }

  private boolean isPrimaryType(@NotNull final Resource resource, @NotNull final String primaryType) {
    Node node = resource.adaptTo(Node.class);
    if (node != null) {
      // JCR-based resource resolver
      try {
        return StringUtils.equals(node.getPrimaryNodeType().getName(), primaryType);
      }
      catch (RepositoryException ex) {
        // ignore
        return false;
      }
    }
    else {
      // sling resource resolver mock
      ValueMap props = resource.getValueMap();
      return StringUtils.equals(props.get(JcrConstants.JCR_PRIMARYTYPE, String.class), primaryType);
    }
  }

}
