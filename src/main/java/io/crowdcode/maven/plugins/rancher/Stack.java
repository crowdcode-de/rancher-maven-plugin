package io.crowdcode.maven.plugins.rancher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Schemmelmann (CROWDCODE) created on 24.07.17.
 */
@Slf4j
public class Stack extends StackModel {

    private static final class BadStateException extends RuntimeException {
        public BadStateException(String message) {
            super(message);
        }
    }

    String stacksUrl;
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private String url;

    public boolean init( RestTemplate restTemplate,HttpHeaders headers,String environmentResponse,String environment ) {
        this.restTemplate = restTemplate;
        this.headers = headers;
        ReadContext ctx = JsonPath.parse(environmentResponse);
        try {
            JSONArray data = ctx.read("data");
            if( data.size() != 0 ) {
                url = ctx.read("data[0].links.self");

                stacksUrl = ctx.read("data[0].links.stacks");

                ResponseEntity<String> responseEntity = restTemplate.exchange(stacksUrl + "?name=" + getName(),HttpMethod.GET,new HttpEntity(headers),String.class);

                ctx = JsonPath.parse(responseEntity.getBody());
                data = ctx.read("data");
                if( data.size() != 0 )
                    url = ctx.read("data[0].links.self");
                else
                    url = "";
            }
            else {
                log.error("access to environment {} not possible",environment);
                return false;
            }
        } catch( RuntimeException ex ) {
            log.info("The stack {} at environment {} does not exists {}",getName(),environment,ex);
        }
        return true;
    }

    /**
     * Remove a stack
     */
    private void removeStack() {
        if( url != null && !url.isEmpty() ) {
            String stackUrl = url + "?action=remove";

            log.info("About to delete the stack: {}",stackUrl);

            try {
                restTemplate.exchange(stackUrl,HttpMethod.POST,new HttpEntity(headers),String.class);
                log.info("Stack {} successfully deleted",stackUrl);
                url = "";
            } catch( RuntimeException ex ) {
                log.error("Error while remove stack",ex);
            }

        }
        else {
            log.info("Stack " + getName() + " does not exist!");
        }
    }


    /**
     * Read the a compose file (docker or rancher)
     *
     * @return the compose file content
     */
    private String readComposeFile( File composeFile ) {
        if( composeFile != null && composeFile.exists() && composeFile.canRead() ) {
            try {
                return new String(Files.readAllBytes(Paths.get(composeFile.toURI())));
            } catch( IOException ex ) {
                log.error("Error while reading the compose file: {}",composeFile.getAbsolutePath(),ex);
            }
        }
        return null;
    }

    /**
     * Create a stack
     */
    private void createStack() {

        //Read the docker compose file content
        String dockerComposeContent = readComposeFile(getDockerComposeFile());
        String rancherComposeContent = readComposeFile(getRancherComposeFile());
        ResponseEntity<String> responseEntity;

        Assert.notNull(dockerComposeContent,"dockerComposeContent "+getDockerComposeFile()+" can not be found");

        //Construct POST request payload
        Map<String, String> payload = new HashMap<>();
        payload.put("description",getDescription());
        payload.put("dockerCompose",dockerComposeContent);
        if( rancherComposeContent != null ) {
            payload.put("rancherCompose",rancherComposeContent);
        }
        payload.put("name",getName());
        payload.put("startOnCreate",getStartOnCreate());

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = null;

        try {
            entity = new HttpEntity(new ObjectMapper().writeValueAsString(payload),headers);
        } catch( JsonProcessingException ex ) {
            log.error("Error while parsing stack payload to json",ex);
        }

        log.info("About to create new stack with url: {} and payload: {}",stacksUrl,payload);

        //Perform http request

        try {
            responseEntity = restTemplate.exchange(stacksUrl,HttpMethod.POST,entity,String.class);
            ReadContext ctx = JsonPath.parse(responseEntity.getBody());
            url = ctx.read("links.self");
            log.info("New stack successfully created");
        } catch( RuntimeException ex ) {
            log.error("Error while parsing stack payload to json {}",ex);
            throw ex;
        }

    }

    /**
     * wait N milliseconds
     * 
     * @action ("wait:ms")
     */
    private void wait(String action) {
        if (action.matches("[0-9]*")) {
            long timeout = Long.parseLong(action);
            try {
                log.info("wait() waiting {} millis", timeout);
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
                log.error("Error while wait sleeping", ex);
            }
        } else {
            log.info("missing time uses wait:NNNN");
        }
    }

    private String verify() {
        if (url != null && !url.isEmpty()) {

            log.info("Verify the stack: {}",getName());

            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(url,HttpMethod.GET,new HttpEntity(headers),String.class);

                ReadContext ctx = JsonPath.parse(responseEntity.getBody());
                String state = ctx.read("state");
                log.info("State={}",state);
                return state;
            } catch(RuntimeException ex ) {
                log.error("Error while verifying stack",ex);
                throw ex;
            }

        }
        else {
            log.info("Stack " + getName() + " does not exist!");
        }
        return "";
    }

    /**
     * Verify a stack
     * 
     * @action ("verify:ms:attempts")
     */
    private void verifyStack(String action) {
    	int paramCount = 0;
    	String[] param = null;
    	if (! action.isEmpty()){
           param = action.split(":");
           paramCount = param.length;
    	}

        String state = "";
        int devider = 10;
        long timeout;
        long runtime;

        switch (paramCount) {
        case 0:
            state = verify();
            Assert.isTrue("active".equals(state), "Stack not at state active");
            return;
        case 2:
            devider = Integer.parseInt(param[1]);
            // no break
        case 1:
            runtime = Long.parseLong(param[0]);
            timeout = java.lang.System.currentTimeMillis() + runtime;
            break;
        default:
            log.error("Error while parsing verify parameters");
            throw (new IllegalArgumentException("Error while parsing verify parameters"));
        }
        long sleeptime = runtime / devider;
        while (!"active".equals(state) && java.lang.System.currentTimeMillis() < timeout) {
            try {
                log.info("verifyStack() waiting {} millis", sleeptime);
                Thread.sleep(sleeptime);
            } catch (InterruptedException ex) {
                log.error("Error while verify sleeping)", ex);
                throw new RuntimeException(ex);
            }
            state = verify();
        }
        if (! "active".equals(state))
            throw new BadStateException("Stack not at state active");
    }

    /**
     * run all actions a stack
     */
    public void run() {
        String[] actions = getActions().split(",");
        for( String action : actions ) {
            String a;
            String param = "";
            if( action.contains(":") ) {
                a = action.split(":")[ 0 ];
                param = action.split(":",2)[1];
            }
            else {
                a = action;
            }
            switch( a.toLowerCase() ) {
                case "remove":
                    removeStack();
                    break;
                case "wait":
                    wait(param);
                    break;
                case "create":
                    createStack();
                    break;
                case "verify":
                    verifyStack(param);
                    break;
                default:
                    log.error("Stack unknown action " + a);
            }
        }
    }
}
