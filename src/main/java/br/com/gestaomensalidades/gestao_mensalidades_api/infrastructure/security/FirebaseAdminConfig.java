package br.com.gestaomensalidades.gestao_mensalidades_api.infrastructure.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuração do Firebase Admin SDK para autenticação híbrida.
 * 
 * Esta classe inicializa o Firebase Admin SDK usando a service account key
 * e disponibiliza o FirebaseAuth como bean do Spring para validação de tokens.
 * 
 * Segurança:
 * - Service account key deve estar fora do repositório (.gitignore)
 * - Para desenvolvimento: coloque em src/main/resources/
 * - Para produção: use variável de ambiente ou secret manager
 * 
 * @author Sistema de Gestão de Mensalidades
 * @since 2025-10-05
 */
@Slf4j
@Configuration
@Profile("!test")
public class FirebaseAdminConfig {

    /**
     * Caminho da service account key do Firebase.
     * Pode ser configurado via application.yml ou variável de ambiente.
     * 
     * Exemplo .env:
     * FIREBASE_PRIVATE_KEY_PATH=subscriptions-dev-fsl-firebase-adminsdk-fbsvc-d43f1fdcdb.json
     */
    @Value("${firebase.service-account-key:subscriptions-dev-fsl-firebase-adminsdk-fbsvc-d43f1fdcdb.json}")
    private String firebasePrivateKeyPath;

    /**
     * Project ID do Firebase (opcional, extraído automaticamente da service account key)
     */
    @Value("${firebase.project-id:#{null}}")
    private String firebaseProjectId;

    /**
     * Inicializa o Firebase Admin SDK.
     * 
     * O método verifica se já existe uma instância do FirebaseApp para evitar
     * inicialização dupla. Isso é importante em ambientes com hot-reload.
     * 
     * Se a service account key não for encontrada, loga um warning mas NÃO quebra a aplicação.
     * Isso permite desenvolvimento sem Firebase configurado (usando apenas JWT).
     */
    @PostConstruct
    public void initializeFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("Inicializando Firebase Admin SDK...");
            
            try {
                InputStream serviceAccount = getServiceAccountStream();
                
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount));
                
                // Adicionar project ID se configurado explicitamente
                if (firebaseProjectId != null && !firebaseProjectId.isEmpty()) {
                    optionsBuilder.setProjectId(firebaseProjectId);
                }
                
                FirebaseOptions options = optionsBuilder.build();
                FirebaseApp.initializeApp(options);
                
                log.info("✅ Firebase Admin SDK inicializado com sucesso!");
                log.info("Project ID: {}", FirebaseApp.getInstance().getOptions().getProjectId());
            } catch (IOException e) {
                log.warn("⚠️  Firebase Admin SDK não pôde ser inicializado: {}", e.getMessage());
                log.warn("⚠️  Firebase Authentication não estará disponível.");
                log.warn("⚠️  A aplicação funcionará apenas com autenticação JWT.");
                log.info("💡 Para habilitar Firebase, adicione o arquivo: {}", firebasePrivateKeyPath);
            }
        } else {
            log.info("Firebase Admin SDK já estava inicializado.");
        }
    }

    /**
     * Obtém o stream da service account key.
     * 
     * Tenta primeiro carregar do classpath (recursos), depois do filesystem.
     * Isso permite flexibilidade entre ambientes de desenvolvimento e produção.
     * 
     * @return InputStream da service account key
     * @throws IOException se o arquivo não for encontrado
     */
    private InputStream getServiceAccountStream() throws IOException {
        try {
            // Opção 1: Carregar do classpath (src/main/resources)
            log.debug("Tentando carregar service account key do classpath: {}", firebasePrivateKeyPath);
            return new ClassPathResource(firebasePrivateKeyPath).getInputStream();
        } catch (IOException e) {
            // Opção 2: Carregar do filesystem (caminho absoluto)
            log.debug("Não encontrado no classpath, tentando filesystem: {}", firebasePrivateKeyPath);
            return new FileInputStream(firebasePrivateKeyPath);
        }
    }

    /**
     * Disponibiliza FirebaseAuth como bean do Spring.
     * 
     * Este bean pode ser injetado em qualquer componente Spring para
     * validação de tokens Firebase ID.
     * 
     * Retorna null se o Firebase não estiver inicializado (para evitar quebrar a aplicação).
     * 
     * @return instância do FirebaseAuth ou null se Firebase não estiver disponível
     */
    @Bean
    public FirebaseAuth firebaseAuth() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseAuth.getInstance();
            } else {
                log.warn("FirebaseAuth bean criado como null - Firebase não está inicializado");
                return null;
            }
        } catch (Exception e) {
            log.warn("Não foi possível criar FirebaseAuth bean: {}", e.getMessage());
            return null;
        }
    }
}
