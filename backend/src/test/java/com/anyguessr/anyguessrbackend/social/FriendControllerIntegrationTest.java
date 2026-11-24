package com.anyguessr.anyguessrbackend.social;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.anyguessr.anyguessrbackend.social.ui.FriendController;
import com.anyguessr.anyguessrbackend.social.app.FriendService;
import com.anyguessr.anyguessrbackend.social.app.UserLookupService.UserSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = FriendController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
class FriendControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendService friendService;

    @Test
    void getFriendsByUserId_returnsOkAndFriendList() throws Exception {
        UserSummary friend = new UserSummary(
                2L,
                "alex",
                "alex",
                "Unknown",
                "AL",
                3,
                "Online",
                "duel-2"
        );
        when(friendService.getFriendsByUserId(1L)).thenReturn(List.of(friend));

        mockMvc.perform(get("/api/friends/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alex"));
    }
}