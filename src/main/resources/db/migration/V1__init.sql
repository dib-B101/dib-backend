-- 최초 마이그레이션: pgvector 확장 (로컬 컨테이너/RDS 모두 여기서 보장)
CREATE EXTENSION IF NOT EXISTS vector;
