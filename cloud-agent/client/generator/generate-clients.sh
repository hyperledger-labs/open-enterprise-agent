#!/bin/bash
set -e

# generate kotlin models
yarn openapi-generator-cli generate \
	-g kotlin \
	-i ../../service/api/http/cloud-agent-openapi-spec.yaml \
	-o ../kotlin \
	--ignore-file-override ../kotlin/.openapi-generator-ignore \
	--additional-properties=packageName=org.hyperledger.identus.client,serializationLibrary=gson,enumPropertyNaming=UPPERCASE

# generate typescript models
yarn openapi-generator-cli generate \
	-g typescript \
	-i ../../service/api/http/cloud-agent-openapi-spec.yaml \
	-o ../typescript \
	--ignore-file-override ../typescript/.openapi-generator-ignore
