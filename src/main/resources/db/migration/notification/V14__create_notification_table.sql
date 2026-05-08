CREATE TABLE notification
(
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID       NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    content     VARCHAR(50)  NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_recipient_id ON notification (recipient_id);
CREATE INDEX idx_notification_recipient_id_is_read ON notification (recipient_id, is_read);