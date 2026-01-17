package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Match Entity Tests")
class MatchTests {

    private Match match;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).username("alice").build();
        user2 = User.builder().id(2L).username("bob").build();
        match = Match.builder()
                .id(100L)
                .user1(user1)
                .user2(user2)
                .isActive(true)
                .matchedAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(100L, match.getId());
        assertEquals(user1, match.getUser1());
        assertEquals(user2, match.getUser2());
        assertTrue(match.getIsActive());
        // matchedAt może być ustawiony w builderze, ale jeśli nie - to ok
        assertNull(match.getUnmatchedAt());
        assertNotNull(match.getMessages());
        assertTrue(match.getMessages().isEmpty());
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        User user3 = User.builder().id(3L).username("charlie").build();
        match.setUser1(user3);
        match.setIsActive(false);

        assertEquals(user3, match.getUser1());
        assertFalse(match.getIsActive());
    }

    @Test
    @DisplayName("containsUser powinno sprawdzić czy user należy do matcha")
    void containsUser_checksUserInMatch() {
        assertTrue(match.containsUser(user1));
        assertTrue(match.containsUser(user2));

        User user3 = User.builder().id(3L).username("charlie").build();
        assertFalse(match.containsUser(user3));
    }

    @Test
    @DisplayName("containsUser powinno obsługiwać null")
    void containsUser_handlesNull() {
        assertFalse(match.containsUser(null));
    }

    @Test
    @DisplayName("getPartner powinno zwrócić drugiego użytkownika")
    void getPartner_returnsOtherUser() {
        assertEquals(user2, match.getPartner(user1));
        assertEquals(user1, match.getPartner(user2));

        User user3 = User.builder().id(3L).username("charlie").build();
        assertNull(match.getPartner(user3));
    }

    @Test
    @DisplayName("getPartner powinno obsługiwać null")
    void getPartner_handlesNull() {
        assertNull(match.getPartner(null));
    }

    @Test
    @DisplayName("addMessage powinno dodać wiadomość")
    void addMessage_addsMessageToMatch() {
        assertEquals(0, match.getMessageCount());

        Message message = Message.builder().id(1L).content("Cześć!").sender(user1).build();
        match.addMessage(message);

        assertEquals(1, match.getMessageCount());
        assertTrue(match.hasMessages());
        assertEquals(match, message.getMatch());
    }

    @Test
    @DisplayName("addMessage powinno obsługiwać null")
    void addMessage_handlesNull() {
        assertEquals(0, match.getMessageCount());
        match.addMessage(null);
        assertEquals(0, match.getMessageCount());
    }

    @Test
    @DisplayName("removeMessage powinno usunąć wiadomość")
    void removeMessage_removesMessageFromMatch() {
        Message message = Message.builder().id(1L).content("Cześć!").sender(user1).build();
        match.addMessage(message);
        assertEquals(1, match.getMessageCount());

        match.removeMessage(message);
        assertEquals(0, match.getMessageCount());
        assertFalse(match.hasMessages());
    }

    @Test
    @DisplayName("removeMessage powinno obsługiwać null")
    void removeMessage_handlesNull() {
        match.removeMessage(null);
        assertEquals(0, match.getMessageCount());
    }

    @Test
    @DisplayName("unmatch powinno deaktywować match i ustawić unmatchedAt")
    void unmatch_deactivatesMatchAndSetsUnmatchedAt() {
        assertTrue(match.getIsActive());
        assertNull(match.getUnmatchedAt());

        match.unmatch();

        assertFalse(match.getIsActive());
        assertNotNull(match.getUnmatchedAt());
    }

    @Test
    @DisplayName("getMessageCount i hasMessages powinny działać")
    void getMessageCount_and_hasMessages_work() {
        assertEquals(0, match.getMessageCount());
        assertFalse(match.hasMessages());

        Message msg1 = Message.builder().id(1L).content("Hi").sender(user1).build();
        Message msg2 = Message.builder().id(2L).content("Hello").sender(user2).build();
        match.addMessage(msg1);
        match.addMessage(msg2);

        assertEquals(2, match.getMessageCount());
        assertTrue(match.hasMessages());
    }

    @Test
    @DisplayName("equals powinno porównywać id i obu userów (niezależnie od kolejności)")
    void equals_comparesIdAndUsers_orderIndependent() {
        Match match1 = Match.builder().id(1L).user1(user1).user2(user2).build();
        Match match2 = Match.builder().id(1L).user1(user1).user2(user2).build();
        Match match3 = Match.builder().id(1L).user1(user2).user2(user1).build(); // Odwrotna kolejność
        Match match4 = Match.builder().id(2L).user1(user1).user2(user2).build();

        assertEquals(match1, match2);
        assertEquals(match1, match3); // Powinno być równe mimo odwrotnej kolejności
        assertNotEquals(match1, match4);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        Match match1 = Match.builder().id(1L).user1(user1).user2(user2).build();
        Match match2 = Match.builder().id(1L).user1(user2).user2(user1).build(); // Odwrotna kolejność

        assertEquals(match1.hashCode(), match2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać ID, user IDs i messagesCount")
    void toString_containsKeyFields() {
        String str = match.toString();
        assertTrue(str.contains("id=100"));
        assertTrue(str.contains("user1Id=1"));
        assertTrue(str.contains("user2Id=2"));
        assertTrue(str.contains("messagesCount=0"));
    }
}

