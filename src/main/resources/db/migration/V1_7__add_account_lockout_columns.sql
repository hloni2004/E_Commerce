-- Add account lockout columns to users table
-- These columns track failed login attempts and lock accounts for 10 minutes after 3 failures

ALTER TABLE users
ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;

ALTER TABLE users
ADD COLUMN IF NOT EXISTS account_locked_until TIMESTAMP;

-- Add comments for documentation
COMMENT ON COLUMN users.failed_login_attempts IS 'Counter for failed login attempts. Resets to 0 on successful login.';
COMMENT ON COLUMN users.account_locked_until IS 'Timestamp when account lock expires. NULL if not locked. Account is locked for 10 minutes after 3 failed attempts.';

-- Create index for faster lockout checks
CREATE INDEX IF NOT EXISTS idx_users_account_locked_until ON users(account_locked_until) WHERE account_locked_until IS NOT NULL;
