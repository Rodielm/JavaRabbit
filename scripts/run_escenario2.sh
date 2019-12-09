# Borrar datos
rm ../deploy/data/subidas/*.png
rm ../deploy/data/procesadas/*.png
rm ../deploy/data/*.csv

# Iniciar los contenedores para el experimento
./containerctl.sh start rabbitmq-broker
sleep 10
./containerctl.sh start worker1 
sleep 5
./containerctl.sh start worker2 
sleep 5
./containerctl.sh start stats
sleep 5
./containerctl.sh start web-server
sleep 5

# Realizar las peticiones POST a la aplicación Web
./peticiones.sh

# Al finalizar obtener el fichero con los tiempos
cp ../deploy/data/tiempo.csv ../results/escenario2_tiempos.csv

