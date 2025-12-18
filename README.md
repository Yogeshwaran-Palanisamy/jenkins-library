# gha-runners

Manifests and notes to deploy self-hosted GitHub Actions runners on Kubernetes using
actions-runner-controller (ARC) and Cert-Manager for certificates.

**Documentation & resources**

- **Cert-Manager docs:** https://cert-manager.io/docs/
- **Cert-Manager Helm install:** https://cert-manager.io/docs/installation/helm/
- **Actions Runner Controller (ARC) docs:** https://actions-runner-controller.github.io/actions-runner-controller/
- **ARC GitHub repo:** https://github.com/actions-runner-controller/actions-runner-controller
- **GitHub self-hosted runners:** https://docs.github.com/en/actions/hosting-your-own-runners

**Overview**

This repository contains example values and notes to install and configure:

- `cert-manager` to provide TLS certificates for controllers and webhooks.
- `actions-runner-controller` to manage self-hosted runners (scale sets or single runners).

Files in this repo:

- [runners/gha-controller-value.yaml](runners/gha-controller-value.yaml) — Helm values for ARC controller.
- [runners/gha-scale-set-value.yaml](runners/gha-scale-set-value.yaml) — Example scale-set values for runners.

Prerequisites

- A Kubernetes cluster with Helm installed.
- `kubectl` configured for the target cluster.
- A GitHub personal access token or app credentials with appropriate repo/org permissions.

Quickstart

1) Install cert-manager (example using Helm):

```bash
curl -LO https://cert-manager.io/public-keys/cert-manager-keyring-2021-09-20-1020CF3C033D4F35BAE1C19E1226061C665DF13E.gpg

helm install \
  cert-manager oci://quay.io/jetstack/charts/cert-manager \
  --version v1.19.2 \
  --namespace cert-manager \
  --create-namespace \
  --verify \
  --keyring ./cert-manager-keyring-2021-09-20-1020CF3C033D4F35BAE1C19E1226061C665DF13E.gpg \
  --set crds.enabled=true
```

See the Cert-Manager docs for Helm installation options and newer versions.

2) Install actions-runner-controller (example using Helm values in this repo):

```bash
helm install -f runners/gha-controller-value.yaml \
  actions-controller oci://ghcr.io/actions/actions-runner-controller-charts/gha-runner-scale-set-controller \
  --namespace gha-controller --create-namespace
```


3) Install a runner scale-set (required):

```bash
helm install -f runners/gha-scale-set-value.yaml \
  actions-runner oci://ghcr.io/actions/actions-runner-controller-charts/gha-runner-scale-set \
  --namespace gha-runner --create-namespace
```

The runner scale-set (or equivalent runner resources) is required to create actual runner pods — the controller provides CRDs and reconciliation logic but does not by itself create runner workloads.

4) Create a Kubernetes Secret with your GitHub token (example):

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: controller-manager
  namespace: gha-runner
type: Opaque
data:
  # Base64-encoded GitHub token (replace with your base64 value)
  github_token: <base64-encoded-token>
```

Notes

- Replace versions and chart locations with the most current stable releases.
- Adjust namespaces and RBAC as required by your cluster policies.

Next steps

- Review and edit the values files in the `runners/` folder to match your GitHub org/repo settings.
- Refer to the linked documentation for advanced features like autoscaling, custom runners, and certificate configuration.

If you'd like, I can also:

- Add a quick script to create the GitHub secret from a raw token.
- Create a minimal example scale-set manifest and validate Helm values.
 