# Aplikacija za Upravljanje Kolesarskih Poti 🏞️

Namizna aplikacija zgrajena z Java in PostgreSQL za upravljanje kolesarskih poti po Sloveniji. Uporabniki lahko dodajajo, pregledajo in urejajo poti, izbirajo zanimivosti (POI – "points of interest") ter hranijo podrobne informacije o posameznih poteh in komentarjih.

## Funkcionalnosti

- Dodajanje nove pohodniške poti:
  - Ime poti, zahtevnost, dolžina, trajanje in opis
  - Začetno in končno mesto (izbirno iz spustnega seznama)
  - Več izbranih zanimivosti (POI)
- Prikaz komentarjev
- Sistem komentarjev za vsako pot
- Samodejno sledenje številu POI na poti z uporabo sprožilcev (triggers)
- Podprto z bazo PostgreSQL in lastnimi SQL funkcijami

## Tehnologije

- Java za uporabniški vmesnik
- PostgreSQL baza podatkov
- JDBC za povezavo z bazo
- SQL funkcije in sprožilci za logiko in podatkovno integriteto

---

## Struktura baze podatkov

### Tabele

```sql
-- Create the "routs" table
CREATE TABLE routs (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  length FLOAT NOT NULL,
  difficulty INTEGER NOT NULL,
  duration FLOAT NOT NULL,
  description TEXT,
  num_of_poi INTEGER NOT NULL DEFAULT 0,
  startLocation_id INTEGER NOT NULL,
  endLocation_id INTEGER NOT NULL,
  date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create the "route_POI" table
CREATE TABLE route_POI (
  id SERIAL PRIMARY KEY,
  poi_position INTEGER NOT NULL,
  route_id INTEGER NOT NULL,
  poi_id INTEGER NOT NULL
);

-- Create the "PointsOfInterest" table
CREATE TABLE PointsOfInterest (
  id SERIAL PRIMARY KEY,
  pointName VARCHAR(255) NOT NULL,
  description TEXT,
  type VARCHAR(255) NOT NULL,
  location_id INTEGER NOT NULL
);

-- Create the "Citys" table
CREATE TABLE Citys (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  decsripton TEXT,
  post_code VARCHAR(20) NOT NULL
);

-- Create the "Users" table
CREATE TABLE Users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  phone_num VARCHAR(20)
);

-- Create the "review_user" table
CREATE TABLE review_user (
  id SERIAL PRIMARY KEY,
  route_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  description TEXT,
  date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints and relationships after creating all tables
ALTER TABLE route_POI
  ADD CONSTRAINT fk_route FOREIGN KEY (route_id) REFERENCES routs(id),
  ADD CONSTRAINT fk_poi FOREIGN KEY (poi_id) REFERENCES PointsOfInterest(id);

ALTER TABLE routs
  ADD CONSTRAINT fk_startLocation FOREIGN KEY (startLocation_id) REFERENCES Citys(id),
  ADD CONSTRAINT fk_endLocation FOREIGN KEY (endLocation_id) REFERENCES Citys(id);

ALTER TABLE PointsOfInterest
  ADD CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES Citys(id);

ALTER TABLE review_user
  ADD CONSTRAINT fk_route_review FOREIGN KEY (route_id) REFERENCES routs(id),
  ADD CONSTRAINT fk_user_review FOREIGN KEY (user_id) REFERENCES Users(id);
```

### Functions

```sql
CREATE OR REPLACE FUNCTION login_user(emaili VARCHAR, passwordi VARCHAR)
RETURNS BOOLEAN AS $$
DECLARE
    stored_password VARCHAR;
BEGIN
    SELECT password INTO stored_password
    FROM Users
    WHERE email = emaili;

    IF stored_password IS NOT NULL AND stored_password = passwordi THEN
        RETURN TRUE;
    ELSE
        RETURN FALSE;
    END IF;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION register_user(usernamei VARCHAR, emaili VARCHAR, passwordi VARCHAR, phone_numi VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO Users (username, email, password, phone_num)
    VALUES (usernamei, emaili, passwordi, phone_numi);

    RETURN TRUE;
EXCEPTION
    WHEN unique_violation THEN
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_all_routes()
RETURNS TABLE (
    id INT,
    name varchar,
    length FLOAT,
    difficulty INT,
    duration FLOAT,
    description TEXT,
    num_of_poi INT,
    start_location_name varchar,
    end_location_name varchar,
    date_created TIMESTAMP
)
AS $$
BEGIN
    RETURN QUERY
    SELECT
        r.id,
        r.name,
        r.length,
        r.difficulty,
        r.duration,
        r.description,
        r.num_of_poi,
        sl.name AS start_location_name,
        el.name AS end_location_name,
        r.date_created
    FROM routs r
    JOIN citys sl ON r.startlocation_id = sl.id
    JOIN citys el ON r.endlocation_id = el.id;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION insert_route(
    p_name VARCHAR,
    p_length FLOAT,
    p_difficulty INT,
    p_duration FLOAT,
    p_description TEXT,
    p_start_location_name VARCHAR,
    p_end_location_name VARCHAR,
    p_pois INT[]
) RETURNS INT AS $$
DECLARE
    start_location_id INT;
    end_location_id INT;
    rout_id INT;
    p_id INT;
BEGIN
    SELECT id INTO start_location_id FROM citys WHERE name = p_start_location_name LIMIT 1;
    SELECT id INTO end_location_id FROM citys WHERE name = p_end_location_name LIMIT 1;

    INSERT INTO routs(name, length, difficulty, duration, description, startlocation_id, endlocation_id)
    VALUES (p_name, p_length, p_difficulty, p_duration, p_description, start_location_id, end_location_id)
    RETURNING id INTO rout_id;

    FOREACH p_id IN ARRAY p_pois LOOP
        INSERT INTO route_poi(route_id, poi_id) VALUES (rout_id, p_id);
    END LOOP;

    RETURN rout_id;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER update_num_of_poi_trigger
AFTER INSERT ON route_poi
FOR EACH ROW
EXECUTE FUNCTION update_num_of_poi();

CREATE OR REPLACE FUNCTION update_num_of_poi()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE routs
    SET num_of_poi = (SELECT COUNT(*) FROM route_poi WHERE route_id = NEW.route_id)
    WHERE id = NEW.route_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_num_of_poi_after_delete()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE routs
    SET num_of_poi = (SELECT COUNT(*) FROM route_poi WHERE route_id = OLD.route_id)
    WHERE id = OLD.route_id;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_num_of_poi_after_delete_trigger
AFTER DELETE ON route_poi
FOR EACH ROW
EXECUTE FUNCTION update_num_of_poi_after_delete();

CREATE OR REPLACE FUNCTION get_route_comments(route_idi INT)
RETURNS TABLE(comment_id INT, comment_text TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT c.id, c.description
    FROM review_user c
    WHERE c.route_id = route_idi;
END;
$$ LANGUAGE plpgsql;
``` 
