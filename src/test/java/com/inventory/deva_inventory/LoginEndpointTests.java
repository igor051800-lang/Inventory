package com.inventory.deva_inventory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventory.deva_inventory.dao.RoleRepository;
import com.inventory.deva_inventory.dao.UserRepository;
import com.inventory.deva_inventory.model.Role;
import com.inventory.deva_inventory.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void createUser() {
        Role role = new Role();
        role.setRoleName("ADMIN");

        User user = new User();
        user.setUserName("admin");
        user.setPassword(encoder.encode("secret-password"));
        user.setUserStatus("enabled");
        user.addRole(role);
        userRepository.save(user);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void loginWithValidCredentialsReturnsTokens() throws Exception {
        mockMvc.perform(post("/api/login")
                .param("userName", "admin")
                .param("password", "secret-password"))
                .andExpect(status().isOk())
                .andExpect(header().exists("access_token"))
                .andExpect(header().exists("refresh_token"))
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty());
    }

    @Test
    void loginWithInvalidPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/login")
                .param("userName", "admin")
                .param("password", "wrong-password"))
                .andExpect(status().isUnauthorized());
    }
}
