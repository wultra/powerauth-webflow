-- Create a new table shedlock
CREATE TABLE shedlock (name VARCHAR(64) NOT NULL, lock_until TIMESTAMP WITHOUT TIME ZONE NOT NULL, locked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL, locked_by VARCHAR(255) NOT NULL, CONSTRAINT shedlock_pkey PRIMARY KEY (name));
