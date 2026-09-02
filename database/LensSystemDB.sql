CREATE DATABASE LensSystemDB;
GO

USE LensSystemDB;
GO

CREATE TABLE Users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',

    status VARCHAR(20) NOT NULL CONSTRAINT DF_Users_Status DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL  DEFAULT GETDATE(),

    CONSTRAINT CK_Users_Role
        CHECK (role IN ('CUSTOMER', 'ADMIN')),

    CONSTRAINT CK_Users_Status
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

INSERT INTO Users (username, password, full_name, email, phone, role)
VALUES ('admin', 'admin', N'Administrator', 'admin@lens.com', '0900000000', 'ADMIN');
GO
