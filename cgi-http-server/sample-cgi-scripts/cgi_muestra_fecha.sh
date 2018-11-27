#!/bin/bash
printf "HTTP/1.1 200 OK\r\n"
printf "Content-Type: text/html\r\n"
msg="<html><head><meta charset="UTF-8"></head><body><h1>Contenido generado desde un script</h1>"
msg="$msg <p>Fecha: $(date)</p></body></html>"
printf "Content-Length: ${#msg}\r\n"
printf "\r\n"
printf "$msg"
