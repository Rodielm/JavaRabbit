#!/bin/bash
# Time to wait before making another POST request (in seconds)
time=0.01
rm peticiones.sh
declare -a ops
ops=(edge gray blur)
imgDir=../images
[[ -z $(ls $imgDir/*.png 2> /dev/null) ]] && tar xzvf $imgDir/images.tgz -C $imgDir
for i in $(seq 0 99); do
    indice=$i%3;
    echo "curl -H 'Expect:' -F \"ACTION=${ops[$indice]}\" -F \"IMAGE=@$imgDir/images-$i.png\" http://localhost:8080/images" >> peticiones.sh
    echo "sleep $time" >> peticiones.sh
done
chmod u+x peticiones.sh
