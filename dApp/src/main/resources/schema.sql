-- ============================================
-- Dating App Database Schema
-- ============================================

-- Drop all tables (for fresh initialization)
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS matches CASCADE;
DROP TABLE IF EXISTS swipes CASCADE;
DROP TABLE IF EXISTS profile_interests CASCADE;
DROP TABLE IF EXISTS photos CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP TABLE IF EXISTS preferences CASCADE;
DROP TABLE IF EXISTS interests CASCADE;
DROP TABLE IF EXISTS profile_backups CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================
-- TABLE: users
-- Description: Główna tabela użytkowników
-- ============================================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    gender VARCHAR(20) NOT NULL COMMENT 'MALE, FEMALE, OTHER',
    birth_date DATE NOT NULL,
    city VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_city (city),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: profiles
-- Description: Profile użytkowników
-- ============================================
CREATE TABLE profiles (
    profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    bio TEXT,
    height_cm INT COMMENT 'Wzrost w centimetrach',
    occupation VARCHAR(100),
    education VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: photos
-- Description: Zdjęcia w profilach
-- ============================================
CREATE TABLE photos (
    photo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_photo_profile FOREIGN KEY (profile_id) REFERENCES profiles(profile_id) ON DELETE CASCADE,
    INDEX idx_profile_id (profile_id),
    INDEX idx_is_primary (is_primary),
    UNIQUE KEY uk_photo_url (photo_url)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: interests
-- Description: Zainteresowania/hobby
-- ============================================
CREATE TABLE interests (
    interest_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    category VARCHAR(50),
    icon VARCHAR(20),

    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: profile_interests
-- Description: Relacja N:N między profilami i zainteresowaniami
-- ============================================
CREATE TABLE profile_interests (
    profile_id BIGINT NOT NULL,
    interest_id BIGINT NOT NULL,

    PRIMARY KEY (profile_id, interest_id),
    CONSTRAINT fk_pi_profile FOREIGN KEY (profile_id) REFERENCES profiles(profile_id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_interest FOREIGN KEY (interest_id) REFERENCES interests(interest_id) ON DELETE CASCADE,
    INDEX idx_interest_id (interest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: preferences
-- Description: Preferencje wyszukiwania
-- ============================================
CREATE TABLE preferences (
    preference_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    preferred_gender VARCHAR(20) NOT NULL COMMENT 'MALE, FEMALE, OTHER',
    min_age INT NOT NULL,
    max_age INT NOT NULL,
    max_distance_km INT NOT NULL DEFAULT 50,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_preference_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    CHECK (min_age >= 18 AND max_age <= 100),
    CHECK (min_age <= max_age)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: swipes
-- Description: Akcje swipe'a (LIKE, DISLIKE, SUPER_LIKE)
-- ============================================
CREATE TABLE swipes (
    swipe_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    swiper_id BIGINT NOT NULL,
    swiped_user_id BIGINT NOT NULL,
    swipe_type VARCHAR(20) NOT NULL COMMENT 'LIKE, DISLIKE, SUPER_LIKE',
    swiped_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_swipe_swiper FOREIGN KEY (swiper_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_swipe_swiped FOREIGN KEY (swiped_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_swiper_swiped (swiper_id, swiped_user_id),
    INDEX idx_swiper_id (swiper_id),
    INDEX idx_swiped_user_id (swiped_user_id),
    INDEX idx_swipe_type (swipe_type),
    INDEX idx_swiped_at (swiped_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: matches
-- Description: Dopasowania między użytkownikami
-- ============================================
CREATE TABLE matches (
    match_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    matched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unmatched_at TIMESTAMP NULL,

    CONSTRAINT fk_match_user1 FOREIGN KEY (user1_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_match_user2 FOREIGN KEY (user2_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user1_user2 (user1_id, user2_id),
    INDEX idx_user1_id (user1_id),
    INDEX idx_user2_id (user2_id),
    INDEX idx_is_active (is_active),
    INDEX idx_matched_at (matched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: messages
-- Description: Wiadomości w konwersacjach
-- ============================================
CREATE TABLE messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,

    CONSTRAINT fk_message_match FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_match_id (match_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_sent_at (sent_at),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: profile_backups
-- Description: Kopie zapasowe profili
-- ============================================
CREATE TABLE profile_backups (
    backup_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    backup_data LONGTEXT NOT NULL,
    backup_format VARCHAR(10) NOT NULL COMMENT 'JSON, XML',
    description VARCHAR(500),
    file_size_bytes BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_backup_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Indeksy dla wydajności
-- ============================================

-- Indeksy dla matching algorithm
CREATE INDEX idx_users_gender_active ON users(gender, is_active);
CREATE INDEX idx_users_city_active ON users(city, is_active);

-- Indeks dla szybkiego wyszukiwania w chatach
CREATE INDEX idx_messages_match_sent ON messages(match_id, sent_at DESC);

-- Indeks dla historii swipe'ów
CREATE INDEX idx_swipes_swiper_type ON swipes(swiper_id, swipe_type);

-- ============================================
-- Initial Statistics View (opcjonalnie)
-- ============================================
-- Ta view pokazuje statystyki systemu
-- CREATE VIEW user_statistics AS
-- SELECT
--   COUNT(DISTINCT u.user_id) as total_users,
--   SUM(CASE WHEN u.is_active = TRUE THEN 1 ELSE 0 END) as active_users,
--   COUNT(DISTINCT u.gender) as unique_genders,
--   COUNT(DISTINCT m.match_id) as total_matches
-- FROM users u
-- LEFT JOIN matches m ON (u.user_id = m.user1_id OR u.user_id = m.user2_id) AND m.is_active = TRUE;

