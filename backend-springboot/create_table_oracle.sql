-- Oracle Table Creation Script for FLOWAI_USERS
-- Run this script in SQL*Plus as SSTOPR1 user

-- Drop table if exists (optional - only if you want to recreate)
-- DROP TABLE FLOWAI_USERS CASCADE CONSTRAINTS;

-- Create FLOWAI_USERS table
CREATE TABLE FLOWAI_USERS (
    NTID VARCHAR2(50) PRIMARY KEY,
    EMAIL VARCHAR2(100) NOT NULL,
    ACCOUNT VARCHAR2(100),
    ACCOUNT_ID NUMBER,
    PASSWORD VARCHAR2(255),
    ROLE VARCHAR2(20) NOT NULL,
    CREATED_AT TIMESTAMP NOT NULL,
    ACTIVE NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT UK_FLOWAI_USERS_EMAIL UNIQUE (EMAIL)
);

-- Create index on email for faster lookups
CREATE INDEX IDX_FLOWAI_USERS_EMAIL ON FLOWAI_USERS(EMAIL);

-- Create index on role for faster filtering
CREATE INDEX IDX_FLOWAI_USERS_ROLE ON FLOWAI_USERS(ROLE);

-- Verify table creation
SELECT table_name, num_rows 
FROM user_tables 
WHERE table_name = 'FLOWAI_USERS';

-- Check table structure
DESC FLOWAI_USERS;

-- Test insert (optional)
-- INSERT INTO FLOWAI_USERS (NTID, EMAIL, ROLE, CREATED_AT, ACTIVE) 
-- VALUES ('test', 'test@example.com', 'USER', SYSTIMESTAMP, 1);
-- COMMIT;

-- Query to see all data
-- SELECT * FROM FLOWAI_USERS;
