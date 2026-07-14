DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS houses CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS location_type;


CREATE TYPE location_type AS ENUM (
    'COUNTRY',
    'REGION',
    'DISTRICT',
    'MUNICIPALITY',
    'LOCALITY'
);


CREATE TABLE users (
    id_user SERIAL PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    token UUID NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);


CREATE TABLE locations (
    id_loc SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type location_type NOT NULL,
    parent_id INT,

    CONSTRAINT fk_parent
        FOREIGN KEY (parent_id)
        REFERENCES locations(id_loc)
        ON DELETE SET NULL
);


CREATE TABLE houses (
    id_house SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    location_id INT NOT NULL,
    area_sq_mt INT NOT NULL CHECK (area_sq_mt >= 0),
    price_per_night NUMERIC NOT NULL CHECK (price_per_night > 0),
    description VARCHAR(500) NOT NULL,
    owner_id INT NOT NULL,

    CONSTRAINT fk_location
        FOREIGN KEY (location_id)
        REFERENCES locations(id_loc)
        ON DELETE CASCADE,

    CONSTRAINT fk_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id_user)
        ON DELETE CASCADE
);


CREATE TABLE bookings (
    id_booking SERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    user_id INT NOT NULL,
    house_id INT NOT NULL,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id_user)
        ON DELETE CASCADE,

    CONSTRAINT fk_house
        FOREIGN KEY (house_id)
        REFERENCES houses(id_house)
        ON DELETE CASCADE,

    CONSTRAINT check_dates
        CHECK (end_date >= start_date)
);

CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE bookings
ADD CONSTRAINT no_overlap
EXCLUDE USING gist (
    house_id WITH =,
    daterange(start_date, end_date, '[]') WITH &&
);

CREATE INDEX idx_house_location ON houses(location_id);
CREATE INDEX idx_booking_house ON bookings(house_id);
CREATE INDEX idx_booking_dates ON bookings(start_date, end_date);
