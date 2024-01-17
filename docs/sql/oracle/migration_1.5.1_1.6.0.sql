-- Changeset powerauth-nextstep/1.6.x/20240116-correct-userid-nullable.xml::1::Zdenek Cerny
-- Make user_id column in table ns_otp_storage nullable
ALTER TABLE ns_otp_storage MODIFY user_id NULL;