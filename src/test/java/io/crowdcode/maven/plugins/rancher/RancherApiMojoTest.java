package io.crowdcode.maven.plugins.rancher;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

/**
 * @author Christoph Schemmelmann (Crowdcode) created on 20.07.17.
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

@Slf4j
public class RancherApiMojoTest extends AbstractMojoTestCase {

    @Rule
    public MojoRule rule = new MojoRule();

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testValidPom() throws Exception {

        File pom = new File(getBasedir(), "src/test/resources/pom.xml");
        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        RancherApiMojo mojo = (RancherApiMojo) lookupMojo("stack-deploy", pom);
        Assert.assertNotNull(mojo);

        mojo.execute();
    }
}
