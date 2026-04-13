ALTER TABLE users 
ADD COLUMN verification_token VARCHAR(64),
ADD COLUMN verification_token_expires_at TIMESTAMP;