CREATE DATABASE LensSystemDB;
GO

USE LensSystemDB;
GO

CREATE TABLE Users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
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

-- device models
CREATE TABLE DeviceModels (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    type VARCHAR(30) NOT NULL,
    brand NVARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    model NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    rental_price DECIMAL(12,0) NOT NULL,
    deposit_amount DECIMAL(12,0) NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_DeviceModels_Brand_Model 
        UNIQUE (brand, model), 

    CONSTRAINT CK_DeviceModels_RentalPrice
        CHECK (rental_price >= 0),

    CONSTRAINT CK_DeviceModels_Deposit
        CHECK (deposit_amount >= 0)
);
GO

-- devices
CREATE TABLE Devices (
    id INT IDENTITY(1,1) PRIMARY KEY,
    device_model_id INT NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Devices_DeviceModels
        FOREIGN KEY (device_model_id)
        REFERENCES DeviceModels(id),

    CONSTRAINT CK_Devices_Status
        CHECK (status IN ('AVAILABLE', 'RENTING'))
);
GO

INSERT INTO Users (username, password, full_name, email, phone, role)
VALUES ('admin', 'admin', N'Administrator', 'admin@lens.com', '0900000000', 'ADMIN');
GO
