-- Drop stale Hibernate-generated check constraints on enum columns.
-- Hibernate 7 auto-creates CHECK constraints for @Enumerated(STRING) columns
-- but ddl-auto:update does not update them when new enum values are added.
ALTER TABLE IF EXISTS design_case DROP CONSTRAINT IF EXISTS design_case_status_check;
