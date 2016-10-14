#!/bin/bash

## compila tudo
find ./src/ -name *.java | xargs javac -cp ./lib/jgroups-3.6.4.Final.jar -d ./build/

## executa
java -cp ./lib/jgroups-3.6.4.Final.jar:./build/ bancobeans.BancoServidor
