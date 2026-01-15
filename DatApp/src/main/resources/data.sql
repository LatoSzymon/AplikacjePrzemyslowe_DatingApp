-- ============================================
-- Dating App - Sample Test Data
-- ============================================

-- ============================================
-- TEST USERS - 10 użytkowników testowych
-- ============================================
INSERT INTO users (user_id, username, email, password, gender, birth_date, city, is_active) VALUES
(1, 'alice_smith', 'alice@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'FEMALE', '2000-05-15', 'Warsaw', TRUE),
(2, 'bob_jones', 'bob@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'MALE', '1998-08-22', 'Warsaw', TRUE),
(3, 'charlie_brown', 'charlie@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'MALE', '1999-03-10', 'Krakow', TRUE),
(4, 'diana_prince', 'diana@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'FEMALE', '2001-07-20', 'Warsaw', TRUE),
(5, 'evan_davis', 'evan@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'MALE', '1997-12-05', 'Gdansk', TRUE),
(6, 'fiona_harris', 'fiona@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'FEMALE', '2002-01-18', 'Wroclaw', TRUE),
(7, 'george_martin', 'george@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'MALE', '1996-06-30', 'Warsaw', TRUE),
(8, 'hannah_clark', 'hannah@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'FEMALE', '2000-11-12', 'Krakow', TRUE),
(9, 'ivan_cooper', 'ivan@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'MALE', '1999-09-25', 'Gdansk', TRUE),
(10, 'julia_taylor', 'julia@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy70jKm', 'FEMALE', '2001-04-08', 'Warsaw', TRUE);

-- ============================================
-- PROFILES - Profile dla użytkowników
-- ============================================
INSERT INTO profiles (profile_id, user_id, bio, height_cm, occupation, education, latitude, longitude) VALUES
(1, 1, 'Kocham podróżować i odkrywać nowe miejsca! 🌍 Szukam kogoś do wspólnych przygód.', 165, 'Software Engineer', 'Bachelor', 52.2297, 21.0122),
(2, 2, 'Pasjonat fotografii i przyrody. Lubię spacery w lesie i dobrą książkę.', 180, 'Photographer', 'Bachelor', 52.2297, 21.0122),
(3, 3, 'Analityk danych, miłośnik kawy i filmów. Zawsze szukam ciekawej rozmowy.', 178, 'Data Analyst', 'Master', 50.0647, 19.9450),
(4, 4, 'Projektantka graficzna z pasją do sztuki. Kocham sport i zdrowy tryb życia.', 168, 'Graphic Designer', 'Bachelor', 52.2297, 21.0122),
(5, 5, 'Nauczyciel matematyki, sportowiec. Uwielbiam grać w piłkę nożną i biegać.', 185, 'Teacher', 'Master', 54.3520, 18.6466),
(6, 6, 'Księgowa z miłością do muzyki i gotowania. Zawsze uśmiechnięta! 😊', 160, 'Accountant', 'Bachelor', 51.1079, 17.0385),
(7, 7, 'Inżynier oprogramowania, gamer, miłośnik technologii i gier planszowych.', 182, 'Software Engineer', 'Master', 52.2297, 21.0122),
(8, 8, 'Psycholog, medytacja i joga. Szukam głębokich rozmów i autentycznych połączeń.', 162, 'Psychologist', 'Master', 50.0647, 19.9450),
(9, 9, 'Menedżer projektów, podróżnik. Szukam kogoś do wspólnych przygód!', 179, 'Project Manager', 'Bachelor', 54.3520, 18.6466),
(10, 10, 'Lekarka, miłośniczka medycyny i dobroczynności. Zawsze udzielam się społecznie.', 166, 'Doctor', 'Master', 52.2297, 21.0122);

-- ============================================
-- PHOTOS - Zdjęcia profili
-- ============================================
INSERT INTO photos (photo_id, profile_id, photo_url, is_primary, display_order) VALUES
-- Alice
(1, 1, 'https://i.pravatar.cc/300?img=1', TRUE, 0),
(2, 1, 'https://i.pravatar.cc/300?img=2', FALSE, 1),
-- Bob
(3, 2, 'https://i.pravatar.cc/300?img=3', TRUE, 0),
(4, 2, 'https://i.pravatar.cc/300?img=4', FALSE, 1),
-- Charlie
(5, 3, 'https://i.pravatar.cc/300?img=5', TRUE, 0),
(6, 3, 'https://i.pravatar.cc/300?img=6', FALSE, 1),
-- Diana
(7, 4, 'https://i.pravatar.cc/300?img=7', TRUE, 0),
(8, 4, 'https://i.pravatar.cc/300?img=8', FALSE, 1),
-- Evan
(9, 5, 'https://i.pravatar.cc/300?img=9', TRUE, 0),
-- Fiona
(10, 6, 'https://i.pravatar.cc/300?img=10', TRUE, 0),
-- George
(11, 7, 'https://i.pravatar.cc/300?img=11', TRUE, 0),
-- Hannah
(12, 8, 'https://i.pravatar.cc/300?img=12', TRUE, 0),
-- Ivan
(13, 9, 'https://i.pravatar.cc/300?img=13', TRUE, 0),
-- Julia
(14, 10, 'https://i.pravatar.cc/300?img=14', TRUE, 0);

-- ============================================
-- INTERESTS - 15 przykładowych zainteresowań
-- ============================================
INSERT INTO interests (interest_id, name, description, category, icon) VALUES
(1, 'Travel', 'Explorowanie nowych miejsc i kultur', 'Adventure', '✈️'),
(2, 'Photography', 'Robienie i edytowanie zdjęć', 'Art', '📷'),
(3, 'Gaming', 'Gry wideo i gry planszowe', 'Entertainment', '🎮'),
(4, 'Fitness', 'Sport i trening fizyczny', 'Health', '💪'),
(5, 'Music', 'Słuchanie i tworzenie muzyki', 'Art', '🎵'),
(6, 'Reading', 'Czytanie książek i artykułów', 'Education', '📚'),
(7, 'Cooking', 'Gotowanie i eksperymenty kulinarne', 'Food', '👨‍🍳'),
(8, 'Yoga', 'Joga i medytacja', 'Health', '🧘'),
(9, 'Movie', 'Oglądanie filmów i seriali', 'Entertainment', '🎬'),
(10, 'Technology', 'Technologia i programowanie', 'Science', '💻'),
(11, 'Nature', 'Przyroda i ochrona środowiska', 'Adventure', '🌿'),
(12, 'Coffee', 'Kawa i kawiarnie', 'Food', '☕'),
(13, 'Hiking', 'Wędrówki górskie', 'Adventure', '🥾'),
(14, 'Art', 'Sztuka i malarstwo', 'Art', '🎨'),
(15, 'Dancing', 'Taniec i muzyka taneczna', 'Entertainment', '💃');

-- ============================================
-- PROFILE_INTERESTS - Zainteresowania użytkowników
-- ============================================
INSERT INTO profile_interests (profile_id, interest_id) VALUES
-- Alice (1, 2, 4, 11, 13) - Travel, Photography, Fitness, Nature, Hiking
(1, 1), (1, 2), (1, 4), (1, 11), (1, 13),
-- Bob (2, 3, 5, 6, 9) - Photography, Gaming, Music, Reading, Movie
(2, 2), (2, 3), (2, 5), (2, 6), (2, 9),
-- Charlie (3, 6, 10, 12, 5) - Reading, Technology, Coffee, Music
(3, 6), (3, 10), (3, 12), (3, 5),
-- Diana (4, 7, 8, 14, 4) - Cooking, Yoga, Art, Fitness
(4, 7), (4, 8), (4, 14), (4, 4),
-- Evan (5, 4, 1, 3, 6) - Fitness, Travel, Gaming, Reading
(5, 4), (5, 1), (5, 3), (5, 6),
-- Fiona (6, 7, 12, 5, 9) - Cooking, Coffee, Music, Movie
(6, 7), (6, 12), (6, 5), (6, 9),
-- George (7, 3, 10, 6, 5) - Gaming, Technology, Reading, Music
(7, 3), (7, 10), (7, 6), (7, 5),
-- Hannah (8, 8, 11, 13, 14) - Yoga, Nature, Hiking, Art
(8, 8), (8, 11), (8, 13), (8, 14),
-- Ivan (9, 1, 4, 13, 9) - Travel, Fitness, Hiking, Movie
(9, 1), (9, 4), (9, 13), (9, 9),
-- Julia (10, 7, 8, 4, 6) - Cooking, Yoga, Fitness, Reading
(10, 7), (10, 8), (10, 4), (10, 6);

-- ============================================
-- PREFERENCES - Preferencje wyszukiwania
-- ============================================
INSERT INTO preferences (preference_id, user_id, preferred_gender, min_age, max_age, max_distance_km) VALUES
(1, 1, 'MALE', 25, 35, 50),
(2, 2, 'FEMALE', 23, 32, 40),
(3, 3, 'FEMALE', 24, 34, 60),
(4, 4, 'MALE', 26, 36, 50),
(5, 5, 'FEMALE', 25, 33, 45),
(6, 6, 'MALE', 27, 37, 55),
(7, 7, 'FEMALE', 24, 35, 50),
(8, 8, 'MALE', 26, 38, 60),
(9, 9, 'FEMALE', 25, 34, 50),
(10, 10, 'MALE', 28, 40, 70);

-- ============================================
-- SWIPES - Przykładowe swipe'y
-- ============================================
INSERT INTO swipes (swipe_id, swiper_id, swiped_user_id, swipe_type, swiped_at) VALUES
-- Alice (1) polubił/polubił
(1, 1, 2, 'LIKE', NOW() - INTERVAL 2 DAY),
(2, 1, 3, 'DISLIKE', NOW() - INTERVAL 1 DAY),
(3, 1, 5, 'LIKE', NOW()),
-- Bob (2) polubił
(4, 2, 1, 'LIKE', NOW() - INTERVAL 2 DAY),
(5, 2, 4, 'LIKE', NOW() - INTERVAL 1 DAY),
(6, 2, 6, 'DISLIKE', NOW()),
-- Charlie (3) polubił
(7, 3, 1, 'DISLIKE', NOW() - INTERVAL 1 DAY),
(8, 3, 4, 'LIKE', NOW()),
-- Diana (4) polubił
(9, 4, 2, 'LIKE', NOW() - INTERVAL 1 DAY),
(10, 4, 7, 'LIKE', NOW()),
-- Evan (5) polubił
(11, 5, 1, 'LIKE', NOW()),
(12, 5, 8, 'LIKE', NOW()),
-- Fiona (6) polubił
(13, 6, 3, 'LIKE', NOW() - INTERVAL 1 DAY),
(14, 6, 7, 'LIKE', NOW()),
-- George (7) polubił
(15, 7, 4, 'LIKE', NOW() - INTERVAL 1 DAY),
(16, 7, 10, 'LIKE', NOW()),
-- Hannah (8) polubił
(17, 8, 5, 'LIKE', NOW() - INTERVAL 2 DAY),
(18, 8, 9, 'LIKE', NOW()),
-- Ivan (9) polubił
(19, 9, 2, 'LIKE', NOW() - INTERVAL 1 DAY),
(20, 9, 10, 'LIKE', NOW()),
-- Julia (10) polubił
(21, 10, 3, 'LIKE', NOW() - INTERVAL 1 DAY),
(22, 10, 7, 'LIKE', NOW());

-- ============================================
-- MATCHES - Dopasowania (wzajemne like'i)
-- ============================================
INSERT INTO matches (match_id, user1_id, user2_id, is_active, matched_at) VALUES
-- Alice (1) x Bob (2) - wzajemny like
(1, 1, 2, TRUE, NOW() - INTERVAL 2 DAY),
-- Bob (2) x Diana (4) - wzajemny like
(2, 2, 4, TRUE, NOW() - INTERVAL 1 DAY),
-- Evan (5) x Alice (1) - wzajemny like
(3, 1, 5, TRUE, NOW()),
-- Diana (4) x George (7) - wzajemny like
(4, 4, 7, TRUE, NOW() - INTERVAL 1 DAY),
-- Hannah (8) x Evan (5) - wzajemny like
(5, 5, 8, TRUE, NOW() - INTERVAL 2 DAY),
-- Ivan (9) x Julia (10) - wzajemny like
(6, 9, 10, TRUE, NOW() - INTERVAL 1 DAY);

-- ============================================
-- MESSAGES - Wiadomości w konwersacjach
-- ============================================
INSERT INTO messages (message_id, match_id, sender_id, content, is_read, sent_at, read_at) VALUES
-- Match 1: Alice (1) x Bob (2)
(1, 1, 1, 'Cześć! Jak się masz? 😊', TRUE, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 5 MINUTE),
(2, 1, 2, 'Cześć Alice! Wszystko super! Jak się masz ty?', TRUE, NOW() - INTERVAL 2 DAY + INTERVAL 30 MINUTE, NOW() - INTERVAL 1 DAY + INTERVAL 10 MINUTE),
(3, 1, 1, 'Super! Czy chciałbyś się kiedyś spotkać? 🎬', TRUE, NOW() - INTERVAL 1 DAY + INTERVAL 2 HOUR, NOW() - INTERVAL 1 DAY + INTERVAL 1 HOUR),

-- Match 2: Bob (2) x Diana (4)
(4, 2, 2, 'Hej Diana! Widziałem, że mamy wspólne zainteresowania!', TRUE, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 12 HOUR),
(5, 2, 4, 'Bob! Tak, to fajne! Lubisz też gotowanie?', TRUE, NOW() - INTERVAL 1 DAY + INTERVAL 1 HOUR, NOW() - INTERVAL 12 HOUR),

-- Match 3: Evan (5) x Alice (1)
(6, 3, 5, 'Cześć Alice! 👋', FALSE, NOW() - INTERVAL 5 MINUTE, NULL),

-- Match 5: Hannah (8) x Evan (5)
(7, 5, 8, 'Evan! Chętnie bym się z Tobą spotkała! 😊', TRUE, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(8, 5, 5, 'Hannah, ja też! Może w ten weekend? ☕', TRUE, NOW() - INTERVAL 2 DAY + INTERVAL 30 MINUTE, NOW() - INTERVAL 1 DAY);

-- ============================================
-- PROFILE_BACKUPS - Kopie zapasowe (opcjonalnie, puste dane)
-- ============================================
-- Te dane będą generowane dynamicznie podczas eksportu

-- ============================================
-- Statystyka po inicjalizacji
-- ============================================
-- Użytkowników: 10
-- Profili: 10
-- Zdjęć: 14
-- Zainteresowań: 15
-- Relacji profil-zainteresowanie: 40
-- Preferencji: 10
-- Swipe'ów: 22
-- Matchów: 6
-- Wiadomości: 8

