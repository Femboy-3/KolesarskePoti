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
CREATE TABLE citys (
    id SERIAL PRIMARY KEY,
    name TEXT
);

CREATE TABLE pois (
    id SERIAL PRIMARY KEY,
    name TEXT
);

CREATE TABLE routs (
    id SERIAL PRIMARY KEY,
    name TEXT,
    length FLOAT,
    difficulty INT,
    duration FLOAT,
    description TEXT,
    startlocation_id INT REFERENCES citys(id),
    endlocation_id INT REFERENCES citys(id),
    num_of_poi INT DEFAULT 0
);

CREATE TABLE route_poi (
    route_id INT REFERENCES routs(id),
    poi_id INT REFERENCES pois(id)
);

CREATE TABLE comments (
    comment_id SERIAL PRIMARY KEY,
    route_id INT REFERENCES routs(id),
    comment_text TEXT
);


CREATE OR REPLACE FUNCTION insert_route(
    p_name VARCHAR,
    p_length FLOAT,
    p_difficulty INT,
    p_duration FLOAT,
    p_description TEXT,
    p_start_location_name VARCHAR,
    p_end_location_name VARCHAR,
    p_pois INT[]
) RETURNS void AS $$
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
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_route_comments(route_id INT) 
RETURNS TABLE(comment_id INT, comment_text TEXT) AS $$
BEGIN
    RETURN QUERY 
    SELECT c.comment_id, c.comment_text
    FROM comments c
    WHERE c.route_id = route_id;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION increase_poi_count() RETURNS TRIGGER AS $$
BEGIN
    UPDATE routs SET num_of_poi = num_of_poi + 1 WHERE id = NEW.route_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_increase_poi
AFTER INSERT ON route_poi
FOR EACH ROW
EXECUTE FUNCTION increase_poi_count();


CREATE OR REPLACE FUNCTION decrease_poi_count() RETURNS TRIGGER AS $$
BEGIN
    UPDATE routs SET num_of_poi = num_of_poi - 1 WHERE id = OLD.route_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrease_poi
AFTER DELETE ON route_poi
FOR EACH ROW
EXECUTE FUNCTION decrease_poi_count();
``` 
