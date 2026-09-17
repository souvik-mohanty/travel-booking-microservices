CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,

    -- identity-service user this profile belongs to. One profile per user.
    user_id UUID NOT NULL,

    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    date_of_birth DATE,
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),

    emergency_contact_name VARCHAR(150),
    emergency_contact_phone VARCHAR(30),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_user_profiles_user UNIQUE (user_id)
);
