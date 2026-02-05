-- Add PASSWORD column to FLOWAI_USERS table
-- Run this script on your Oracle database

ALTER TABLE FLOWAI_USERS 
ADD PASSWORD VARCHAR2(255);

-- Optional: Add comment to the column
COMMENT ON COLUMN FLOWAI_USERS.PASSWORD IS 'User password (plain text for now, should be hashed in future)';

-- Verify the column was added
SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE 
FROM USER_TAB_COLUMNS 
WHERE TABLE_NAME = 'FLOWAI_USERS' 
AND COLUMN_NAME = 'PASSWORD';
