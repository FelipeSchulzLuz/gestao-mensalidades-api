# 🔐 Autenticação Híbrida - JWT + Firebase

**Versão:** 1.0  
**Data:** 2025-10-05  
**Arquitetura:** Sistema Híbrido com Multi-Tenancy

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura](#arquitetura)
3. [Fluxos de Autenticação](#fluxos-de-autenticação)
4. [Multi-Tenancy](#multi-tenancy)
5. [Implementação](#implementação)
6. [Configuração](#configuração)
7. [Endpoints](#endpoints)
8. [Segurança](#segurança)
9. [Testes](#testes)
10. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral

Este sistema implementa **autenticação híbrida**, permitindo que usuários façam login de duas formas:

### 1. **JWT (Email/Senha)**
- Usuários tradicionais com email e senha
- Token JWT gerado pelo backend (HS512, 24h)
- Ideal para: Sistemas corporativos, aplicações internas

### 2. **Firebase Authentication (Login Social)**
- Login via Google, Facebook, Apple, etc.
- Token Firebase ID validado pelo Firebase Admin SDK
- Ideal para: Apps mobile, SaaS, consumer apps

### Por que Híbrido?

✅ **Flexibilidade:** Usuários escolhem método preferido  
✅ **Zero Waste:** Mantém sistema JWT existente funcional  
✅ **Detecção Automática:** Backend detecta tipo de token automaticamente  
✅ **Multi-Tenancy:** Ambos métodos suportam isolamento por organização  
✅ **Escalabilidade:** Firebase gerencia OAuth flows complexos  

---

## 🏗️ Arquitetura

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (React/Vue/Mobile)               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────┐           ┌──────────────────┐      │
│  │  Login Email     │           │ Login Social     │      │
│  │  /auth/login     │           │ Firebase SDK     │      │
│  │  (recebe JWT)    │           │ (recebe Firebase)│      │
│  └────────┬─────────┘           └─────────┬────────┘      │
│           │                               │                │
│           └───────────────┬───────────────┘                │
│                           │                                │
│                           │ Authorization: Bearer <TOKEN>  │
│                           ▼                                │
└───────────────────────────┼────────────────────────────────┘
                            │
┌───────────────────────────┼────────────────────────────────┐
│                    BACKEND (Spring Boot)                    │
├───────────────────────────┼────────────────────────────────┤
│                           ▼                                │
│          ┌────────────────────────────┐                   │
│          │ UnifiedAuthenticationFilter │                   │
│          │  1. Extrai token do header  │                   │
│          │  2. Detecta tipo (JWT/FB)   │                   │
│          │  3. Delega validação        │                   │
│          └────────────┬───────────────┘                   │
│                       │                                    │
│          ┌────────────┴───────────────┐                   │
│          │                            │                   │
│          ▼                            ▼                   │
│  ┌──────────────────┐    ┌──────────────────┐           │
│  │  JwtTokenProvider│    │FirebaseTokenProv.│           │
│  │  - validateToken │    │ - validateToken  │           │
│  │  - getUserEmail  │    │ - getUid         │           │
│  │  - getRole       │    │ - getEmail       │           │
│  └────────┬─────────┘    └─────────┬────────┘           │
│           │                         │                    │
│           └──────────┬──────────────┘                    │
│                      ▼                                    │
│          ┌───────────────────────────┐                   │
│          │   SecurityContextHolder   │                   │
│          │   username = email | uid  │                   │
│          │   authorities = [roles]   │                   │
│          └────────────┬──────────────┘                   │
│                       │                                   │
│                       ▼                                   │
│              ┌───────────────────────┐                   │
│              │     UserService       │                   │
│              │  getCurrentUser()     │                   │
│              │  (busca por email OU  │                   │
│              │   firebaseUid)        │                   │
│              └───────────┬───────────┘                   │
│                          │                               │
│                          ▼                               │
│         ┌────────────────────────────────┐              │
│         │  getCurrentOrganizationId()    │              │
│         │  ← Multi-Tenancy Enforcement   │              │
│         └────────────┬───────────────────┘              │
│                      │                                   │
│                      ▼                                   │
│              ┌───────────────────────┐                  │
│              │  LÓGICA DE NEGÓCIO    │                  │
│              │  (isolada por org)    │                  │
│              └───────────────────────┘                  │
└─────────────────────────────────────────────────────────┘
```

### Detecção de Token

**UnifiedAuthenticationFilter** detecta tipo automaticamente:

```java
private AuthType detectAuthType(String token) {
    // JWT: Formato header.payload.signature (3 partes)
    if (token.split("\\.").length == 3 && jwtTokenProvider.validateToken(token)) {
        return AuthType.JWT;
    } 
    // Firebase: Formato diferente
    else {
        return AuthType.FIREBASE;
    }
}
```

---

## 🔄 Fluxos de Autenticação

### Fluxo 1: Login com Email/Senha (JWT)

```mermaid
sequenceDiagram
    participant U as Usuário
    participant F as Frontend
    participant B as Backend
    participant DB as Database

    U->>F: Preenche email/senha
    F->>B: POST /api/auth/login
    B->>DB: Busca usuário por email
    B->>B: Valida senha (BCrypt)
    B->>B: Gera JWT (HS512, 24h)
    B->>F: Retorna { token, user }
    F->>F: Armazena JWT em localStorage
    F->>B: GET /api/plans (Authorization: Bearer <JWT>)
    B->>B: UnifiedAuthenticationFilter detecta JWT
    B->>B: JwtTokenProvider.validateToken()
    B->>B: Popula SecurityContextHolder
    B->>F: Retorna dados filtrados por organizationId
```

### Fluxo 2: Login com Google (Firebase)

```mermaid
sequenceDiagram
    participant U as Usuário
    participant F as Frontend
    participant FB as Firebase Auth
    participant B as Backend
    participant DB as Database

    U->>F: Clica "Login com Google"
    F->>FB: signInWithPopup(googleProvider)
    FB->>U: Redireciona para tela Google OAuth
    U->>FB: Autoriza acesso
    FB->>F: Retorna FirebaseUser + ID Token
    F->>B: POST /api/auth/firebase-login { firebaseToken }
    B->>FB: Firebase Admin SDK valida token
    FB->>B: Retorna FirebaseToken (uid, email, name)
    B->>DB: Busca usuário por firebaseUid
    B->>F: Retorna { user }
    F->>F: Armazena Firebase token (auto-renovado)
    F->>B: GET /api/plans (Authorization: Bearer <FIREBASE_TOKEN>)
    B->>B: UnifiedAuthenticationFilter detecta Firebase
    B->>B: FirebaseTokenProvider.validateToken()
    B->>B: Popula SecurityContextHolder
    B->>F: Retorna dados filtrados por organizationId
```

### Fluxo 3: Registro com Firebase (Primeiro Acesso)

```mermaid
sequenceDiagram
    participant U as Usuário
    participant F as Frontend
    participant FB as Firebase Auth
    participant B as Backend
    participant DB as Database

    U->>F: Clica "Login com Google"
    F->>FB: signInWithPopup(googleProvider)
    FB->>F: Retorna FirebaseUser + ID Token
    F->>B: POST /api/auth/firebase-login { firebaseToken }
    B->>DB: Busca usuário por firebaseUid
    DB->>B: Usuário não encontrado (404)
    B->>F: Retorna 404 "Usuário não registrado"
    F->>F: Detecta usuário novo
    F->>F: Exibe formulário: "Selecione sua organização"
    U->>F: Seleciona organizationId
    F->>B: POST /api/auth/firebase-register { firebaseToken, organizationId }
    B->>FB: Valida token
    B->>DB: Cria usuário (firebaseUid, email, name, organizationId)
    B->>F: Retorna { user }
    F->>F: Redireciona para dashboard
```

---

## 🏢 Multi-Tenancy

### Conceito

**Multi-tenancy** = Múltiplas organizações no mesmo banco de dados, com **isolamento total** de dados.

### Implementação

Cada `User` tem um `organizationId`:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;  // ← Chave para isolamento
    
    // ... outros campos
}
```

### UserService - Single Source of Truth

```java
@Service
public class UserService {
    
    /**
     * ⚠️ CRÍTICO: Use SEMPRE em queries
     * Retorna organizationId do usuário autenticado (JWT ou Firebase)
     */
    public Long getCurrentOrganizationId() {
        User user = getCurrentUser();
        return user.getOrganizationId();
    }
    
    /**
     * Funciona para JWT e Firebase
     */
    public User getCurrentUser() {
        String identifier = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();  // email (JWT) ou uid (Firebase)
        
        // Busca por email OU firebaseUid
        return userRepository
            .findByEmailOrFirebaseUid(identifier, identifier)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
```

### Padrão de Uso ✅

```java
@GetMapping("/plans")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<List<Plan>> getPlans() {
    // ✅ CORRETO: Obter organizationId do usuário autenticado
    Long orgId = userService.getCurrentOrganizationId();
    
    // ✅ CORRETO: Filtrar por organizationId
    List<Plan> plans = planRepository.findByOrganizationId(orgId);
    
    return ResponseEntity.ok(plans);
}
```

### Anti-Padrão ❌

```java
@GetMapping("/plans")
public ResponseEntity<List<Plan>> getPlans(
    @RequestParam Long organizationId  // ❌ NUNCA!
) {
    // ❌ ERRO: Usuário pode enviar organizationId de outra organização!
    List<Plan> plans = planRepository.findByOrganizationId(organizationId);
    return ResponseEntity.ok(plans);
}
```

### Validação de Acesso

Para recursos específicos (ex: `/api/plans/{id}`):

```java
@GetMapping("/plans/{id}")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<Plan> getPlan(@PathVariable Long id) {
    Plan plan = planRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado"));
    
    // ✅ VALIDAR: Plano pertence à organização do usuário
    userService.validateOrganizationAccess(
        plan.getOrganizationId(), 
        "Plano"
    );
    
    return ResponseEntity.ok(plan);
}
```

---

## 💻 Implementação

### Componentes Principais

#### 1. FirebaseAdminConfig

**Localização:** `infrastructure/security/FirebaseAdminConfig.java`

```java
@Configuration
public class FirebaseAdminConfig {
    
    @Value("${firebase.service-account-key:serviceAccountKey.json}")
    private String serviceAccountKeyPath;
    
    @PostConstruct
    public void initialize() throws IOException {
        FileInputStream serviceAccount = 
            new FileInputStream(serviceAccountKeyPath);
        
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
        
        FirebaseApp.initializeApp(options);
    }
    
    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
```

#### 2. FirebaseTokenProvider

**Localização:** `infrastructure/security/FirebaseTokenProvider.java`

```java
@Component
public class FirebaseTokenProvider {
    
    @Autowired
    private FirebaseAuth firebaseAuth;
    
    public FirebaseToken validateToken(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            // Trata: EXPIRED_ID_TOKEN, INVALID_ID_TOKEN, REVOKED_ID_TOKEN
            throw new IllegalArgumentException("Token Firebase inválido");
        }
    }
    
    public String getUidFromToken(String idToken) {
        return validateToken(idToken).getUid();
    }
    
    public String getEmailFromToken(String idToken) {
        return validateToken(idToken).getEmail();
    }
}
```

#### 3. UnifiedAuthenticationFilter

**Localização:** `infrastructure/security/UnifiedAuthenticationFilter.java`

```java
@Component
public class UnifiedAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private FirebaseTokenProvider firebaseTokenProvider;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null) {
            AuthType authType = detectAuthType(token);
            
            switch (authType) {
                case JWT:
                    authenticateWithJwt(token);
                    break;
                case FIREBASE:
                    authenticateWithFirebase(token);
                    break;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private AuthType detectAuthType(String token) {
        if (token.split("\\.").length == 3 && 
            jwtTokenProvider.validateToken(token)) {
            return AuthType.JWT;
        }
        return AuthType.FIREBASE;
    }
    
    private void authenticateWithJwt(String token) {
        String email = jwtTokenProvider.getUsernameFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);
        
        UserDetails userDetails = User.builder()
            .username(email)
            .password("")
            .authorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
            ))
            .build();
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
        
        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
    
    private void authenticateWithFirebase(String token) {
        FirebaseToken decodedToken = 
            firebaseTokenProvider.validateToken(token);
        
        String uid = decodedToken.getUid();
        boolean isAdmin = firebaseTokenProvider.isAdmin(token);
        String role = isAdmin ? "ADMIN" : "USER";
        
        UserDetails userDetails = User.builder()
            .username(uid)  // UID como username
            .password("")
            .authorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
            ))
            .build();
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
        
        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
}
```

#### 4. UserService

**Localização:** `application/services/UserService.java`

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Retorna usuário autenticado (JWT ou Firebase)
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder
            .getContext()
            .getAuthentication();
        
        String identifier = auth.getName();  // email ou uid
        
        return userRepository
            .findByEmailOrFirebaseUid(identifier, identifier)
            .orElseThrow(() -> 
                new RuntimeException("Usuário não encontrado"));
    }
    
    /**
     * ⚠️ CRÍTICO: Use em TODAS as queries
     */
    public Long getCurrentOrganizationId() {
        return getCurrentUser().getOrganizationId();
    }
    
    /**
     * Valida se usuário tem acesso ao recurso
     */
    public void validateOrganizationAccess(
        Long resourceOrgId, 
        String resourceName
    ) {
        Long userOrgId = getCurrentOrganizationId();
        
        if (!userOrgId.equals(resourceOrgId)) {
            throw new AccessDeniedException(
                "Acesso negado ao " + resourceName
            );
        }
    }
}
```

---

## ⚙️ Configuração

### 1. Firebase Console

1. Acesse: https://console.firebase.google.com/
2. Crie projeto ou use existente
3. Ative **Authentication** > **Sign-in method**
4. Habilite providers (Google, Facebook, etc.)
5. Adicione domínios autorizados (ex: localhost, seu-dominio.com)

### 2. Service Account Key

1. **Firebase Console** > **Project Settings** > **Service Accounts**
2. Clique em **"Generate New Private Key"**
3. Baixe o arquivo JSON
4. Salve em: `src/main/resources/serviceAccountKey.json`
5. ⚠️ **IMPORTANTE:** Adicione ao `.gitignore`:

```gitignore
# Firebase
src/main/resources/serviceAccountKey.json
src/main/resources/firebase-*.json
```

### 3. Variáveis de Ambiente

**`.env`:**

```env
# Firebase
FIREBASE_PROJECT_ID=meu-projeto-123
FIREBASE_SERVICE_ACCOUNT_KEY_PATH=serviceAccountKey.json
```

**`application.yml`:**

```yaml
firebase:
  project-id: ${FIREBASE_PROJECT_ID}
  service-account-key: ${FIREBASE_SERVICE_ACCOUNT_KEY_PATH:serviceAccountKey.json}
```

### 4. Frontend - Firebase SDK

**Instalar:**

```bash
npm install firebase
```

**Configurar (`firebaseConfig.js`):**

```javascript
import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "AIza...",  // Firebase Console > Project Settings
  authDomain: "meu-projeto.firebaseapp.com",
  projectId: "meu-projeto-123",
  // ...
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();
```

**Login (`LoginPage.jsx`):**

```javascript
import { signInWithPopup } from 'firebase/auth';
import { auth, googleProvider } from './firebaseConfig';

async function handleGoogleLogin() {
  try {
    // 1. Login via Firebase SDK
    const result = await signInWithPopup(auth, googleProvider);
    const user = result.user;
    
    // 2. Obter ID token
    const idToken = await user.getIdToken();
    
    // 3. Enviar para backend
    const response = await fetch('/api/auth/firebase-login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ firebaseToken: idToken })
    });
    
    if (response.status === 404) {
      // Usuário não registrado - redirecionar para registro
      navigate('/register', { state: { idToken, user } });
      return;
    }
    
    const data = await response.json();
    
    // 4. Armazenar token
    localStorage.setItem('firebaseToken', idToken);
    localStorage.setItem('user', JSON.stringify(data.user));
    
    // 5. Redirecionar
    navigate('/dashboard');
    
  } catch (error) {
    console.error('Erro no login:', error);
  }
}
```

**Registro (`RegisterPage.jsx`):**

```javascript
async function handleRegister(organizationId) {
  const { idToken, user } = location.state;  // Do estado da navegação
  
  const response = await fetch('/api/auth/firebase-register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ 
      firebaseToken: idToken,
      organizationId: organizationId  // Selecionado pelo usuário
    })
  });
  
  const data = await response.json();
  
  localStorage.setItem('firebaseToken', idToken);
  localStorage.setItem('user', JSON.stringify(data.user));
  
  navigate('/dashboard');
}
```

**Requisições Autenticadas:**

```javascript
// Interceptor Axios
axios.interceptors.request.use(async (config) => {
  const firebaseToken = localStorage.getItem('firebaseToken');
  
  if (firebaseToken) {
    // Firebase SDK renova automaticamente se expirado
    const currentUser = auth.currentUser;
    if (currentUser) {
      const freshToken = await currentUser.getIdToken(true);
      localStorage.setItem('firebaseToken', freshToken);
      config.headers.Authorization = `Bearer ${freshToken}`;
    }
  }
  
  return config;
});
```

---

## 🔌 Endpoints

### Autenticação JWT

**POST /api/auth/register**

```json
// Request
{
  "email": "joao@empresa.com",
  "password": "senha123",
  "name": "João Silva",
  "organizationId": 1
}

// Response 201
{
  "message": "Usuário registrado com sucesso",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**POST /api/auth/login**

```json
// Request
{
  "email": "joao@empresa.com",
  "password": "senha123"
}

// Response 200
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "joao@empresa.com",
    "name": "João Silva",
    "role": "USER",
    "organizationId": 1
  }
}
```

### Autenticação Firebase

**POST /api/auth/firebase-register**

```json
// Request
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "organizationId": 1
}

// Response 201
{
  "message": "Usuário registrado com sucesso via Firebase",
  "user": {
    "id": 2,
    "email": "maria@gmail.com",
    "name": "Maria Santos",
    "firebaseUid": "abc123xyz",
    "organizationId": 1,
    "role": "USER",
    "authMethod": "FIREBASE"
  }
}
```

**POST /api/auth/firebase-login**

```json
// Request
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}

// Response 200
{
  "message": "Login via Firebase realizado com sucesso",
  "user": {
    "id": 2,
    "email": "maria@gmail.com",
    "name": "Maria Santos",
    "firebaseUid": "abc123xyz",
    "organizationId": 1,
    "role": "USER",
    "authMethod": "FIREBASE"
  }
}

// Response 404 (usuário não registrado)
{
  "error": "Usuário não registrado. Use /firebase-register primeiro."
}
```

**POST /api/auth/firebase-verify** (Debug)

```json
// Request
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}

// Response 200
{
  "valid": true,
  "uid": "abc123xyz",
  "email": "maria@gmail.com",
  "name": "Maria Santos",
  "emailVerified": true,
  "registeredInDatabase": true,
  "userId": 2,
  "organizationId": 1
}
```

---

## 🔒 Segurança

### Checklist de Segurança

#### ✅ Backend

- [ ] Service account key **NUNCA** commitado no Git
- [ ] Service account key em `.gitignore`
- [ ] Validação de token Firebase em TODAS as requisições
- [ ] `getCurrentOrganizationId()` usado em TODAS as queries
- [ ] Validação de `organizationId` em criação de recursos
- [ ] `@PreAuthorize` em endpoints administrativos
- [ ] HTTPS obrigatório em produção
- [ ] CORS configurado corretamente (sem `*` em prod)

#### ✅ Frontend

- [ ] Firebase Config **NÃO é secreto** (pode ser público)
- [ ] Tokens armazenados em `localStorage` (ou `sessionStorage`)
- [ ] Token renovado automaticamente antes de expirar
- [ ] Logout limpa `localStorage` E chama Firebase `signOut()`
- [ ] Redirect para login se token inválido (401)

### Boas Práticas

**1. Nunca confie em dados do cliente**

```java
// ❌ ERRADO
@PostMapping("/plans")
public Plan createPlan(@RequestBody Plan plan) {
    // Usuário pode enviar organizationId diferente!
    return planRepository.save(plan);
}

// ✅ CORRETO
@PostMapping("/plans")
public Plan createPlan(@RequestBody Plan plan) {
    // Força organizationId do usuário autenticado
    Long orgId = userService.getCurrentOrganizationId();
    plan.setOrganizationId(orgId);
    return planRepository.save(plan);
}
```

**2. Sempre valide acesso a recursos específicos**

```java
// ✅ CORRETO
@GetMapping("/plans/{id}")
public Plan getPlan(@PathVariable Long id) {
    Plan plan = planRepository.findById(id)
        .orElseThrow(() -> new NotFoundException());
    
    // Valida se plano pertence à organização do usuário
    userService.validateOrganizationAccess(
        plan.getOrganizationId(), 
        "Plano"
    );
    
    return plan;
}
```

**3. Use @PreAuthorize para controle fino**

```java
// ✅ Apenas ADMIN pode criar planos
@PostMapping("/plans")
@PreAuthorize("hasRole('ADMIN')")
public Plan createPlan(@RequestBody Plan plan) {
    // ...
}

// ✅ USER e ADMIN podem listar
@GetMapping("/plans")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public List<Plan> getPlans() {
    // ...
}
```

---

## 🧪 Testes

### Teste Manual - JWT

```bash
# 1. Registrar
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@empresa.com",
    "password": "senha123",
    "name": "Teste",
    "organizationId": 1
  }'

# 2. Login
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@empresa.com",
    "password": "senha123"
  }' | jq -r '.token')

# 3. Testar endpoint protegido
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### Teste Manual - Firebase

```bash
# 1. Obter token Firebase (via frontend ou Firebase CLI)
FIREBASE_TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# 2. Verificar token
curl -X POST http://localhost:8080/api/auth/firebase-verify \
  -H "Content-Type: application/json" \
  -d "{\"firebaseToken\": \"$FIREBASE_TOKEN\"}"

# 3. Registrar (primeira vez)
curl -X POST http://localhost:8080/api/auth/firebase-register \
  -H "Content-Type: application/json" \
  -d "{
    \"firebaseToken\": \"$FIREBASE_TOKEN\",
    \"organizationId\": 1
  }"

# 4. Login (após registro)
curl -X POST http://localhost:8080/api/auth/firebase-login \
  -H "Content-Type: application/json" \
  -d "{\"firebaseToken\": \"$FIREBASE_TOKEN\"}"

# 5. Testar endpoint protegido
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $FIREBASE_TOKEN"
```

### Testes Unitários

**JwtTokenProviderTest:**

```java
@Test
void shouldValidateJwtToken() {
    String token = jwtTokenProvider.generateToken("test@example.com", "USER");
    assertTrue(jwtTokenProvider.validateToken(token));
}

@Test
void shouldExtractRoleFromToken() {
    String token = jwtTokenProvider.generateToken("test@example.com", "ADMIN");
    String role = jwtTokenProvider.getRoleFromToken(token);
    assertEquals("ADMIN", role);
}
```

**FirebaseTokenProviderTest:**

```java
@Test
void shouldValidateFirebaseToken() {
    // Mock FirebaseAuth
    when(firebaseAuth.verifyIdToken(anyString()))
        .thenReturn(mockFirebaseToken);
    
    FirebaseToken result = firebaseTokenProvider.validateToken("valid-token");
    assertNotNull(result);
}
```

**UnifiedAuthenticationFilterTest:**

```java
@Test
void shouldDetectJwtToken() {
    String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.signature";
    
    AuthType type = filter.detectAuthType(jwtToken);
    assertEquals(AuthType.JWT, type);
}

@Test
void shouldDetectFirebaseToken() {
    String firebaseToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vcHJvamVjdC1pZCJ9.signature";
    
    AuthType type = filter.detectAuthType(firebaseToken);
    assertEquals(AuthType.FIREBASE, type);
}
```

---

## 🐛 Troubleshooting

### Problema: Token Firebase inválido

**Erro:**

```
Token Firebase inválido: Token has been expired
```

**Causa:** Token expirou (válido por 1h)

**Solução:**

```javascript
// Frontend: Renovar token automaticamente
const freshToken = await auth.currentUser.getIdToken(true);
```

---

### Problema: Service account key não encontrado

**Erro:**

```
FileNotFoundException: serviceAccountKey.json
```

**Causa:** Arquivo não existe ou caminho errado

**Solução:**

1. Verificar se arquivo existe:
   ```bash
   ls src/main/resources/serviceAccountKey.json
   ```

2. Verificar variável de ambiente:
   ```env
   FIREBASE_SERVICE_ACCOUNT_KEY_PATH=serviceAccountKey.json
   ```

3. Usar caminho absoluto se necessário:
   ```env
   FIREBASE_SERVICE_ACCOUNT_KEY_PATH=/path/completo/serviceAccountKey.json
   ```

---

### Problema: Usuário não tem organizationId

**Erro:**

```
NullPointerException: organizationId is null
```

**Causa:** Usuário criado antes da migration ou sem organizationId

**Solução:**

```sql
-- Atualizar usuários sem organizationId
UPDATE users 
SET organization_id = 1 
WHERE organization_id IS NULL;
```

---

### Problema: CORS bloqueando requisições

**Erro (Frontend):**

```
Access to fetch at 'http://localhost:8080' from origin 'http://localhost:3000' 
has been blocked by CORS policy
```

**Solução:**

```yaml
# application.yml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

```env
# .env
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

---

### Problema: Firebase Admin SDK não inicializa

**Erro:**

```
FirebaseApp not initialized
```

**Causa:** Service account key inválido ou projeto Firebase incorreto

**Solução:**

1. Verificar service account key:
   ```json
   {
     "type": "service_account",
     "project_id": "meu-projeto-123",
     "private_key_id": "...",
     "private_key": "-----BEGIN PRIVATE KEY-----\n...",
     // ...
   }
   ```

2. Verificar project_id corresponde ao .env:
   ```env
   FIREBASE_PROJECT_ID=meu-projeto-123
   ```

3. Regenerar service account key se necessário

---

## 📚 Recursos Adicionais

### Documentação Oficial

- **Firebase Admin SDK (Java):** https://firebase.google.com/docs/admin/setup
- **Firebase Authentication:** https://firebase.google.com/docs/auth
- **Spring Security:** https://spring.io/projects/spring-security

### Exemplos Completos

- **UserExampleController:** `/presentation/controllers/UserExampleController.java`
- **FirebaseAuthController:** `/presentation/controllers/FirebaseAuthController.java`
- **UserService:** `/application/services/UserService.java`

### Próximos Passos

1. Implementar refresh token JWT (opcional)
2. Adicionar suporte a mais providers Firebase (Apple, Microsoft)
3. Implementar rate limiting por organizationId
4. Adicionar logs de auditoria por tenant
5. Implementar 2FA (Two-Factor Authentication)

---

**Versão:** 1.0  
**Data:** 2025-10-05  
**Autor:** Sistema de Gestão de Mensalidades  
**Licença:** Proprietary
