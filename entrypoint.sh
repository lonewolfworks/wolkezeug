#!/bin/bash
printenv
cd $6

ls -alh 
java -jar /wolkezeug.jar ecs-push \
			-e $1 \
		    -t $3 \
    		-v app.artifactId=$4 \
    		-v app.version=$5 \
    		-v aws.region=$2 