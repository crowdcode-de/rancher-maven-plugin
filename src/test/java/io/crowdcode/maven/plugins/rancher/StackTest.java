package io.crowdcode.maven.plugins.rancher;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StackTest {

    private static String stacksUrl = "…/v2-beta/projects/1a5/stacks";
    private static String stackUrl = "…/v2-beta/projects/1a5/stacks/1st257";
    private static HttpHeaders headers = new HttpHeaders();
    private static URL enviromentResponceUrl = Resources.getResource("EnviromentResponce.json");
    private static URL stackResponceUrl = Resources.getResource("StackResponce.json");
    private static URL stackActiveUrl = Resources.getResource("StackActive.json");
    private static URL stackRemovedUrl = Resources.getResource("StackRemoved.json");

    @Mock
    private RestTemplate restTemplate;

    private static Stack defaultStack() {
        Stack stack = new Stack();
        stack.setName("TestStack")
                .setDescription("Test Rancher Plugin")
                .setDockerComposeFile(new File("src/main/resources/docker-compose.yml"));
        return stack;
    }

    @Test
    public void RunWaitTest() {
        Stack s = defaultStack();
        s.setActions("wait:100");
        long time = System.currentTimeMillis();
        s.run();
        Assert.assertTrue(time + 100 < System.currentTimeMillis());
    }

    @Test
    public void RunVerifyoParameterTest() {
        try {
            String environmentResponse = Resources.toString(enviromentResponceUrl,Charsets.UTF_8);
            String stackResponce = Resources.toString(stackResponceUrl,Charsets.UTF_8);
            String stackActive = Resources.toString(stackActiveUrl,Charsets.UTF_8);
            ResponseEntity<String> stackResponceEntity = new ResponseEntity<String>(stackResponce,HttpStatus.ACCEPTED);
            ResponseEntity<String> sstackActiveEntity = new ResponseEntity<String>(stackActive,HttpStatus.ACCEPTED);
            Stack s = defaultStack();
            Mockito.when(restTemplate.exchange(
                    Matchers.eq(stacksUrl + "?name=" + s.getName()),
                    Matchers.eq(HttpMethod.GET),
                    Matchers.<HttpEntity<String>> any(),
                    Matchers.<Class<String>> any())
            ).thenReturn(stackResponceEntity);
            Mockito.when(restTemplate.exchange(
                    Matchers.eq(stackUrl),
                    Matchers.eq(HttpMethod.GET),
                    Matchers.<HttpEntity<String>> any(),
                    Matchers.<Class<String>> any())
            ).thenReturn(sstackActiveEntity);
            s.init(restTemplate,headers,environmentResponse,"default");
            s.setActions("verify");
            s.run();
            s.setActions("verify:2000");
            s.run();
            s.setActions("verify:2000:20");
            s.run();
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

    @Test(expected = RuntimeException.class)
    public void RunVerifyoFailtStackTest() {
        try {
            String environmentResponse = Resources.toString(enviromentResponceUrl,Charsets.UTF_8);
            String stackResponce = Resources.toString(stackResponceUrl,Charsets.UTF_8);
            String stackRemoved = Resources.toString(stackRemovedUrl,Charsets.UTF_8);
            ResponseEntity<String> stackResponceEntity = new ResponseEntity<String>(stackResponce,HttpStatus.ACCEPTED);
            ResponseEntity<String> sstackActiveEntity = new ResponseEntity<String>(stackRemoved,HttpStatus.ACCEPTED);
            Stack s = defaultStack();
            Mockito.when(restTemplate.exchange(
                    Matchers.eq(stacksUrl + "?name=" + s.getName()),
                    Matchers.eq(HttpMethod.GET),
                    Matchers.<HttpEntity<String>> any(),
                    Matchers.<Class<String>> any())
            ).thenReturn(stackResponceEntity);
            Mockito.when(restTemplate.exchange(
                    Matchers.eq(stackUrl),
                    Matchers.eq(HttpMethod.GET),
                    Matchers.<HttpEntity<String>> any(),
                    Matchers.<Class<String>> any())
            ).thenReturn(sstackActiveEntity);
            s.init(restTemplate,headers,environmentResponse,"default");
            s.setActions("verify");
            s.run();
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

}
