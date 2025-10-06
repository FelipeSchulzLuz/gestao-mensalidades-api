package br.com.gestaomensalidades.gestao_mensalidades_api.config;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * Configuração de teste para Firebase.
 * 
 * Fornece um mock do FirebaseAuth para evitar a necessidade de
 * service account key em ambiente de testes.
 * 
 * @author Sistema de Gestão de Mensalidades
 */
@TestConfiguration
@Profile("test")
public class TestFirebaseConfig {

    /**
     * Fornece um mock do FirebaseAuth para testes.
     * 
     * @return mock do FirebaseAuth
     */
    @Bean
    @Primary
    public FirebaseAuth firebaseAuth() {
        return mock(FirebaseAuth.class);
    }
}
