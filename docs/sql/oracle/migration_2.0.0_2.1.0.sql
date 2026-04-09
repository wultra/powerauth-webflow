-- Changeset powerauth-webflow/2.1.x/20260407-add-subject-id-to-audit-log.xml::1::Pavel Sindelar
-- Add column subject_id to audit_log
ALTER TABLE audit_log ADD subject_id VARCHAR2(256);

-- Changeset powerauth-webflow/2.1.x/20260407-add-subject-id-to-audit-log.xml::2::Pavel Sindelar
-- Create index on audit_log(subject_id)
CREATE INDEX audit_log_subject_id_idx ON audit_log(subject_id);
