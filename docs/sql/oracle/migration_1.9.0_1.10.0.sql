-- Create a new table shedlock
CREATE TABLE shedlock (name VARCHAR2(64) NOT NULL, lock_until TIMESTAMP NOT NULL, locked_at TIMESTAMP NOT NULL, locked_by VARCHAR2(255) NOT NULL, CONSTRAINT PK_SHEDLOCK PRIMARY KEY (name));
