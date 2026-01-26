# Variables
IMAGE_NAME = booking-service-backend
CURRENT_GIT_HASH = $(shell git rev-parse --short HEAD)
PREVIOUS_GIT_HASH = $(shell git rev-parse --short HEAD~1)
DOCKER_TAG = $(IMAGE_NAME):$(CURRENT_GIT_HASH)

.PHONY: all test clean dev
dev:
	./gradlew bootRun

test:
	./gradlew test

docker-run: docker-build
	docker run --rm -p 8080:8080 -v $(shell pwd)/src/main/resources:/app/resources $(DOCKER_TAG)


docker-build:
	docker buildx build --platform linux/arm64 -t $(DOCKER_TAG) --load .




# Push the multi-arch image to the registry
docker-push:
	docker buildx build --platform linux/amd64,linux/arm64 -t $(DOCKER_TAG) --push .

verify:
	docker buildx imagetools inspect $(DOCKER_TAG)

# 下載 migrations from S3
atlas/sync/download:
	aws s3 sync s3://talos-cluster-terraform-state/prod-db-migration/subscriptions ./migrations

# 上傳 migrations to S3
atlas/sync/upload:
	aws s3 sync ./migrations s3://talos-cluster-terraform-state/prod-db-migration/subscriptions

# ========================================
# 開發用：比較 schema 差異
# ========================================
atlas/db-inspect:
	source .env && \
	atlas schema inspect \
		-u "postgres://$${DB_USER}:$${DB_PASSWORD}@$${DB_HOST}:$${DB_PORT}/$${DB_NAME}" \
		> atlas-inspect.hcl

atlas/diff: atlas/db-inspect
	atlas schema diff \
		--from file://atlas-inspect.hcl \
		--to file://src/main/resources/hcl \
		--dev-url "docker://postgres/17/dev" > .plan
	cat .plan
	rm atlas-inspect.hcl

atlas/apply/tenant:
	source .env && \
	atlas schema apply \
	  --url "postgres://$${DB_USER}:$${DB_PASSWORD}@$${DB_HOST}:$${DB_PORT}/$${DB_NAME}" \
	  --to file://src/main/resources/hcl \
	  --dev-url "docker://postgres/17/dev" \
  	  --var tenant=${TENANT} \
  	  --schema ${TENANT},public

terraform/apply:

	source .env && \
	terraform -chdir=terraform apply -var="database_url=postgres://$${DB_USER}:$${DB_PASSWORD}@$${DB_HOST}:$${DB_PORT}/$${DB_NAME}"


