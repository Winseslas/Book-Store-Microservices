# Dockerfile pour PostgreSQL
FROM postgres:17.2

# Définir les variables d'environnement
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV PGDATA=/var/lib/postgresql/data

# Copier des fichiers initiaux (si nécessaires)
COPY init.sql /docker-entrypoint-initdb.d/

# Exposer le port par défaut
EXPOSE 5432


# # Dockerfile pour pgAdmin
# FROM dpage/pgadmin4:8.14.0

# # Définir les variables d'environnement
# ENV PGADMIN_DEFAULT_EMAIL=admin.pgadmin@localhost.ms
# ENV PGADMIN_DEFAULT_PASSWORD=ms_pgadmin_password
# ENV PGADMIN_CONFIG_SERVER_MODE=False
# ENV GUNICORN_CMD_ARGS=--timeout 120

# # Exposer le port HTTP
# EXPOSE 80


# # Dockerfile pour MongoDB
# FROM mongo:latest

# # Définir les variables d'environnement
# ENV MONGO_INITDB_ROOT_USERNAME=admin
# ENV MONGO_INITDB_ROOT_PASSWORD=pass

# # Exposer le port par défaut
# EXPOSE 27017


# # Dockerfile pour mongo-express
# FROM mongo-express:1.0.2-20-alpine3.19

# # Définir les variables d'environnement
# ENV ME_CONFIG_MONGODB_SERVER=mongodb
# ENV ME_CONFIG_MONGODB_ENABLE_ADMIN=false
# ENV ME_CONFIG_MONGODB_ADMINUSERNAME=admin
# ENV ME_CONFIG_MONGODB_ADMINPASSWORD=pass
# ENV ME_CONFIG_BASICAUTH_USERNAME=admin
# ENV ME_CONFIG_BASICAUTH_PASSWORD=tribes

# # Exposer le port par défaut
# EXPOSE 8081


# # Dockerfile pour Mailhog
# FROM mailhog/mailhog:v1.0.1

# # Exposer les ports pour le service SMTP et l'interface web
# EXPOSE 1025 8025
