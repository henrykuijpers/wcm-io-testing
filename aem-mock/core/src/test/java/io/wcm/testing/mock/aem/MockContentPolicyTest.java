/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static com.day.cq.commons.jcr.JcrConstants.JCR_DESCRIPTION;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LAST_MODIFIED_BY;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;

import io.wcm.testing.mock.aem.models.policy.ContentPolicyTestModel;
import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.policies.ContentPolicy;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockContentPolicyTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Test
  @SuppressWarnings("null")
  public void testProperties() {
    Calendar timestamp = Calendar.getInstance();
    Resource resource = context.create().resource("/content/test",
        JCR_LASTMODIFIED, timestamp,
        JCR_LAST_MODIFIED_BY, "user1",
        JCR_TITLE, "title1",
        JCR_DESCRIPTION, "description1",
        "prop1", "value1");
    ContentPolicy underTest = new MockContentPolicy(resource);

    assertEquals("value1", underTest.getProperties().get("prop1", String.class));
    assertEquals(timestamp.getTimeInMillis(), underTest.getLastModified().getTimeInMillis());
    assertEquals("user1", underTest.getLastModifiedBy());
    assertEquals("title1", underTest.getTitle());
    assertEquals("description1", underTest.getDescription());

    assertEquals("test", underTest.getName());
    assertEquals(resource.getPath(), underTest.getPath());
    assertNull(underTest.adaptTo(Object.class));
  }

  @Test
  public void testEmpty() {
    Resource resource = context.create().resource("/content/test");
    ContentPolicy underTest = new MockContentPolicy(resource);

    assertNull(underTest.getProperties().get("prop1", String.class));
    assertNull(underTest.getLastModified());
    assertNull(underTest.getLastModifiedBy());
  }

  @Test
  public void testAdaptToCustomSlingModel() {
    context.addModelsForClasses(ContentPolicyTestModel.class);
    Resource resource = context.create().resource("/content/test",
            "prop1", "value1",
            "prop2", true);
    ContentPolicy policy = new MockContentPolicy(resource);
    ContentPolicyTestModel underTest = policy.adaptTo(ContentPolicyTestModel.class);

    assertNotNull(underTest);
    assertEquals("value1", underTest.getProp1());
    assertEquals(true, underTest.getProp2());
  }

}
