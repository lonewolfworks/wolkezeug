#!/bin/bash
java -jar herman.jar ecs-push \
			-e $DEPLOY_ENV \
		    -t $TIMEOUT \
    		-v bamboo.maven.artifactId=$ARTIFACT \
    		-v bamboo.maven.version=$VERSION \
    		-v aws.region=$REGION \
    		-v bamboo.planRepository.revision=$PLANREVISION \
    		-v bamboo.deploy.version=$VERSION