package it.uniroma2.cloud;

public enum PROVIDER {
	AWS_EC2 {
		@Override
		public String toString() {
			return "aws-ec2";
		}
	},
	CLOUDSTACK {
		@Override
		public String toString() {
			return "cloudstack";
		}
	}
}
