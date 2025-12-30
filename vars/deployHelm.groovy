def call(deploy = "") {

    sh """#!/usr/local/bin/python3
import os
import subprocess
import sys
import yaml

deploy = "${deploy}"
value_file = os.listdir("deploy")
if os.path.exists("out") is False:
    os.mkdir("out")

for file in value_file:
    if file.endswith(".yaml"):
        with open(f"deploy/{file}", 'r') as filename:
            try:
                values = yaml.load(filename, Loader=yaml.FullLoader)
                chart = values["metadata"]["chart"]
                name = values["metadata"]["name"]
                namespace = values["metadata"]["namespace"]
                chartVersion = values["metadata"]["chartVersion"]
                repoUrl = values["metadata"]["repoUrl"] if "repoUrl" in values["metadata"] else None
                chart_name = chart.split("/")[-1]
            except yaml.YAMLError as exc:
                print(exc)
    if repoUrl is not None:
        helm_repo_cmd = f'helm repo add {name} {repoUrl}'
        subprocess.run(helm_repo_cmd, shell=True, capture_output=True, text=True)
    if deploy == "true":
        helm_cmd = f'helm upgrade --install {name} {chart} --version {chartVersion} --namespace {namespace} --create-namespace --values deploy/{file} --take-ownership'
    else:
        helm_cmd = f'helm template {name} {chart} --version {chartVersion} --namespace {namespace} --create-namespace --values deploy/{file} > out/{name}.yaml'
    result = subprocess.run(helm_cmd, shell=True, capture_output=True, text=True)
    print(helm_cmd)
    if result.returncode != 0:
        print(f"Error running helm command: {result.stderr}")
        sys.exit(1)
"""
}
