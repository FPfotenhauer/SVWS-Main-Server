-- Add additional school information fields from SVWS
ALTER TABLE schule
    ADD COLUMN strasse VARCHAR(255),
    ADD COLUMN hausnummer VARCHAR(255),
    ADD COLUMN hausnummer_zusatz VARCHAR(255),
    ADD COLUMN plz VARCHAR(10),
    ADD COLUMN ort VARCHAR(255),
    ADD COLUMN telefon VARCHAR(255),
    ADD COLUMN fax VARCHAR(255),
    ADD COLUMN email VARCHAR(255),
    ADD COLUMN homepage VARCHAR(512),
    ADD COLUMN schulleiter VARCHAR(255),
    ADD COLUMN schulleiter_telefon VARCHAR(255),
    ADD COLUMN schulleiter_email VARCHAR(255),
    ADD COLUMN kreis VARCHAR(255),
    ADD COLUMN schulamt VARCHAR(255),
    ADD COLUMN schulnummer2 VARCHAR(255),
    ADD COLUMN schulstatus VARCHAR(255);
