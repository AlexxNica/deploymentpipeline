#!/bin/bash
#*******************************************************************************
#  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
#*******************************************************************************
SCRIPT_DIR=`dirname "${BASH_SOURCE[0]}"`
SCRIPT_DIR=$(cd $SCRIPT_DIR;pwd)

APP_NAME="${APP_NAME:-oppv-widget}"
IMAGE_NAME="${IMAGE_NAME}"
K8S_CTX="${K8S_CTX:-development}"
APP_NS="${APP_NS:-default}"
TARGET_ENV="${TARGET_ENV:-DEV}"
VERSION="${VERSION}"
LABEL_VERSION="${LABEL_VERSION}"
REPLICA_COUNT=${REPLICA_COUNT}
SERVICE_ACCOUNT="${SERVICE_ACCOUNT}"

KUBECTL="${KUBECTL:-kubectl}"
KUBECTL_OPTS="${KUBECTL_OPTS:-}"
#--kubeconfig=${ROOT_DIR}/kubectl.conf

RESOURCES_LIMITS_CPU=${RESOURCES_LIMITS_CPU:-}
RESOURCES_LIMITS_MEMORY=${RESOURCES_LIMITS_MEMORY:-}
RESOURCES_REQUESTS_CPU=${RESOURCES_REQUESTS_CPU:-}
RESOURCES_REQUESTS_MEMORY=${RESOURCES_REQUESTS_MEMORY:-}


function log {
	echo "[$(date +"%F %T")] $*"
}

function generate {
    TEMPLATE_SOURCE=$1
    GENERATED_TARGET=$2

eval "cat <<EOF
$(<${TEMPLATE_SOURCE})
EOF
" > ${GENERATED_TARGET}

    cat ${GENERATED_TARGET}
}


#PREFIX=${K8S_CTX}-${TARGET_ENV}-${APP_NS}-${APP_NAME}

#generate ${SCRIPT_DIR}/cfgmap.tpl.yml ${SCRIPT_DIR}/.${PREFIX}-cfgmap.yml
#generate ${SCRIPT_DIR}/deployment.tpl.yml ${SCRIPT_DIR}/.${PREFIX}-deployment.yml
#generate ${SCRIPT_DIR}/svc.tpl.yml ${SCRIPT_DIR}/.${PREFIX}-svc.yml


log "================================================================"
log "Application     : ${APP_NAME}"
log "Deploying Image : ${IMAGE_NAME}"
log "K8S Context     : ${K8S_CTX}"
log "K8S Namespace   : ${APP_NS}"
log "K8S Environment : ${TARGET_ENV}"
log "SCRIP_DIR"		 : ${SCRIPT_DIR}
log "KUBECTL"		 : ${KUBECTL}
log "KUBECTL_OPTS"   : ${KUBECTL_OPTS}
log "================================================================"

#secret file for aaf keyfile
${KUBECTL} create secret generic ${APP_NAME}-aaf-keystore --from-file ${SCRIPT_DIR}/keyfile \
  --namespace ${APP_NS} --context ${K8S_CTX} ${KUBECTL_OPTS}
 
#secret file for csilogging keystore
${KUBECTL} create secret generic ${APP_NAME}-keystore --from-file ${SCRIPT_DIR}/spm2.jks \
  --namespace ${APP_NS} --context ${K8S_CTX} ${KUBECTL_OPTS} 

# configmap
${KUBECTL} apply \
  --namespace ${APP_NS} \
  --context ${K8S_CTX} ${KUBECTL_OPTS} \
  -f ${SCRIPT_DIR}/${APP_NAME}-config.yml

# svc
${KUBECTL} apply \
  --namespace ${APP_NS} --context ${K8S_CTX} ${KUBECTL_OPTS} \
  -f ${SCRIPT_DIR}/${APP_NAME}-svc.yaml

# trigger deployment
${KUBECTL} apply \
  --namespace ${APP_NS} --context ${K8S_CTX} ${KUBECTL_OPTS} \
  -f ${SCRIPT_DIR}/${APP_NAME}-rc.yaml


if [ $? -ne 0 ]; then
    log "================================================================"
    log " Failure - ${APP_NAME} Deployment Failed!!"
    log "================================================================"
    exit 1;
else
    log "================================================================"
    log " Success - ${APP_NAME} Deployment Successfully!"
    log "================================================================"
fi
