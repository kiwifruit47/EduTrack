su postgres
psql
CREATE USER ejournal_usr WITH PASSWORD 'KWxu6SyPmPV62UWTjvtbXJOXRAhdxZPENSmSUO02TqokpJem9qPF9X5iCb2JR4cm';
CREATE DATABASE ejournal OWNER ejournal_usr;
GRANT ALL PRIVILEGES ON DATABASE ejournal TO ejournal_usr;