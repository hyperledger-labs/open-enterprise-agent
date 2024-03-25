# How to run issuance flow

## Prerequisites

- Docker installed v2.24.0 or later
- Python 3 with the following packages installed
  - [requests](https://pypi.org/project/requests/)
  - [pyjwt](https://pyjwt.readthedocs.io/en/stable/)
  - [cryptography](https://cryptography.io/en/latest/)
- Virtual environment (optional)

Example of the script to install the required packages in a virtual environment:
```shell
python -m venv {path-to-the-project-dir}/open-enterprise-agent/examples/st-oidc4vc/python-env
source {path-to-the-project-dir}/open-enterprise-agent/examples/st-oidc4vc/python-env/bin/activate
pip install requests pyjwt cryptography
```

- the latest Cloud Agent image is built and available in the local Docker registry

```shell
sbt docker:publishLocal
```

### 1. Spin up the agent stack with pre-configured Keycloak

```bash
docker-compose up --build
```

This builds a custom Keycloak image with OIDC4VC plugin.
The Keycloak UI is available at `http://localhost:9980` and the admin username is `admin` with password `admin`.

### 2. Run the issuance demo script

```bash
python demo.py
```

- 2.1 Follow the instructions in the terminal. The holder will then be asked to log in via a browser
- 2.2 Enter the username `alice` and password `1234` to log in
- 2.3 Grant access for the scopes displayed on the consent UI

The credential should be issued at the end of the flow and logged to the terminal.
