package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas del flujo JWT de la Clase 5.
 *
 * <p>Reproducen las comprobaciones con curl de la guia:
 * sin token -&gt; 401, login -&gt; 200 con accessToken, con token -&gt; 200.
 *
 * <p>El perfil "test" usa H2 en memoria, de modo que las pruebas no dependen
 * de la base de datos MySQL de la aplicacion.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthFlowTests {

    @Autowired
    private MockMvc mockMvc;

    private String accessToken;

    @BeforeEach
    void login() throws Exception {
        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"alumno\",\"password\":\"demo123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String marker = "\"accessToken\":\"";
        accessToken = json.substring(json.indexOf(marker) + marker.length());
        accessToken = accessToken.substring(0, accessToken.indexOf('"'));
    }

    /** Sin token: la ruta autenticada responde 401. */
    @Test
    void rutaProtegidaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    /** Credenciales incorrectas: la cadena de seguridad responde 401. */
    @Test
    void loginConCredencialesIncorrectasDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"alumno\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    /** Con token valido: la ruta autenticada responde 200 con el subject del JWT. */
    @Test
    void rutaProtegidaConTokenDevuelve200() throws Exception {
        mockMvc.perform(get("/api/usuarios/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alumno"));
    }

    /** Token manipulado: la firma no valida y se responde 401. */
    @Test
    void tokenManipuladoDevuelve401() throws Exception {
        String[] partes = accessToken.split("\\.");
        partes[1] = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"atacante\",\"iss\":\"ecommerce-clase\"}".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/usuarios/me")
                        .header("Authorization", "Bearer " + String.join(".", partes)))
                .andExpect(status().isUnauthorized());
    }

    /** Token sin el prefijo "Bearer " no autentica. */
    @Test
    void tokenSinPrefijoBearerDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me")
                        .header("Authorization", accessToken))
                .andExpect(status().isUnauthorized());
    }
}
