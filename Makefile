OSSINDEX_ERRORS = "Unable to contact OSS Index|authentication failed|401 Unauthorized|403 Forbidden|429 Too Many Requests|Too many requests|Rate limit|Unknown host|Connection refused|timed out|unreachable|402 Payment Required"

.PHONY: all
all: audit test build

.PHONY: audit
audit:
	@echo "🔍 Running OSS Index audit..." && \
	mkdir -p target && \
	mvn -B ossindex:audit > target/ossindex-audit.log 2>&1; status=$$?; cat target/ossindex-audit.log; \
	[ $$status -eq 0 ] && grep -Eiqn $(OSSINDEX_ERRORS) target/ossindex-audit.log && \
		{ echo "❌ OSS Index API/auth/network error detected — see target/ossindex-audit.log"; exit 1; }; \
	exit $$status

.PHONY: build
build:
	mvn -Dmaven.test.skip -Dossindex.skip=true clean package dependency:copy-dependencies

.PHONY: debug
debug:
	./run.sh

.PHONY: test
test:
	mvn -Dossindex.skip=true test
