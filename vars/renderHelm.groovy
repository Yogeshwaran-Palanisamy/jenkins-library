def call() {
    sh """
import os
import subprocess
import sys
import yaml

value_file = os.listdir("deploy")
print(value_file)
for file in value_file:
    if file.endswith(".yaml"):
        with open(f"deploy/{file}", 'r') as filename:
            try:
                values = yaml.load(filename, Loader=yaml.FullLoader)
                chart = values["metadata"]["chart"]
                name = values["metadata"]["name"]
                namespace = values["metadata"]["namespace"]
                chart_name = chart.split("/")[-1]
            except yaml.YAMLError as exc:
                print(exc)
    helm_cmd = f'helm template {name} {chart} --namespace {namespace} --create-namespace --values deploy/{file} > out/{name}.yaml'
    result = subprocess.run(helm_cmd, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error running helm command: {result.stderr}")
        sys.exit(1)
    """
}
