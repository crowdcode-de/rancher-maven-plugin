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


import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christoph Schemmelmann (Crowdcode) created on 19.07.17.
 */

@Slf4j
@Mojo(defaultPhase = LifecyclePhase.PROCESS_SOURCES, name = "stack-deploy")
@Execute(phase = LifecyclePhase.PROCESS_SOURCES, goal = "stack-deploy")
public class RancherApiMojo extends AbstractMojo {

    private RestTemplate restTemplate;
    /**
     * Rancher app key
     */
    @Parameter(required = true)
    private String accessKey;
    /**
     * Rancher password
     */
    @Parameter(required = true)
    private String password;
    /**
     * Rancher url
     */
    @Parameter(required = true)
    private String url;
    /**
     * Envrionnment
     */
    @Parameter(required = true)
    private String environment;
    /**
     * Stack
     */
    @Parameter(property = "stack", required = true)
    private Stack stack;

    @Parameter
    private boolean skip = false;

    public RancherApiMojo() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    /**
     * Create basic auth header
     *
     * @return HttpHeaders instance
     */
    private HttpHeaders createBasicAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = accessKey + ":" + password;

        byte[] encodedAuth = Base64Utils.encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        headers.set("Authorization",authHeader);

        return headers;
    }

    /**
     * Get an environment (project) by name
     *
     * @return the api response body (should be json typed)
     */
    private String getEnvironment() {

        String envUrl = url + "/projects?name=" + environment;
        ResponseEntity<String> responseEntity = restTemplate.exchange(envUrl,HttpMethod.GET,new HttpEntity(createBasicAuthHeaders()),String.class);

        String responseBody = responseEntity.getBody();
        Assert.notNull(responseBody,"No http response body for url: " + envUrl);
        Assert.isTrue(!responseBody.isEmpty(),"No http response body for url: " + envUrl);

        return responseBody;
    }

    /**
     * Rancher plugin execution
     *
     * @throws MojoExecutionException maven plugin exception
     */
    public void execute() throws MojoExecutionException {
        if (skip)
          return;
        if (stack.getName().length()>63) {
        	stack.setName(stack.getName().substring(0,62));
        	log.warn("Stackname truncate: \"" + stack.getName() + "\"" );
        }
        Assert.notNull(stack,"stack not defined");
        if( stack.init(restTemplate,createBasicAuthHeaders(),getEnvironment(),environment) )
            stack.run();

    }
}
