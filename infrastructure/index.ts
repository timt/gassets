import * as aws from "@pulumi/aws";
import * as pulumi from "@pulumi/pulumi";

// Replace with your actual public key path
const deployKeyPath = process.env.DEPLOY_SSH_KEY

const publicKey = require("fs").readFileSync(`${deployKeyPath}.pub`, "utf-8");

const keyPair = new aws.ec2.KeyPair("gassets-key", {
  publicKey: publicKey,
});
const sg = new aws.ec2.SecurityGroup("web-sg", {
  ingress: [
    { protocol: "tcp", fromPort: 22, toPort: 22, cidrBlocks: ["0.0.0.0/0"] },
    { protocol: "tcp", fromPort: 80, toPort: 80, cidrBlocks: ["0.0.0.0/0"] }, // 👈 Allow HTTP
  ],
  egress: [
    { protocol: "-1", fromPort: 0, toPort: 0, cidrBlocks: ["0.0.0.0/0"] },
  ],
});

const ami = aws.ec2.getAmi({
  filters: [
    { name: "name", values: ["amzn2-ami-hvm-*-x86_64-gp2"] },
    { name: "owner-alias", values: ["amazon"] },
  ],
  mostRecent: true,
});

const server = new aws.ec2.Instance("gassets-instance", {
  ami: ami.then(a => a.id),
  instanceType: "t2.micro",
  keyName: keyPair.keyName,
  vpcSecurityGroupIds: [sg.id],
  tags: { Name: "gassets" },
  associatePublicIpAddress: true,
});

export const publicIp = server.publicIp;

const hostedZone = aws.route53.getZone({ name: "allout.click" });

const dnsRecord = new aws.route53.Record("assets-subdomain", {
  name: "assets.allout.click",
  zoneId: hostedZone.then(zone => zone.zoneId),
  type: "A",
  ttl: 300,
  records: [server.publicIp],
});