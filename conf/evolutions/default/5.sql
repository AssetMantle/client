# --- !Ups

ALTER TABLE BLOCKCHAIN."Classification"
    ADD COLUMN IF NOT EXISTS "classificationType" VARCHAR NOT NULL DEFAULT '';

UPDATE BLOCKCHAIN."Classification"
SET "classificationType" = 'ASSET'
WHERE "idString" = '1EG2j7AG28egAdW0ceRu2uUEuzTB6jxhr9T1ddvfkV8=';

UPDATE BLOCKCHAIN."Classification"
SET "classificationType" = 'IDENTITY'
WHERE "idString" = 'Tw96hXEJjSw_aQ9rNh0c_72wQUL5gzEdODohcVY4l6I='
   OR "idString" = 'eR64NW1RJJYvgUOwtvUla5EpcJLmAIRc3Wj4yL7JWY0=';

UPDATE BLOCKCHAIN."Classification"
SET "classificationType" = 'MAINTAINER'
WHERE "idString" = 'TsZqVsnKEht2QDXJ2GDnnYAP7U0tqcIM-zm-eEq9-L4=';

UPDATE BLOCKCHAIN."Classification"
SET "classificationType" = 'ORDER'
WHERE "idString" = 'M9284TlAYzhf-uMXP2uJO65TY9tELWw5MzeReVv9pzw=';

# --- !Downs