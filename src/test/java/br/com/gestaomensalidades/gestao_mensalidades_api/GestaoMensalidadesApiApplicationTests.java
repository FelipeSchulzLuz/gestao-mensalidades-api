package br.com.gestaomensalidades.gestao_mensalidades_api;

import br.com.gestaomensalidades.gestao_mensalidades_api.config.TestFirebaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestFirebaseConfig.class)
class GestaoMensalidadesApiApplicationTests {

	@Test
	void contextLoads() {
		// Test that Spring context loads successfully
	}

}
