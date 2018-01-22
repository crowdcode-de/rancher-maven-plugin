package io.crowdcode.maven.plugins.rancher;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StackTest {

	@Mock
	private RestTemplate restTemplate;

	private static Stack defaultStack() {
		Stack stack = new Stack();
		stack.setName("StackName")
		     .setDescription("Stackdiscriptions")
		     .setDockerComposeFile(new File("src/main/resources/docker-compose.yml"));
		return stack;

	}

	// @InjectMocks
	// private SomeService underTest;
	//
	// @Test
	// public void testGetObjectAList() {
	// ObjectA myobjectA = new ObjectA();
	// //define the entity you want the exchange to return
	// ResponseEntity<List<ObjectA>> myEntity = new
	// ResponseEntity<List<ObjectA>>(HttpStatus.ACCEPTED);
	// Mockito.when(restTemplate.exchange(
	// Matchers.eq("/objects/get-objectA"),
	// Matchers.eq(HttpMethod.POST),
	// Matchers.<HttpEntity<List<ObjectA>>>any(),
	// Matchers.<ParameterizedTypeReference<List<ObjectA>>>any())
	// ).thenReturn(myEntity);
	//
	// List<ObjectA> res = underTest.getListofObjectsA();
	// Assert.assertEquals(myobjectA, res.get(0));
	// }

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
		Stack s = defaultStack();
		s.setActions("verify");
		s.run();
		s.setActions("verify:2000");
		s.run();
	}

}
