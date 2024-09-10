-- Drop user first if they exist
DROP USER IF EXISTS 'jobportal'@'%';

-- Now create user with prop privileges
CREATE USER 'jobportal'@'%' IDENTIFIED BY 'jobportal';

GRANT ALL PRIVILEGES ON * . * TO 'jobportal'@'%';