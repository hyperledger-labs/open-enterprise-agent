#!/bin/bash
set -e

# generate kotlin models
yarn openapi-generator-cli generate \
    -g kotlin \
    -i ../../service/api/http/prism-agent-openapi-spec.yaml \
    -o ../kotlin \
    --ignore-file-override ../kotlin/.openapi-generator-ignore \
    --additional-properties=packageName=io.iohk.atala.prism,serializationLibrary=gson,enumPropertyNaming=UPPERCASE

# generate typescript models
yarn openapi-generator-cli generate \
    -g typescript \
    -i ../../service/api/http/prism-agent-openapi-spec.yaml \
    -o ../typescript \
    --ignore-file-override ../typescript/.openapi-generator-ignore

# generate python models
# yarn openapi-generator-cli generate -g python -i oas.yml --skip-validate-spec -o ../python --ignore-file-override ../python/.openapi-generator-ignore
