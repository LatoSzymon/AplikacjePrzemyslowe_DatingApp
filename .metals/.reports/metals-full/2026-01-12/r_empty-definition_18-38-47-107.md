error id: file:///C:/Users/szymo/repozytoria/AplikacjePrzemyslowe_DatingApp/dApp/src/test/java/AplikacjePrzeyslowe/dApp/controller/rest/UserRestControllerTest.java:AplikacjePrzeyslowe/dApp/dto/UserRegistrationDTO#
file:///C:/Users/szymo/repozytoria/AplikacjePrzemyslowe_DatingApp/dApp/src/test/java/AplikacjePrzeyslowe/dApp/controller/rest/UserRestControllerTest.java
empty definition using pc, found symbol in pc: AplikacjePrzeyslowe/dApp/dto/UserRegistrationDTO#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 889
uri: file:///C:/Users/szymo/repozytoria/AplikacjePrzemyslowe_DatingApp/dApp/src/test/java/AplikacjePrzeyslowe/dApp/controller/rest/UserRestControllerTest.java
text:
```scala
package AplikacjePrzeyslowe.dApp.controller.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import AplikacjePrzeyslowe.dApp.service.UserService;
import AplikacjePrzeyslowe.dApp.dto.@@UserRegistrationDTO;

@WebMvcTest(UserRestController.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCreateUser() throws Exception {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("test@example.com");
        dto.setUsername("testuser");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: AplikacjePrzeyslowe/dApp/dto/UserRegistrationDTO#