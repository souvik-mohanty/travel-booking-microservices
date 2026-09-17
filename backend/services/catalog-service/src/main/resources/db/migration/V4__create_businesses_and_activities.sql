CREATE TABLE businesses (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    phone VARCHAR(100),
    email VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_businesses_owner_id
    ON businesses(owner_id);


CREATE TABLE activities (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    location VARCHAR(255),
    price NUMERIC(10,2) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    max_participants INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_activities_business
        FOREIGN KEY (business_id)
        REFERENCES businesses(id)
);

CREATE INDEX idx_activities_business_id
    ON activities(business_id);
