package io.crowdcode.maven.plugins.rancher;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StackTest {

    private static String stacksUrl = "…/v2-beta/projects/1a5/stacks";
    private static String stackUrl = "…/v2-beta/projects/1a5/stacks/1st257";
    private static HttpHeaders headers = new HttpHeaders();
    private static URL enviromentResponseUrl = Resources.getResource("EnviromentResponce.json");
    private static URL stackResponseUrl = Resources.getResource("StackResponce.json");
    private static URL stackActiveUrl = Resources.getResource("StackActive.json");
    private static URL stackRemovedUrl = Resources.getResource("StackRemoved.json");

    private boolean init;
    private int verify;

    @Before
    public void before(){
        init = false;
        verify = 0;
    }

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
    public void runWaitTest() {
        Stack s = defaultStack();
        s.setActions("wait:100");
        long time = System.currentTimeMillis();
        s.run();
        assertTrue(time + 100 <= System.currentTimeMillis());
    }

    @Test
    public void runVerifyParameterTest() {
        try {
            String environmentResponse = Resources.toString(enviromentResponseUrl,Charsets.UTF_8);
            String stackResponse = Resources.toString(stackResponseUrl,Charsets.UTF_8);
            String stackActive = Resources.toString(stackActiveUrl,Charsets.UTF_8);
            final ResponseEntity<String> stackResponseEntity = new ResponseEntity<String>(stackResponse,HttpStatus.ACCEPTED);
            final ResponseEntity<String> stackActiveEntity = new ResponseEntity<String>(stackActive,HttpStatus.ACCEPTED);
            Stack s = defaultStack();

            Answer<ResponseEntity<String>> answer = invocation -> {
                init = true;
                return stackResponseEntity;
            };

            doAnswer(answer).when(restTemplate).exchange(
                    Matchers.eq(stacksUrl + "?name=" + s.getName()),
                    Matchers.eq(HttpMethod.GET),
                    Matchers.<HttpEntity<String>> any(),
                    Matchers.<Class<String>> any());


            Answer<ResponseEntity<String>> verifyAnswer = invocation -> {
                verify++;
                return stackActiveEntity;
            };


            doAnswer(verifyAnswer).when(restTemplate).exchange(
                    Matchers.eq(stackUrl),
                    Matchers.eq(HttpMethod.GET),
                    Matchers.<HttpEntity<String>> any(),
                    Matchers.<Class<String>> any());

            s.init(restTemplate,headers,environmentResponse,"default");
            assertTrue(init);

            s.setActions("verify");
            s.run();

            assertEquals(1, verify);
            s.setActions("verify:2000");
            s.run();
            assertEquals(2, verify);

            s.setActions("verify:2000:20");
            s.run();
            assertEquals(3, verify);

        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

    @Test(expected = RuntimeException.class)
    public void runVerifyFailtStackTest() {
        try {
            String environmentResponse = Resources.toString(enviromentResponseUrl,Charsets.UTF_8);
            String stackResponce = Resources.toString(stackResponseUrl,Charsets.UTF_8);
            String stackRemoved = Resources.toString(stackRemovedUrl,Charsets.UTF_8);
            ResponseEntity<String> stackResponceEntity = new ResponseEntity<String>(stackResponce,HttpStatus.ACCEPTED);
            ResponseEntity<String> sstackActiveEntity = new ResponseEntity<String>(stackRemoved,HttpStatus.ACCEPTED);
            Stack s = defaultStack();
            when(restTemplate.exchange(
                    Matchers.eq(stacksUrl + "?name=" + s.getName()),
                    Matchers.eq(HttpMethod.GET),
                    Matchers.<HttpEntity<String>> any(),
                    Matchers.<Class<String>> any())
            ).thenReturn(stackResponceEntity);
            when(restTemplate.exchange(
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
