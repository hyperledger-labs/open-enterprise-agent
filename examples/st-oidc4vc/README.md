# How to run issuance flow

## Prerequisites

- Docker installed
- Python 3 with the following packages installed
  - [requests](https://pypi.org/project/requests/)
  - [pyjwt](https://pyjwt.readthedocs.io/en/stable/)
  - [cryptography](https://cryptography.io/en/latest/)

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
