package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Message Entity Tests")
class MessageTests {

    private Message message;
    private User sender;
    private User receiver;
    private Match match;

    @BeforeEach
    void setUp() {
        sender = User.builder().id(1L).username("alice").build();
        receiver = User.builder().id(2L).username("bob").build();
        match = Match.builder().id(100L).user1(sender).user2(receiver).build();

        message = Message.builder()
                .id(1L)
                .match(match)
                .sender(sender)
                .receiver(receiver)
                .content("Cześć! Jak się masz?")
                .isRead(false)
                .sentAt(LocalDateTime.now().minusMinutes(5))
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(1L, message.getId());
        assertEquals(match, message.getMatch());
        assertEquals(sender, message.getSender());
        assertEquals(receiver, message.getReceiver());
        assertEquals("Cześć! Jak się masz?", message.getContent());
        assertFalse(message.getIsRead());
        assertNotNull(message.getSentAt());
        assertNull(message.getReadAt());
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        message.setContent("Nowa wiadomość");
        message.setIsRead(true);

        assertEquals("Nowa wiadomość", message.getContent());
        assertTrue(message.getIsRead());
    }

    @Test
    @DisplayName("markAsRead powinno ustawić isRead i readAt")
    void markAsRead_setsIsReadAndReadAt() {
        assertFalse(message.getIsRead());
        assertNull(message.getReadAt());

        message.markAsRead();

        assertTrue(message.getIsRead());
        assertNotNull(message.getReadAt());
    }

    @Test
    @DisplayName("isSentBy powinno sprawdzić czy wiadomość wysłana przez danego użytkownika")
    void isSentBy_checksIfSentByUser() {
        assertTrue(message.isSentBy(sender));
        assertFalse(message.isSentBy(receiver));

        User otherUser = User.builder().id(3L).username("charlie").build();
        assertFalse(message.isSentBy(otherUser));
    }

    @Test
    @DisplayName("getRecipient powinno zwrócić odborcę (partnera w konwersacji)")
    void getRecipient_returnsPartnerInConversation() {
        User recipient = message.getRecipient();
        assertEquals(receiver, recipient);

        // Test z odwrotną kolejnością
        message.setSender(receiver);
        recipient = message.getRecipient();
        assertEquals(sender, recipient);
    }

    @Test
    @DisplayName("isUnread powinno zwrócić true gdy wiadomość nieprzeczytana")
    void isUnread_returnsTrueWhenUnread() {
        assertTrue(message.isUnread());

        message.setIsRead(true);
        assertFalse(message.isUnread());
    }

    @Test
    @DisplayName("equals powinno porównywać id i sentAt")
    void equals_comparesIdAndSentAt() {
        LocalDateTime now = LocalDateTime.now();
        Message msg1 = Message.builder().id(1L).sentAt(now).build();
        Message msg2 = Message.builder().id(1L).sentAt(now).build();
        Message msg3 = Message.builder().id(2L).sentAt(now).build();
        Message msg4 = Message.builder().id(1L).sentAt(now.plusSeconds(1)).build();

        assertEquals(msg1, msg2);
        assertNotEquals(msg1, msg3);
        assertNotEquals(msg1, msg4);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        LocalDateTime now = LocalDateTime.now();
        Message msg1 = Message.builder().id(1L).sentAt(now).build();
        Message msg2 = Message.builder().id(1L).sentAt(now).build();

        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać kluczowe pola")
    void toString_containsKeyFields() {
        String str = message.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("matchId=100"));
        assertTrue(str.contains("senderId=1"));
        assertTrue(str.contains("isRead=false"));
    }
}

