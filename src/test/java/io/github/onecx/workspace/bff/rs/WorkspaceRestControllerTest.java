package io.github.onecx.workspace.bff.rs;

import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.junit.QuarkusTest;
import org.mockserver.client.MockServerClient;

@QuarkusTest
public class WorkspaceRestControllerTest extends AbstractTest {
  @InjectMockServerClient
  MockServerClient mockServerClient;
}
