package com.inventory.deva_inventory.security;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.deva_inventory.config.WebSecurity;
import com.inventory.deva_inventory.controller.RoleController;
import com.inventory.deva_inventory.controller.UserController;
import com.inventory.deva_inventory.model.User;
import com.inventory.deva_inventory.service.RoleService;
import com.inventory.deva_inventory.service.UserService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = {UserController.class, RoleController.class})
@Import({WebSecurity.class, JwtService.class, SecurityWebMvcTest.ProbeController.class})
@TestPropertySource(properties = "jwt.secret=" + SecurityWebMvcTest.SECRET)
class SecurityWebMvcTest {

    static final String SECRET = "test-secret-that-is-at-least-32-bytes-long";

    @RestController
    static class ProbeController {
        @GetMapping("/api/probe")
        String probe() {
            return "ok";
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    @MockBean
    private RoleService roleService;
    @MockBean(name = "userDetailService")
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(userService.hasUsers()).thenReturn(true);
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(
                new org.springframework.security.core.userdetails.User("alice", encoder.encode("pw123"),
                        List.of(new SimpleGrantedAuthority("Admin"))));
    }

    private String accessToken(String... roles) {
        return jwtService.createAccessToken("bob", List.of(roles));
    }

    @Test
    void loginIssuesTokensThatAuthorizeRequests() throws Exception {
        String body = mvc.perform(post("/api/login").param("userName", "alice").param("password", "pw123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.refresh_token", not(emptyOrNullString())))
                .andReturn().getResponse().getContentAsString();
        JsonNode tokens = objectMapper.readTree(body);
        String accessToken = tokens.get("access_token").asText();

        assertExpiring(accessToken);
        assertExpiring(tokens.get("refresh_token").asText());

        mvc.perform(post("/api/checktoken/" + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("Admin"));

        when(userService.listUsers()).thenReturn(List.of());
        mvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    private static void assertExpiring(String token) {
        Instant expiresAt = JWT.decode(token).getExpiresAtAsInstant();
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new AssertionError("token must carry a future exp claim");
        }
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/login").param("userName", "alice").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRequiresValidToken() throws Exception {
        mvc.perform(get("/api/probe")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/probe").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/probe").header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtService.createRefreshToken("bob")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/probe").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken("Staff")))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void tokenSignedWithOtherSecretOrExpiredIsRejected() throws Exception {
        String forged = JWT.create().withSubject("bob").withIssuer("deva-inventory")
                .withClaim("token_type", "access").withClaim("roles", List.of("Admin"))
                .sign(Algorithm.HMAC256("secret"));
        String expired = JWT.create().withSubject("bob").withIssuer("deva-inventory")
                .withClaim("token_type", "access").withClaim("roles", List.of("Admin"))
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(SECRET));
        for (String token : List.of(forged, expired)) {
            mvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/checktoken/" + token)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void checkTokenRejectsBadTokensWith401() throws Exception {
        mvc.perform(post("/api/checktoken/garbage")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/checktoken/" + jwtService.createRefreshToken("bob")))
                .andExpect(status().isUnauthorized());
        String noRoles = JWT.create().withSubject("bob").withIssuer("deva-inventory")
                .withClaim("token_type", "access").withExpiresAt(Instant.now().plusSeconds(60))
                .sign(Algorithm.HMAC256(SECRET));
        mvc.perform(post("/api/checktoken/" + noRoles)).andExpect(status().isUnauthorized());
    }

    @Test
    void userAndRoleManagementRequiresAdmin() throws Exception {
        String staff = "Bearer " + accessToken("Staff");
        mvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, staff)).andExpect(status().isForbidden());
        mvc.perform(get("/api/roles").header(HttpHeaders.AUTHORIZATION, staff)).andExpect(status().isForbidden());
        mvc.perform(post("/api/users/1").header(HttpHeaders.AUTHORIZATION, staff)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userName\":\"eve\",\"password\":\"x\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/users/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"eve\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON).content("{\"roleName\":\"Admin\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bootstrapAllowsAnonymousRoleAndUserCreationOnlyWhileNoUsersExist() throws Exception {
        when(userService.hasUsers()).thenReturn(false);
        User saved = new User();
        saved.setUserName("alice");
        saved.setPassword("$2a$10$hash");
        when(userService.saveUser(eq(1), any(User.class))).thenReturn(saved);

        mvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON).content("{\"roleName\":\"Admin\"}"))
                .andExpect(status().isAccepted());
        mvc.perform(post("/api/users/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"alice\",\"password\":\"pw123\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.userName").value("alice"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void passwordIsNotSerialized() throws Exception {
        User user = new User();
        user.setUserName("alice");
        user.setPassword("$2a$10$hash");
        when(userService.listUsers()).thenReturn(List.of(user));

        mvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken("Admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("alice"))
                .andExpect(jsonPath("$[0].password").doesNotExist());

        User parsed = objectMapper.readValue("{\"userName\":\"a\",\"password\":\"pw\"}", User.class);
        if (!"pw".equals(parsed.getPassword())) {
            throw new AssertionError("password must still be accepted on input");
        }
    }

    @Test
    void corsAllowsOnlyConfiguredOrigin() throws Exception {
        mvc.perform(options("/api/probe").header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
        mvc.perform(options("/api/probe").header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
