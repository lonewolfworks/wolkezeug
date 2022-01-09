#!/bin/bash
printenv

java -jar wolkezeug.jar ecs-push \
			-e $1 \
		    -t $3 \
    		-v bamboo.maven.artifactId=$4 \
    		-v bamboo.maven.version=$5 \
    		-v aws.region=$2 \
    		-v bamboo.planRepository.revision=$PLANREVISION \
    		-v bamboo.deploy.version=$5