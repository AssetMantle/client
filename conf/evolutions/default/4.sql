# --- !Ups

ALTER TABLE BLOCKCHAIN."Maintainer"
    DROP CONSTRAINT IF EXISTS Maintainer_ClassificationID;

ALTER TABLE BLOCKCHAIN."Maintainer"
    RENAME COLUMN "classificationID" TO "maintainedClassificationID";

ALTER TABLE BLOCKCHAIN."Maintainer"
    ADD CONSTRAINT Maintainer_MaintainedClassificationID FOREIGN KEY ("maintainedClassificationID") REFERENCES BLOCKCHAIN."Classification" ("id");
# --- !Downs