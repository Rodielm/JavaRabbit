cd ..

# Directorios para almacenar las imágenes
mkdir -p deploy/data/subidas
mkdir -p deploy/data/procesadas

# WEB-APP

# Copia del servidor dinámico a su directorio 
mkdir -p deploy/web-server/server
cp ../dynamic-http-server/target/dynamic-server-1.0.jar deploy/web-server/server
cp ../dynamic-http-server/config.ini deploy/web-server/server/config.ini

mkdir -p deploy/web-server/config
mkdir -p deploy/web-server/dynamic

# Generación y copia de la aplicación que se ejecutará en el servidor dinámico a su directorio
cd src
cd web-app
mvn clean package

# WORKER
cd ..
# Generación y copia de la aplicación que se ejecutará en los workers
cd worker
mvn clean package

# STATS
cd ..
# Generación y copia de la aplicación que recoge las estadísticas
cd stats
mvn clean package

cd ..
cd ../scripts
