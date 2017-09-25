package io.crowdcode.maven.plugins.rancher;

import lombok.Getter;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.File;


/**
 * @author Christoph Schemmelmann (Crowdcode) created on 24.07.17.
 */
@Getter
public class StackModel {

    /**
     * Stack name
     */
    @Parameter(required = true)
    private String name;

    /**
     * Stack description
     */
    @Parameter(required = true)
    private String description;

    /**
     * Stack start on create
     */
    @Parameter
    private String startOnCreate;

    /**
     * Location of the docker composer file.
     */
    @Parameter(required = true)
    private File dockerComposeFile;

    /**
     * Location of the rancher composer file.
     */
    @Parameter
    private File rancherComposeFile;

    /**
     * actions
     */
    @Parameter(required = true)
    private String actions;

    public String getStartOnCreate(){
        return (startOnCreate == null)? "true":startOnCreate;
    }

}
