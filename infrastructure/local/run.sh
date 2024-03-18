#!/usr/bin/env bash

set -e

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)

Help() {
	# Display Help
	echo "Run an instance of the ATALA bulding-block stack locally"
	echo
	echo "Syntax: run.sh [-n/--name NAME|-p/--port PORT|-b/--background|-e/--env|-w/--wait|--network|-h/--help]"
	echo "options:"
	echo "-n/--name              Name of this instance - defaults to dev."
	echo "-p/--port              Port to run this instance on - defaults to 80."
	echo "-b/--background        Run in docker-compose daemon mode in the background."
	echo "-e/--env               Provide your own .env file with versions."
	echo "-w/--wait              Wait until all containers are healthy (only in the background)."
	echo "--network              Specify a docker network to run containers on."
	echo "--webhook              Specify webhook URL for agent events"
	echo "--webhook-api-key      Specify api key to secure webhook if required"
	echo "--debug                Run additional services for debug using docker-compose debug profile."
	echo "-h/--help              Print this help text."
	echo
}

POSITIONAL_ARGS=()

while [[ $# -gt 0 ]]; do
	case $1 in
	-n | --name)
		NAME="$2"
		shift # past argument
		shift # past value
		;;
	-p | --port)
		PORT="$2"
		shift # past argument
		shift # past value
		;;
	-b | --background)
		BACKGROUND="-d"
		shift # past argument
		;;
	-w | --wait)
		WAIT="--wait"
		shift # past argument
		;;
	-d | --dockerhost)
		DOCKERHOST="$2"
		shift # past argument
		shift # past value
		;;
	-e | --env)
		ENV_FILE="$2"
		shift # past argument
		shift # past value
		;;
	--network)
		NETWORK="$2"
		shift # past argument
		shift # past value
		;;
	--webhook)
		GLOBAL_WEBHOOK_URL="$2"
		shift # past argument
		shift # past value
		;;
	--webhook-api-key)
		GLOBAL_WEBHOOK_API_KEY="$2"
		shift # past argument
		shift # past value
		;;
	--debug)
		DEBUG="--profile debug"
		shift # past argument
		;;
	-h | --help)
		Help
		exit
		;;
	-* | --*)
		echo "Unknown option $1"
		Help
		exit 1
		;;
	*)
		POSITIONAL_ARGS+=("$1") # save positional arg
		shift                   # past argument
		;;
	esac
done

set -- "${POSITIONAL_ARGS[@]}" # restore positional parameters

if [[ -n $1 ]]; then
	echo "Last line of file specified as non-opt/last argument:"
	tail -1 "$1"
fi

NAME="${NAME:=local}"
PORT="${PORT:=80}"
ENV_FILE="${ENV_FILE:=${SCRIPT_DIR}/.env}"
NETWORK="${NETWORK:=${NAME}-prism}"
DOCKERHOST="${DOCKERHOST:=host.docker.internal}"

echo "NAME                   = ${NAME}"
echo "PORT                   = ${PORT}"
echo "ENV_FILE               = ${ENV_FILE}"
echo "NETWORK                = ${NETWORK}"
echo "GLOBAL_WEBHOOK_URL     = ${GLOBAL_WEBHOOK_URL}"
echo "GLOBAL_WEBHOOK_API_KEY = ${GLOBAL_WEBHOOK_API_KEY}"
echo "DOCKERHOST = ${DOCKERHOST}"

echo "--------------------------------------"
echo "Starting stack using docker compose"
echo "--------------------------------------"

# optionally pass variable if and only if the variable is set
if [ -n "$GLOBAL_WEBHOOK_URL" ]; then
	export GLOBAL_WEBHOOK_URL=${GLOBAL_WEBHOOK_URL}
fi
if [ -n "$GLOBAL_WEBHOOK_API_KEY" ]; then
	export GLOBAL_WEBHOOK_API_KEY=${GLOBAL_WEBHOOK_API_KEY}
fi

PORT=${PORT} NETWORK=${NETWORK} DOCKERHOST=${DOCKERHOST} docker compose \
	-p ${NAME} \
	${DEBUG} \
	-f ${SCRIPT_DIR}/../shared/docker-compose.yml \
	--env-file ${ENV_FILE} ${DEBUG} up ${BACKGROUND} ${WAIT}
