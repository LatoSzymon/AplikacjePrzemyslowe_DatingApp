package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Swipe Entity Tests")
class SwipeTests {

    private Swipe swipe;
    private User swiper;
    private User swipedUser;

    @BeforeEach
    void setUp() {
        swiper = User.builder().id(1L).username("alice").build();
        swipedUser = User.builder().id(2L).username("bob").build();
        swipe = Swipe.builder()
                .id(1L)
                .swiper(swiper)
                .swipedUser(swipedUser)
                .swipeType(SwipeType.LIKE)
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(1L, swipe.getId());
        assertEquals(swiper, swipe.getSwiper());
        assertEquals(swipedUser, swipe.getSwipedUser());
        assertEquals(SwipeType.LIKE, swipe.getSwipeType());
        // swipedAt jest ustawiany automatycznie przez @CreationTimestamp podczas zapisu do bazy
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        swipe.setSwipeType(SwipeType.DISLIKE);
        assertEquals(SwipeType.DISLIKE, swipe.getSwipeType());
    }

    @Test
    @DisplayName("isLike powinno zwrócić true dla LIKE")
    void isLike_returnsTrueForLike() {
        swipe.setSwipeType(SwipeType.LIKE);
        assertTrue(swipe.isLike());

        swipe.setSwipeType(SwipeType.DISLIKE);
        assertFalse(swipe.isLike());
    }

    @Test
    @DisplayName("isDislike powinno zwrócić true dla DISLIKE")
    void isDislike_returnsTrueForDislike() {
        swipe.setSwipeType(SwipeType.DISLIKE);
        assertTrue(swipe.isDislike());

        swipe.setSwipeType(SwipeType.LIKE);
        assertFalse(swipe.isDislike());
    }

    @Test
    @DisplayName("isSuperLike powinno zwrócić true dla SUPER_LIKE")
    void isSuperLike_returnsTrueForSuperLike() {
        swipe.setSwipeType(SwipeType.SUPER_LIKE);
        assertTrue(swipe.isSuperLike());

        swipe.setSwipeType(SwipeType.LIKE);
        assertFalse(swipe.isSuperLike());
    }

    @Test
    @DisplayName("equals powinno porównywać id, swiper i swipedUser")
    void equals_comparesIdAndUsers() {
        Swipe swipe1 = Swipe.builder().id(1L).swiper(swiper).swipedUser(swipedUser).build();
        Swipe swipe2 = Swipe.builder().id(1L).swiper(swiper).swipedUser(swipedUser).build();
        Swipe swipe3 = Swipe.builder().id(2L).swiper(swiper).swipedUser(swipedUser).build();

        User otherUser = User.builder().id(3L).username("charlie").build();
        Swipe swipe4 = Swipe.builder().id(1L).swiper(otherUser).swipedUser(swipedUser).build();

        assertEquals(swipe1, swipe2);
        assertNotEquals(swipe1, swipe3);
        assertNotEquals(swipe1, swipe4);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        Swipe swipe1 = Swipe.builder().id(1L).swiper(swiper).swipedUser(swipedUser).build();
        Swipe swipe2 = Swipe.builder().id(1L).swiper(swiper).swipedUser(swipedUser).build();

        assertEquals(swipe1.hashCode(), swipe2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać kluczowe pola")
    void toString_containsKeyFields() {
        String str = swipe.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("swiperId=1"));
        assertTrue(str.contains("swipedUserId=2"));
        assertTrue(str.contains("swipeType=LIKE"));
    }
}

