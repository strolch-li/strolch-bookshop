package li.strolch.bookshop.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import li.strolch.privilege.model.Certificate;
import li.strolch.testbase.runtime.RuntimeMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BookshopStartTest {

	private static final String TARGET_PATH = "target/" + BookshopStartTest.class.getSimpleName();
	private static final String SOURCE_PATH = "src/test/resources/runtime";

	private static RuntimeMock runtimeMock;
	private static Certificate cert;

	@BeforeClass
	public static void beforeClass() {
		runtimeMock = new RuntimeMock().mockRuntime(TARGET_PATH, SOURCE_PATH);
		runtimeMock.startContainer();
		cert = runtimeMock.getPrivilegeHandler().authenticate("test", "test".toCharArray());
	}

	@AfterClass
	public static void afterClass(){
		runtimeMock.destroyRuntime();
	}

	@Test
	public void shouldStartApp() {

		assertNotNull(cert);
		assertEquals("test", cert.getUsername());
	}
}
