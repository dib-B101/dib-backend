UPDATE member
SET phone_number = regexp_replace(phone_number, '[-[:space:]]', '', 'g');

ALTER TABLE member
    ADD CONSTRAINT uq_member_phone_number UNIQUE (phone_number);
