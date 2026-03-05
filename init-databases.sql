-- This script runs automatically when the PostgreSQL container starts for the first time.
-- It creates the databases needed by the services that use PostgreSQL.

CREATE DATABASE yafd_orders;
CREATE DATABASE yafd_vouchers;
CREATE DATABASE yafd_payments;

-- Note: yafd_accounts is created automatically as POSTGRES_DB in docker-compose.yml

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE yafd_accounts TO yafd;
GRANT ALL PRIVILEGES ON DATABASE yafd_orders TO yafd;
GRANT ALL PRIVILEGES ON DATABASE yafd_vouchers TO yafd;
GRANT ALL PRIVILEGES ON DATABASE yafd_payments TO yafd;
