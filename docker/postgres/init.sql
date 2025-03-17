-- Drop databases if they exist
DROP DATABASE IF EXISTS usermanager;
DROP DATABASE IF EXISTS bookstockmanager;
DROP DATABASE IF EXISTS notificationmanager;

-- Create fresh databases
CREATE DATABASE usermanager;
CREATE DATABASE bookstockmanager;
CREATE DATABASE notificationmanager;

-- Set up usermanager database
\c usermanager;

-- Create and configure schema
DROP SCHEMA IF EXISTS bookstore CASCADE;
CREATE SCHEMA bookstore;

-- Set ownership and permissions
ALTER SCHEMA bookstore OWNER TO postgres;
GRANT ALL ON SCHEMA bookstore TO postgres;
GRANT ALL ON ALL TABLES IN SCHEMA bookstore TO postgres;
GRANT ALL ON ALL SEQUENCES IN SCHEMA bookstore TO postgres;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA bookstore TO postgres;

-- Set search path
ALTER DATABASE usermanager SET search_path TO bookstore,public;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set default privileges
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA bookstore
GRANT ALL ON TABLES TO postgres;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA bookstore
GRANT ALL ON SEQUENCES TO postgres;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA bookstore
GRANT ALL ON FUNCTIONS TO postgres;

-- Create test table to verify permissions
CREATE TABLE IF NOT EXISTS bookstore.test_table (id SERIAL PRIMARY KEY);
DROP TABLE IF EXISTS bookstore.test_table;

-- Set up bookstockmanager database
\c bookstockmanager;
DROP SCHEMA IF EXISTS bookstore CASCADE;
CREATE SCHEMA bookstore;
ALTER SCHEMA bookstore OWNER TO postgres;
GRANT ALL ON SCHEMA bookstore TO postgres;
ALTER DATABASE bookstockmanager SET search_path TO bookstore,public;

-- Set up notificationmanager database
\c notificationmanager;
DROP SCHEMA IF EXISTS bookstore CASCADE;
CREATE SCHEMA bookstore;
ALTER SCHEMA bookstore OWNER TO postgres;
GRANT ALL ON SCHEMA bookstore TO postgres;
ALTER DATABASE notificationmanager SET search_path TO bookstore,public;

-- Verify all databases are created
SELECT datname FROM pg_database WHERE datname IN ('usermanager', 'bookstockmanager', 'notificationmanager');
