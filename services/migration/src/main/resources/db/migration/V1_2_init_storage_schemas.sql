SET search_path TO storage;

CREATE TABLE media_files
(
    id             VARCHAR(255)                NOT NULL,
    created_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    original_name  VARCHAR(255)                NOT NULL,
    stored_name    VARCHAR(255)                NOT NULL,
    file_url       TEXT                        NOT NULL,
    file_size      BIGINT                      NOT NULL,
    is_public      BOOLEAN                     NOT NULL,
    download_count BIGINT,
    CONSTRAINT pk_media_files PRIMARY KEY (id)
);

CREATE TABLE video_streams
(
    id                  VARCHAR(255)                NOT NULL,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    source_service      VARCHAR(100)                NOT NULL,
    stream_url          TEXT,
    title               VARCHAR(255)                NOT NULL,
    description         TEXT,
    status              VARCHAR(20)                 NOT NULL,
    duration_seconds    BIGINT,
    resolution          VARCHAR(20),
    bitrate             INTEGER,
    format              VARCHAR(20),
    file_size           BIGINT,
    stored_file_url     TEXT,
    thumbnail_url       TEXT,
    owner_id            VARCHAR(36),
    processing_progress INTEGER,
    error_message       TEXT,
    CONSTRAINT pk_video_streams PRIMARY KEY (id)
);