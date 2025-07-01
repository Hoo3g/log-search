-- Cài đặt unaccent hỗ trợ tìm kiếm tiếng Việt không dấu
CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;
-- Bắt buộc cài đặt pg_trgm nếu muốn tìm kiếm ILIKE
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


