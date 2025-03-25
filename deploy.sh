#!/bin/bash

set -e

INFRA_DIR="./infrastructure"
KEY_PATH="$DEPLOY_SSH_KEY"
KEY_NAME="gassets-key"
USER="ec2-user"
PORT=8000
IMAGE_NAME="gassets"
TAR_NAME="gassets.tar"

echo "▶ Building Docker image locally..."
docker build --platform=linux/amd64 -t $IMAGE_NAME .

echo "▶ Saving Docker image to $TAR_NAME..."
docker save $IMAGE_NAME > $TAR_NAME

echo "▶ Running Pulumi to provision infrastructure..."
cd $INFRA_DIR
pulumi up --yes
EC2_IP=$(pulumi stack output publicIp | tr -d '"')
cd -

echo "▶ Waiting for EC2 instance at $EC2_IP to become SSH-ready..."
while ! ssh -o ConnectTimeout=2 -o StrictHostKeyChecking=no -i $KEY_PATH $USER@$EC2_IP 'echo ready' 2>/dev/null; do
  sleep 2
done

echo "▶ Copying Docker image to EC2..."
scp -i $KEY_PATH $TAR_NAME $USER@$EC2_IP:/home/ec2-user/

echo "▶ Running setup commands remotely..."
ssh -i $KEY_PATH $USER@$EC2_IP << EOF
  sudo yum update -y
  sudo amazon-linux-extras install docker -y
  sudo systemctl start docker

  echo "▶ Loading image..."
  sudo docker load < gassets.tar

  echo "Stop any existing container using port 80"
  sudo docker rm -f gassets || true
  echo "▶ Running container..."
  sudo docker run -d -p 80:$PORT --name gassets gassets
EOF


echo "🚀 App deployed! Access it at: http://$EC2_IP:$PORT"