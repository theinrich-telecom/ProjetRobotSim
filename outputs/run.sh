#!/bin/sh

java -jar WebServer.jar &
java -jar Microservice.jar &
java -jar RobotSim.jar
