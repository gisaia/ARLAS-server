[Helm](helm.sh) is a package management system for kubernetes applications. Its packaging format is called *charts*. A Helm chart is implemented for arlas-server.

Once you have access to a kubernetes cluster, & [installed](https://docs.helm.sh/using_helm/#installing-helm) Helm, you can install the arlas-server chart. See the following instructions:

```bash
# Download latest ARLAS-server archive
IFS=',' read -r ARLAS_server_tag ARLAS_server_tarball <<< $(curl -s https://api.github.com/repos/gisaia/ARLAS-server/releases/latest | jq -r '"\(.tag_name),\(.tarball_url)"'); \
    echo "Downloading latest ARLAS server version $ARLAS_server_tag"; \
    ARLAS_server_commit=$(curl -s "https://api.github.com/repos/gisaia/ARLAS-server/git/refs/tags/$ARLAS_server_tag" | jq -r '.object.sha' | head -c 7); \
    curl -L -s "$ARLAS_server_tarball" | tar xz; \
    cd "gisaia-ARLAS-server-$ARLAS_server_commit"

# Install helm chart
helm install -f my_values.yaml packaging/helm/arlas-server
```

, where `my_values.yaml` contains your custom configuration settings for the chart.

Documentation about the chart's configuration settings is available [here](../../docs/arlas-server-configuration.md#helm-configuration)
