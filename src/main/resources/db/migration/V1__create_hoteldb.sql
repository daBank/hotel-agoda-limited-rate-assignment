CREATE TABLE cities (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100)
);

CREATE INDEX cities_name
ON cities(name);

CREATE TABLE rooms (
    hotel_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    city_id VARCHAR(36),
    price DECIMAL(10, 2),
    CONSTRAINT fk_city FOREIGN KEY(city_id) REFERENCES cities(id)
);