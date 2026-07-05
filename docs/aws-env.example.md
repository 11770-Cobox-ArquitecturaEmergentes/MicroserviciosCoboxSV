# AWS runtime env files

Create these files on the EC2 instance under `/opt/cobox/env`. Do not commit real
secret values to the repository.

## Database-backed services

These files require the same minimum database variables because production
Postgres is expected to be external to `docker-compose.aws.yml`:

- `/opt/cobox/env/iam.env`
- `/opt/cobox/env/fleet.env`
- `/opt/cobox/env/delivery.env`
- `/opt/cobox/env/support.env`
- `/opt/cobox/env/edge.env`
- `/opt/cobox/env/mobile-bff.env`
- `/opt/cobox/env/ai-validation.env`
- `/opt/cobox/env/incident.env`
- `/opt/cobox/env/maintenance.env`

```env
POSTGRES_HOST=your-rds-endpoint.amazonaws.com
POSTGRES_PORT=5432
POSTGRES_DATABASE=cobox
POSTGRES_USER=cobox_app
POSTGRES_PASSWORD=replace-with-secret
AUTH0_ISSUER_URI=https://your-auth0-domain/
AUTH0_AUDIENCE=https://api.coboxsv.dev
```

`mobile-bff.env` and `ai-validation.env` also use RabbitMQ and S3 settings:

```env
RABBITMQ_USERNAME=replace-with-rabbitmq-user
RABBITMQ_PASSWORD=replace-with-rabbitmq-password
AWS_REGION=us-east-1
S3_EVIDENCE_BUCKET=replace-with-bucket
S3_PRESIGNED_URL_EXPIRATION_MINUTES=15
```

AWS access keys are not required in these files when the EC2 instance role grants
the required S3 permissions. Add `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
only if there is no instance role.

## Desktop BFF

`/opt/cobox/env/desktop-bff.env`

```env
AUTH0_ISSUER_URI=https://your-auth0-domain/
AUTH0_AUDIENCE=https://api.coboxsv.dev
```

## RabbitMQ

`/opt/cobox/env/rabbitmq.env`

```env
RABBITMQ_DEFAULT_USER=replace-with-non-guest-user
RABBITMQ_DEFAULT_PASS=replace-with-secret
```

The deploy script rejects `guest` for either value in AWS.

## Grafana

`/opt/cobox/env/grafana.env`

```env
GF_SECURITY_ADMIN_PASSWORD=replace-with-secret
```

The deploy script rejects the default `admin` password in AWS.
