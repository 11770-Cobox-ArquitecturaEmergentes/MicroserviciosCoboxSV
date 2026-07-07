## Fases recomendadas

### Fase 1: Alinear servicios local vs AWS

Objetivo: que el workflow y `docker-compose.aws.yml` hablen el mismo idioma.

Enfócate en:

```text
- Agregar servicios faltantes al array de aws-deploy.yml.
- Decidir e incluir support-service en AWS.
- Verificar que cada servicio tenga imagen ECR esperada.
- Verificar que cada servicio tenga env file esperado.
```

Commit sugerido:

```bash
git commit -m "feat(devops): align aws service deployment list"
```

---

### Fase 2: Hacer que el deploy falle correctamente

Objetivo: eliminar falsos positivos.

Enfócate en:

```text
- Quitar || true en docker compose pull.
- Quitar || true en docker compose up.
- Agregar set -euo pipefail en scripts remotos.
- Agregar validación previa de archivos requeridos en /opt/cobox.
```

Commit sugerido:

```bash
git commit -m "fix(devops): fail aws deploy on compose errors"
```

---

### Fase 3: Completar archivos de runtime AWS

Objetivo: que EC2 tenga todo lo que `docker-compose.aws.yml` necesita.

Enfócate en:

```text
- Copiar nginx.conf.
- Copiar prometheus.yml.
- Validar /opt/cobox/env/*.env.
- Agregar support.env si incluyes support-service.
- Documentar plantilla mínima de env si no quieres subir secretos.
```

Commit sugerido:

```bash
git commit -m "feat(devops): validate aws runtime files"
```

---

### Fase 4: Persistencia y seguridad básica de runtime

Objetivo: que RabbitMQ/Postgres/observabilidad no queden inseguros o efímeros.

Enfócate en:

```text
- Agregar volumen persistente para RabbitMQ.
- Evitar guest/guest en AWS.
- Restringir Prometheus/Grafana a localhost si aplica.
- Revisar exposición de puertos.
```

Commit sugerido:

```bash
git commit -m "chore(devops): harden aws runtime services"
```

---

### Fase 5: Optimización de memoria

Objetivo: que la VM no muera por 13 Spring Boots.

Enfócate en:

```text
- Agregar JAVA_TOOL_OPTIONS por servicio.
- Agregar mem_limit por contenedor.
- Dar más memoria a gateway, mobile-bff, desktop-bff y ai-validation.
- Mantener límites más bajos en servicios secundarios.
```

Commit sugerido:

```bash
git commit -m "chore(devops): add container memory limits"
```

---

### Fase 6: Ajustes del entorno local

Objetivo: que local siga funcionando igual o mejor.

Enfócate en:

```text
- mobile-bff-service depende de rabbitmq healthy.
- postgres-db usa volumen data.
- rabbitmq usa volumen rabbitmq_data.
- start-infrastructure.ps1 valida Java 21.
- start-infrastructure.ps1 valida archivos necesarios.
```

Commit sugerido:

```bash
git commit -m "fix(devops): improve local infrastructure startup"
```

## Orden recomendado

```text
1. Fase 1
2. Fase 2
3. Fase 3
4. Probar deploy AWS seco
5. Fase 6 local
6. Fase 4
7. Fase 5
```

No mezcles todo en un solo commit. Esta rama puede tocar mucho archivo sensible; conviene poder revertir fases específicas.
